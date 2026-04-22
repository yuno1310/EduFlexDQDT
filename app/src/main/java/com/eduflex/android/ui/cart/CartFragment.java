package com.eduflex.android.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.eduflex.android.R;
import com.eduflex.android.cart.CartManager;
import com.eduflex.android.model.CartItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class CartFragment extends Fragment {

    private LinearLayout llCartEmpty, llCartItems, llCheckoutBar;
    private Button btnCheckout;
    private TextView tvCartTotal;

    public CartFragment() {
        super(R.layout.fragment_cart);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        llCartEmpty = view.findViewById(R.id.ll_cart_empty);
        llCartItems = view.findViewById(R.id.ll_cart_items);
        llCheckoutBar = view.findViewById(R.id.ll_checkout_bar);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        tvCartTotal = view.findViewById(R.id.tv_cart_total);

        Button btnBrowseCourses = view.findViewById(R.id.btn_browse_courses);
        btnBrowseCourses.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.searchFragment);
            } else {
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.searchFragment);
            }
        });

        btnCheckout.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_cart_to_payment);
        });

        refreshCart();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCart();
    }

    private void refreshCart() {
        List<CartItem> items = CartManager.getInstance().getItems();
        if (items.isEmpty()) {
            showEmptyCart();
            return;
        }

        llCartEmpty.setVisibility(View.GONE);
        llCartItems.setVisibility(View.VISIBLE);
        llCheckoutBar.setVisibility(View.VISIBLE);

        llCartItems.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        double total = 0d;
        for (CartItem item : items) {
            View itemView = inflater.inflate(R.layout.item_cart, llCartItems, false);

            TextView tvTitle = itemView.findViewById(R.id.tv_cart_item_title);
            ImageView ivRemove = itemView.findViewById(R.id.iv_remove_item);
            ImageView ivThumbnail = itemView.findViewById(R.id.iv_cart_thumbnail);

            tvTitle.setText(item.getCourseTitle());
            Glide.with(requireContext())
                    .load(item.getImageUrl())
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.darker_gray)
                    .centerCrop()
                    .into(ivThumbnail);
            ivRemove.setOnClickListener(v -> {
                CartManager.getInstance().removeItem(item.getCourseId());
                refreshCart();
            });

            llCartItems.addView(itemView);
            total += parsePrice(item.getPrice());
        }
        tvCartTotal.setText(String.format(java.util.Locale.US, "$%.2f", total));
    }

    private void showEmptyCart() {
        llCartEmpty.setVisibility(View.VISIBLE);
        llCartItems.setVisibility(View.GONE);
        llCheckoutBar.setVisibility(View.GONE);
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
}
