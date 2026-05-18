package com.jobtracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("emailTaskExecutor")
    public void sendInterviewReminder(
            String toEmail,
            String recipientName,
            String companyName,
            String role,
            String roundType,
            String scheduledTime
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Interview Reminder: " + companyName + " — " + roundType);

            String htmlContent = buildInterviewReminderHtml(
                    recipientName, companyName, role, roundType, scheduledTime
            );
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Interview reminder sent to {} for {} at {}", toEmail, companyName, scheduledTime);
        } catch (MessagingException e) {
            log.error("Failed to send interview reminder to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildInterviewReminderHtml(
            String name, String company, String role, String roundType, String scheduledTime) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
                <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background:#0f172a; color:#e2e8f0; margin:0; padding:20px;">
                  <div style="max-width:600px; margin:0 auto; background:#1e293b; border-radius:12px; overflow:hidden;">
                    <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6); padding:30px; text-align:center;">
                      <h1 style="color:white; margin:0; font-size:24px;">⏰ Interview Reminder</h1>
                    </div>
                    <div style="padding:30px;">
                      <p style="font-size:16px;">Hi <strong>%s</strong>,</p>
                      <p>You have an upcoming interview scheduled within the next 24 hours!</p>
                      <div style="background:#0f172a; border-left:4px solid #6366f1; padding:20px; border-radius:8px; margin:20px 0;">
                        <p style="margin:5px 0;"><strong>🏢 Company:</strong> %s</p>
                        <p style="margin:5px 0;"><strong>💼 Role:</strong> %s</p>
                        <p style="margin:5px 0;"><strong>📋 Round:</strong> %s</p>
                        <p style="margin:5px 0;"><strong>🕐 Scheduled:</strong> %s</p>
                      </div>
                      <p>Best of luck! Remember to prepare your questions and review your past projects.</p>
                      <p style="color:#94a3b8; font-size:13px; margin-top:30px;">Job Application Tracker — Your career journey partner</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(name, company, role, roundType, scheduledTime);
    }
}
