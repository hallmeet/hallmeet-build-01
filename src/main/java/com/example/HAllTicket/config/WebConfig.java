package com.example.HAllTicket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        // Handle images from static/img directory
        registry.addResourceHandler("/img/**")
                .addResourceLocations(
                    "classpath:/static/img/",
                    "file:./src/main/resources/static/img/",
                    "file:" + System.getProperty("user.home") + File.separator + "HallTicket" + File.separator + "img" + File.separator
                );
        
        // Handle QR codes from static/qr directory
        String projectDir = System.getProperty("user.dir");
        String qrPath = projectDir + File.separator + "src" + File.separator + "main" + 
                       File.separator + "resources" + File.separator + "static" + File.separator + "qr" + File.separator;
        
        registry.addResourceHandler("/qr/**")
                .addResourceLocations(
                    "classpath:/static/qr/",
                    "file:" + qrPath,
                    "file:" + System.getProperty("user.home") + File.separator + "HallTicket" + File.separator + "qr" + File.separator
                );
    }
}

