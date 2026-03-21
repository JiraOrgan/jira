package com.jira.mng.global.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jira.mng.global.config.adapter.LocalDateAdapter;
import com.jira.mng.global.config.adapter.LocalDateTimeAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class GsonConfig {

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .disableHtmlEscaping()
                .create();
    }
}
