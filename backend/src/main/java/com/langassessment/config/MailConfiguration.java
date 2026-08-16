package com.langassessment.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {

    @Bean
    @ConditionalOnProperty(name = "mail.host")
    public JavaMailSender javaMailSender(
            org.springframework.boot.autoconfigure.mail.MailProperties mailProperties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());
        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", mailProperties.getProperties().getOrDefault("auth", "true"));
        props.put("mail.smtp.starttls.enable", mailProperties.getProperties().getOrDefault("starttls.enable", "true"));
        props.put("mail.smtp.starttls.required", mailProperties.getProperties().getOrDefault("starttls.required", "true"));

        return mailSender;
    }
}
