package com.icronic_Sangeetha.serviceImpl;

import com.icronic_Sangeetha.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j // This automatically creates the 'log' variable for you
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendURL;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendCredentialsEmail(String toEmail, String userName, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Sangeetha - Your Temporary Password");

            String emailBody =
                    "Hi " + userName + ",\n\n"
                            + "We received a request to reset your password. Here is your temporary password:\n\n"
                            + "Temporary Password: " + password + "\n\n"
                            + "Please use this temporary password to log in to your account.\n\n"
                            + "IMPORTANT: For security reasons, please change your password immediately after logging in.\n\n"
                            + "You can log in at: " + frontendURL + "/login\n\n"
                            + "If you didn't request a password reset, please contact our support team immediately.\n\n"
                            + "Best regards,\n"
                            + "Musify Team";

            message.setText(emailBody); // Set the text
            mailSender.send(message);   // Actually send the email
            log.info("Credentials email sent successfully to {}", toEmail);

        } catch (Exception ex) {
            // Passing 'ex' as the last parameter prints the full stack trace automatically
            log.error("Failed to send temporary password email to: {}", toEmail, ex);
            throw new RuntimeException("Failed to send temporary password email");
        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String userName, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Musify - Your Account is Ready");

            String emailBody =
                    "Hi " + userName + ",\n\n"
                            + "Welcome to Sangeetha music streaming platform! Your account has been successfully created.\n\n"
                            + "Here are your login credentials:\n\n"
                            + "Email: " + toEmail + "\n"
                            + "Temporary Password: " + password + "\n\n"
                            + "You can log in at: " + frontendURL + "/login\n\n"
                            + "IMPORTANT: For security reasons, please change your password immediately after login.\n"
                            + "Start exploring and enjoying your favorite music!\n\n"
                            + "Best regards,\n"
                            + "Musify Team";

            message.setText(emailBody);
            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);

        } catch (Exception ex) {
            log.error("Failed to send welcome email to {} ", toEmail, ex);
            throw new RuntimeException("Failed to send welcome email");
        }
    }
}