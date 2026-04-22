package com.eduflex.android.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.eduflex.android.R;
import com.eduflex.android.cart.CartManager;
import com.eduflex.android.model.CartItem;

import java.util.List;

public class CartFragment extends Fragment {

    private LinearLayout llCartEmpty, llCartContent;
    private Button btnCheckout;

    public CartFragment() {
        super(R.layout.fragment_cart);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        llCartEmpty = view.findViewById(R.id.ll_cart_empty);
        llCartContent = view.findViewById(R.id.ll_cart_content);
        btnCheckout = view.findViewById(R.id.btn_checkout);

        Button btnBrowseCourses = view.findViewById(R.id.btn_browse_courses);
        btnBrowseCourses.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.coursesFragment);
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
        llCartContent.setVisibility(View.VISIBLE);
    }

    private void showEmptyCart() {
        llCartEmpty.setVisibility(View.VISIBLE);
        llCartContent.setVisibility(View.GONE);
    }
}
