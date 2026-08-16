package com.langassessment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:3000}")
    private String appBaseUrl;

    public void sendCandidateInvitation(String candidateEmail, String candidateName, String assessmentTitle, String secureLink, String tempPassword) {
        try {
            String assessmentLink = appBaseUrl + "/assessment/" + secureLink;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(candidateEmail);
            message.setSubject("Language Assessment Invitation: " + assessmentTitle);
            message.setText(buildEmailBody(candidateName, assessmentTitle, assessmentLink, tempPassword));

            mailSender.send(message);
            log.info("Invitation email sent successfully to: {}", candidateEmail);
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", candidateEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email invitation: " + e.getMessage());
        }
    }

    private String buildEmailBody(String candidateName, String assessmentTitle, String assessmentLink, String tempPassword) {
        String candidateLoginLink = appBaseUrl + "/candidate-login?secureLink=" + assessmentLink.split("/assessment/")[1];
        return "Dear " + candidateName + ",\n\n" +
                "You have been invited to take the '" + assessmentTitle + "' language assessment.\n\n" +
                "Click the link below to access the assessment:\n" +
                candidateLoginLink + "\n\n" +
                "If the link doesn't work, you can manually log in at:\n" +
                appBaseUrl + "/candidate-login\n\n" +
                "Your credentials:\n" +
                "Secure Link: " + assessmentLink.split("/assessment/")[1] + "\n" +
                "Password: " + tempPassword + "\n\n" +
                "The assessment evaluates your language proficiency according to the CEFR framework (A1-C2 levels).\n\n" +
                "If you have any questions, please contact the assessment administrator.\n\n" +
                "Best regards,\n" +
                "Language Assessment System";
    }
}
