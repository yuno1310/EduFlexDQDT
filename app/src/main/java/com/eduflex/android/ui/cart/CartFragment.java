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
        llCartItems.setVisibility(View.VISIBLE);
        llCheckoutBar.setVisibility(View.VISIBLE);

        llCartItems.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
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
        }
    }

    private void showEmptyCart() {
        llCartEmpty.setVisibility(View.VISIBLE);
        llCartContent.setVisibility(View.GONE);
    }
}
