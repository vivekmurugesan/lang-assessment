package com.langassessment.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.beans.factory.annotation.Value;

import java.util.Properties;

@Configuration
public class MailConfiguration {

    @Bean
    @ConditionalOnProperty(name = "mail.host")
    public JavaMailSender javaMailSender(
            @Value("${mail.host:}") String mailHost,
            @Value("${mail.port:587}") String mailPortStr,
            @Value("${mail.username:}") String mailUsername,
            @Value("${mail.password:}") String mailPassword) {

        int mailPort = 587;
        try {
            if (mailPortStr != null && !mailPortStr.isEmpty()) {
                mailPort = Integer.parseInt(mailPortStr);
            }
        } catch (NumberFormatException e) {
            // Use default port
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailHost);
        sender.setPort(mailPort);
        sender.setUsername(mailUsername);
        sender.setPassword(mailPassword);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        return sender;
    }
}
