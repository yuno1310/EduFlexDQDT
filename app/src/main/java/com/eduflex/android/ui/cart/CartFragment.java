package com.eduflex.android.ui.cart;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.eduflex.android.R;

public class CartFragment extends Fragment {

    private LinearLayout llCartEmpty, llCartItems, llCheckoutBar;
    private Button btnCheckout;

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

        // Show sample cart item for UI demonstration
        showCartWithItems();

        btnCheckout.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.paymentFragment);
        });

        // Remove item button
        ImageView ivRemoveItem = view.findViewById(R.id.iv_remove_item);
        ivRemoveItem.setOnClickListener(v -> showEmptyCart());
    }

    private void showCartWithItems() {
        llCartEmpty.setVisibility(View.GONE);
        llCartItems.setVisibility(View.VISIBLE);
        llCheckoutBar.setVisibility(View.VISIBLE);
    }

    private void showEmptyCart() {
        llCartEmpty.setVisibility(View.VISIBLE);
        llCartItems.setVisibility(View.GONE);
        llCheckoutBar.setVisibility(View.GONE);
    }
}

