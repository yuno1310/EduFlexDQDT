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
import com.eduflex.android.model.ResetPasswordRequest;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etResetCode, etNewPassword;
    private TextInputLayout tilResetCode, tilNewPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;
    private UserApi userApi;
    private boolean codeRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        userApi = ApiClient.getInstance().create(UserApi.class);

        etEmail = findViewById(R.id.et_email);
        etResetCode = findViewById(R.id.et_reset_code);
        etNewPassword = findViewById(R.id.et_new_password);
        tilResetCode = findViewById(R.id.til_reset_code);
        tilNewPassword = findViewById(R.id.til_new_password);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progress_bar);

        btnResetPassword.setOnClickListener(v -> {
            if (codeRequested) attemptReset(); else requestCode();
        });
    }

    private void requestCode() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        setLoading(true);
        userApi.forgotPassword(new ForgotPasswordRequest(email))
            .enqueue(new Callback<ForgotPasswordResponse>() {
                @Override
                public void onResponse(@NonNull Call<ForgotPasswordResponse> call,
                                       @NonNull Response<ForgotPasswordResponse> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                            response.body().getMessage(), Toast.LENGTH_LONG).show();
                        codeRequested = true;
                        etEmail.setEnabled(false);
                        tilResetCode.setVisibility(View.VISIBLE);
                        tilNewPassword.setVisibility(View.VISIBLE);
                        btnResetPassword.setText("Reset password");
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

    private void attemptReset() {
        String code = etResetCode.getText().toString().trim();
        String password = etNewPassword.getText().toString();
        if (code.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter the reset code and a new password", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        userApi.resetPassword(new ResetPasswordRequest(code, password))
            .enqueue(new Callback<ForgotPasswordResponse>() {
                @Override
                public void onResponse(@NonNull Call<ForgotPasswordResponse> call,
                        @NonNull Response<ForgotPasswordResponse> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                response.body().getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "The reset code or password is invalid.", Toast.LENGTH_LONG).show();
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
