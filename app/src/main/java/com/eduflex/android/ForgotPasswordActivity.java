package com.eduflex.android;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.UserApi;
import com.eduflex.android.model.ForgotPasswordRequest;
import com.eduflex.android.model.ForgotPasswordResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etNewPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;
    private UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        userApi = ApiClient.getInstance().create(UserApi.class);

        etEmail = findViewById(R.id.et_email);
        etNewPassword = findViewById(R.id.et_new_password);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progress_bar);

        btnResetPassword.setOnClickListener(v -> attemptReset());
    }

    private void attemptReset() {
        String email = etEmail.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (email.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        userApi.forgotPassword(new ForgotPasswordRequest(email, newPassword))
            .enqueue(new Callback<ForgotPasswordResponse>() {
                @Override
                public void onResponse(@NonNull Call<ForgotPasswordResponse> call,
                                       @NonNull Response<ForgotPasswordResponse> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                            "Password reset! Please log in.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String msg = (response.body() != null) ? response.body().getMessage() : "Reset failed";
                        Toast.makeText(ForgotPasswordActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ForgotPasswordResponse> call, @NonNull Throwable t) {
                    setLoading(false);
                    Toast.makeText(ForgotPasswordActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnResetPassword.setEnabled(!loading);
    }
}
