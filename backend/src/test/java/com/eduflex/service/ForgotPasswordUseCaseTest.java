package com.eduflex.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eduflex.dto.user.ForgotPasswordDTO.ForgotPasswordRequest;
import com.eduflex.dto.user.ForgotPasswordDTO.ResetPasswordRequest;
import com.eduflex.entity.user.UsersDbO;
import com.eduflex.generated.tables.records.UsersRecord;
import com.eduflex.repository.user.PasswordResetTokenRepository;
import com.eduflex.repository.user.UserRepository;
import com.eduflex.security.RefreshTokenService;
import com.eduflex.service.user.ForgotPasswordUseCase;
import com.eduflex.service.user.PasswordResetDelivery;

class ForgotPasswordUseCaseTest {
  @Test
  void recoveryIsUnavailableWithoutAConfiguredDeliveryChannel() {
    TestParts parts = parts(null);

    var response = parts.service.request(new ForgotPasswordRequest("learner@example.test"));

    assertThat(response.success()).isFalse();
    verify(parts.users, never()).find_by_email(any());
  }

  @Test
  void requestUsesGenericResponseAndStoresOnlyAHash() {
    PasswordResetDelivery delivery = mock(PasswordResetDelivery.class);
    TestParts parts = parts(delivery);
    UUID userId = UUID.randomUUID();
    UsersRecord record = new UsersRecord(userId, "learner@example.test", "hash", "Learner",
        null, true, "user", null, null);
    when(parts.users.find_by_email("learner@example.test")).thenReturn(new UsersDbO(record));

    var response = parts.service.request(new ForgotPasswordRequest("learner@example.test"));

    assertThat(response.success()).isTrue();
    ArgumentCaptor<String> delivered = ArgumentCaptor.forClass(String.class);
    verify(delivery).send(eq("learner@example.test"), delivered.capture(), eq(15L));
    ArgumentCaptor<String> stored = ArgumentCaptor.forClass(String.class);
    verify(parts.tokens).replaceForUser(eq(userId), stored.capture(), any());
    assertThat(stored.getValue()).hasSize(64).isNotEqualTo(delivered.getValue());
  }

  @Test
  void invalidOrReusedTokenCannotChangePassword() {
    TestParts parts = parts(mock(PasswordResetDelivery.class));
    when(parts.tokens.consume(any(), any())).thenReturn(null);

    var response = parts.service.confirm(new ResetPasswordRequest("expired", "Strong1!"));

    assertThat(response.success()).isFalse();
    verify(parts.users, never()).updatePasswordById(any(), any());
  }

  @SuppressWarnings("unchecked")
  private TestParts parts(PasswordResetDelivery delivery) {
    UserRepository users = mock(UserRepository.class);
    PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);
    RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
    ObjectProvider<PasswordResetDelivery> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(delivery);
    return new TestParts(users, tokens,
        new ForgotPasswordUseCase(users, tokens, encoder, refreshTokens, provider, 15));
  }

  private record TestParts(UserRepository users, PasswordResetTokenRepository tokens,
      ForgotPasswordUseCase service) {}
}
