package com.eduflex.service.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "eduflex.password-reset.delivery-enabled", havingValue = "true")
public class SmtpPasswordResetDelivery implements PasswordResetDelivery {
  private final JavaMailSender mailSender;
  private final String from;

  public SmtpPasswordResetDelivery(JavaMailSender mailSender,
      @Value("${eduflex.password-reset.from}") String from) {
    this.mailSender = mailSender;
    this.from = from;
  }

  @Override
  public void send(String email, String token, long ttlMinutes) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(email);
    message.setSubject("EduFlex password reset code");
    message.setText("Your EduFlex password reset code is:\n\n" + token
        + "\n\nIt expires in " + ttlMinutes + " minutes and can be used once.");
    mailSender.send(message);
  }
}
