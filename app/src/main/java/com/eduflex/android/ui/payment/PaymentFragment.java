package com.eduflex.android.ui.payment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.eduflex.android.R;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.PaymentApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.cart.CartManager;
import com.eduflex.android.model.CartItem;
import com.eduflex.android.model.PaymentRequest;
import com.eduflex.android.model.PaymentResponse;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentFragment extends Fragment {

    private static final String TAG = "PaymentFragment";

    private LinearLayout optionCreditCard, optionPaypal, optionBank;
    private LinearLayout llCardForm;
    private LinearLayout llSummaryItems;
    private EditText etCardholder, etCardNumber, etExpiry, etCvv;
    private Button btnPayNow;
    private ProgressBar progressBar;
    private TextView tvSubtotal, tvDiscount, tvTotal;
    private String selectedMethod = "credit_card";
    private String courseId;

    private PaymentApi paymentApi;
    private TokenManager tokenManager;

    public PaymentFragment() {
        super(R.layout.fragment_payment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            courseId = getArguments().getString("courseId", "");
        }

        paymentApi = ApiClient.createAuthenticatedService(PaymentApi.class);
        tokenManager = new TokenManager(requireContext());

        bindViews(view);
        renderOrderSummary();
        setupPaymentMethodSelection();
        setupPayButton();
    }

    private void bindViews(View view) {
        optionCreditCard = view.findViewById(R.id.option_credit_card);
        optionPaypal = view.findViewById(R.id.option_paypal);
        optionBank = view.findViewById(R.id.option_bank);
        llCardForm = view.findViewById(R.id.ll_card_form);
        etCardholder = view.findViewById(R.id.et_cardholder);
        etCardNumber = view.findViewById(R.id.et_card_number);
        etExpiry = view.findViewById(R.id.et_expiry);
        etCvv = view.findViewById(R.id.et_cvv);
        btnPayNow = view.findViewById(R.id.btn_pay_now);
        progressBar = view.findViewById(R.id.progress_bar_payment);
        llSummaryItems = view.findViewById(R.id.ll_summary_items);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvDiscount = view.findViewById(R.id.tv_discount);
        tvTotal = view.findViewById(R.id.tv_total);
    }

    @Override
    public void onResume() {
        super.onResume();
        renderOrderSummary();
    }

    private void setupPaymentMethodSelection() {
        View.OnClickListener methodListener = v -> {
            // Reset all backgrounds
            optionCreditCard.setBackgroundResource(R.drawable.bg_payment_method);
            optionPaypal.setBackgroundResource(R.drawable.bg_payment_method);
            optionBank.setBackgroundResource(R.drawable.bg_payment_method);

            // Highlight selected
            v.setBackgroundResource(R.drawable.bg_payment_method_selected);

            int id = v.getId();
            if (id == R.id.option_credit_card) {
                selectedMethod = "credit_card";
                llCardForm.setVisibility(View.VISIBLE);
            } else if (id == R.id.option_paypal) {
                selectedMethod = "paypal";
                llCardForm.setVisibility(View.GONE);
            } else if (id == R.id.option_bank) {
                selectedMethod = "bank_transfer";
                llCardForm.setVisibility(View.GONE);
            }
        };

        optionCreditCard.setOnClickListener(methodListener);
        optionPaypal.setOnClickListener(methodListener);
        optionBank.setOnClickListener(methodListener);
    }

    private void setupPayButton() {
        btnPayNow.setOnClickListener(v -> {
            if ("credit_card".equals(selectedMethod)) {
                if (!validateCardForm()) return;
            }
            processPayment();
        });
    }

    private boolean validateCardForm() {
        String cardholder = etCardholder.getText().toString().trim();
        String cardNumber = etCardNumber.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        String cvv = etCvv.getText().toString().trim();

        if (cardholder.isEmpty()) {
            etCardholder.setError("Required");
            etCardholder.requestFocus();
            return false;
        }
        if (cardNumber.isEmpty() || cardNumber.length() < 13) {
            etCardNumber.setError("Enter a valid card number");
            etCardNumber.requestFocus();
            return false;
        }
        if (expiry.isEmpty() || expiry.length() < 4) {
            etExpiry.setError("Enter expiry date");
            etExpiry.requestFocus();
            return false;
        }
        if (cvv.isEmpty() || cvv.length() < 3) {
            etCvv.setError("Enter CVV");
            etCvv.requestFocus();
            return false;
        }
        return true;
    }

    private void processPayment() {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> cartItems = CartManager.getInstance().getItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Your cart is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        AtomicBoolean failed = new AtomicBoolean(false);
        AtomicInteger pending = new AtomicInteger(cartItems.size());
        for (CartItem item : cartItems) {
            paymentApi.processPayment(new PaymentRequest(userId, item.getCourseId()))
                    .enqueue(new Callback<PaymentResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<PaymentResponse> call,
                                               @NonNull Response<PaymentResponse> response) {
                            if (!isAdded()) return;
                            boolean succeeded = response.isSuccessful() && response.body() != null
                                    && response.body().isSuccess();
                            if (!succeeded) failed.set(true);
                            Log.d(TAG, "Simulated checkout for " + item.getCourseId() + ": " + response.code());
                            if (pending.decrementAndGet() == 0) finishCheckout(failed.get());
                        }

                        @Override
                        public void onFailure(@NonNull Call<PaymentResponse> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            failed.set(true);
                            Log.e(TAG, "Checkout failed for " + item.getCourseId() + ": " + t.getMessage());
                            if (pending.decrementAndGet() == 0) finishCheckout(true);
                        }
                    });
        }
    }

    private void renderOrderSummary() {
        if (!isAdded()) {
            return;
        }
        List<CartItem> cartItems = CartManager.getInstance().getItems();
        llSummaryItems.removeAllViews();

        double subtotal = 0d;
        for (CartItem item : cartItems) {
            double price = parsePrice(item.getPrice());
            subtotal += price;
            llSummaryItems.addView(createSummaryItemView(item, price));
        }

        tvSubtotal.setText(formatCurrency(subtotal));
        tvDiscount.setText("-$0.00");
        tvTotal.setText(formatCurrency(subtotal));
    }

    private View createSummaryItemView(CartItem item, double price) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setPadding(0, 0, 0, 12);

        TextView title = new TextView(requireContext());
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f);
        title.setLayoutParams(titleParams);
        title.setText(item.getCourseTitle());
        title.setTextSize(14f);
        title.setMaxLines(1);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

        TextView priceView = new TextView(requireContext());
        priceView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        priceView.setText(formatCurrency(price));
        priceView.setTextSize(14f);
        priceView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

        row.addView(title);
        row.addView(priceView);
        return row;
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }

    private double parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.trim().isEmpty()) {
            return 0d;
        }
        String normalized = rawPrice.replaceAll("[^\\d,.-]", "").replace(",", "");
        if (normalized.isEmpty() || "-".equals(normalized) || ".".equals(normalized)) {
            return 0d;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return 0d;
        }
    }

    private void finishCheckout(boolean failed) {
        setLoading(false);
        if (failed) {
            Toast.makeText(requireContext(),
                    "Some courses could not be enrolled. Retry the simulation.", Toast.LENGTH_LONG).show();
            return;
        }
        CartManager.getInstance().clear();
        Toast.makeText(requireContext(),
                "Simulation completed. No real payment method was charged.", Toast.LENGTH_LONG).show();
        NavHostFragment.findNavController(PaymentFragment.this).popBackStack();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnPayNow.setEnabled(!loading);
    }
}
