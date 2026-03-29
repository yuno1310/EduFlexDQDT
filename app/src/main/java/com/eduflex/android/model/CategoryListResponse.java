package com.eduflex.android.model;

import java.util.List;

public class CategoryListResponse {
    private boolean success;
    private String message;
    private List<Category> listCategory;

    public CategoryListResponse(boolean success, String message, List<Category> listCategory) {
        this.success = success;
        this.message = message;
        this.listCategory = listCategory;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Category> getListCategory() {
        return listCategory;
    }

    public void setListCategory(List<Category> listCategory) {
        this.listCategory = listCategory;
    }
}