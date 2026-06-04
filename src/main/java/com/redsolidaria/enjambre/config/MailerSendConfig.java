package com.redsolidaria.enjambre.config;

import com.mailersend.sdk.MailerSend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailerSendConfig {

    @Value("${mailersend.api.token}")
    private String apiToken;

    @Bean
    public MailerSend mailerSend() {
        MailerSend ms = new MailerSend();
        ms.setToken(apiToken);
        return ms;
    }
}
