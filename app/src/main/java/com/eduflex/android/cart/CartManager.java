package com.eduflex.android.cart;

import com.eduflex.android.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private final List<CartItem> items = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addItem(CartItem item) {
        for (CartItem existing : items) {
            if (existing.getCourseId().equals(item.getCourseId())) return;
        }
        items.add(item);
    }

    public boolean contains(String courseId) {
        for (CartItem item : items) {
            if (item.getCourseId().equals(courseId)) return true;
        }
        return false;
    }

    public void removeItem(String courseId) {
        items.removeIf(item -> item.getCourseId().equals(courseId));
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
