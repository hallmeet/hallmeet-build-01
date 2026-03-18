package com.example.HAllTicket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Returns the persistent uploads directory path.
     * Lives at {project.dir}/uploads/ — OUTSIDE src/ and target/
     * so Maven NEVER wipes it on rebuild or restart.
     */
    public static String getUploadsDir() {
        String projectDir = System.getProperty("user.dir");
        File uploadsDir = new File(projectDir + File.separator + "uploads");
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }
        return uploadsDir.getAbsolutePath() + File.separator;
    }

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        String uploadsPath = "file:" + getUploadsDir();

        // /img/** → uploads/img/ (persistent, survives restarts) + classpath fallback
        registry.addResourceHandler("/img/**")
                .addResourceLocations(
                    uploadsPath + "img" + File.separator,
                    "classpath:/static/img/",
                    "file:./src/main/resources/static/img/"
                );

        // /qr/** → uploads/qr/ (persistent, survives restarts) + classpath fallback
        registry.addResourceHandler("/qr/**")
                .addResourceLocations(
                    uploadsPath + "qr" + File.separator,
                    "classpath:/static/qr/",
                    "file:./src/main/resources/static/qr/"
                );
    }
}
