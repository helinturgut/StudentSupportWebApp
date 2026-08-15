package com.studentsupport.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        String plainText =
                "We received a request to reset your password.\n\n"
                        + "Open this link to choose a new password. This link expires in 30 minutes:\n\n"
                        + resetLink
                        + "\n\nIf you didn't request this, you can safely ignore this email.";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Reset your Student Support & Career Advisor password");
            helper.setText(plainText, buildHtml(resetLink));
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", e);
        }
    }

    public void sendStaffWelcomeEmail(String toEmail, String fullName, String createPasswordLink) {
        String plainText =
                "Hi " + fullName + ",\n\n"
                        + "An administrator has set up a career support staff account for you on Student Support & "
                        + "Career Advisor.\n\n"
                        + "Open this link to create your password and activate your account. This link expires in "
                        + "7 days:\n\n"
                        + createPasswordLink;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("You're invited to join as career support staff");
            helper.setText(plainText, buildStatusHtml(
                    "You're invited to join as career support staff",
                    "An administrator has set up a career support staff account for you on Student Support "
                            + "&amp; Career Advisor. Click the button below to create your password and activate "
                            + "your account. This link expires in 7 days.",
                    createPasswordLink,
                    "Create your password"));
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Failed to send staff welcome email to {}", e);
        }
    }

    private String buildStatusHtml(String heading, String bodyHtml) {
        return buildStatusHtml(heading, bodyHtml, null, null);
    }

    private String buildStatusHtml(String heading, String bodyHtml, String buttonLink, String buttonLabel) {
        String button = buttonLink == null ? "" : """
                <p style="text-align: center; margin: 24px 0 0;">
                  <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #6d28d9, #c026d3); color: #ffffff; text-decoration: none; font-weight: 600; padding: 12px 28px; border-radius: 8px; font-size: 15px;">
                    %s
                  </a>
                </p>
                """.formatted(buttonLink, buttonLabel);

        return """
                <div style="font-family: -apple-system, 'Segoe UI', Roboto, Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 32px 24px; color: #3f3d46;">
                  <h1 style="font-size: 20px; color: #08060d; margin: 0 0 16px;">%s</h1>
                  <p style="font-size: 14px; line-height: 1.6; margin: 0;">
                    %s
                  </p>
                  %s
                </div>
                """.formatted(heading, bodyHtml, button);
    }

    private String buildHtml(String resetLink) {
        return """
                <div style="font-family: -apple-system, 'Segoe UI', Roboto, Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 32px 24px; color: #3f3d46;">
                  <h1 style="font-size: 20px; color: #08060d; margin: 0 0 16px;">Reset your password</h1>
                  <p style="font-size: 14px; line-height: 1.6; margin: 0 0 24px;">
                    We received a request to reset the password for your Student Support &amp; Career Advisor account.
                    Click the button below to choose a new one. This link expires in 30 minutes.
                  </p>
                  <p style="text-align: center; margin: 0 0 24px;">
                    <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #6d28d9, #c026d3); color: #ffffff; text-decoration: none; font-weight: 600; padding: 12px 28px; border-radius: 8px; font-size: 15px;">
                      Reset password
                    </a>
                  </p>
                  <p style="font-size: 12px; color: #6b6874; line-height: 1.6; margin: 0 0 8px;">
                    If the button doesn't work, copy and paste this link into your browser:
                  </p>
                  <p style="font-size: 12px; word-break: break-all; margin: 0 0 24px;">
                    <a href="%s" style="color: #6d28d9;">%s</a>
                  </p>
                  <p style="font-size: 12px; color: #6b6874; margin: 0;">
                    If you didn't request this, you can safely ignore this email.
                  </p>
                </div>
                """.formatted(resetLink, resetLink, resetLink);
    }
}
