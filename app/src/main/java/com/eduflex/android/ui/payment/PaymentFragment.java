package com.eduflex.android.ui.payment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.eduflex.android.R;

public class PaymentFragment extends Fragment {

    private LinearLayout optionCreditCard, optionPaypal, optionBank;
    private LinearLayout llCardForm;
    private EditText etCardholder, etCardNumber, etExpiry, etCvv;
    private Button btnPayNow;
    private ProgressBar progressBar;
    private String selectedMethod = "credit_card";

    public PaymentFragment() {
        super(R.layout.fragment_payment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
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
        setLoading(true);

        // Simulate payment processing (UI only)
        btnPayNow.postDelayed(() -> {
            if (!isAdded()) return;
            setLoading(false);
            Toast.makeText(requireContext(), "Payment successful!", Toast.LENGTH_LONG).show();
            NavHostFragment.findNavController(this).popBackStack();
        }, 2000);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnPayNow.setEnabled(!loading);
    }
}
