package com.example.HAllTicket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Returns the persistent uploads directory path.
     * Lives at {project.dir}/uploads/ — OUTSIDE src/ and target/
     * so Maven NEVER wipes it on rebuild or restart.
     */
    public static String getUploadsDir() {
        String projectDir = System.getProperty("user.dir");
        try {
            // Use absolute path for safety
            File uploadsDir = new File(projectDir, "uploads");
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }
            // Absolute path with trailing slash
            String path = uploadsDir.getAbsolutePath();
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            return path;
        } catch (Exception e) {
            // Fallback for extreme cases
            return "uploads" + File.separator;
        }
    }

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        // Paths.get(...).toUri().toString() is the most robust way to get a file: URI
        String uploadsPath = Paths.get(getUploadsDir()).toUri().toString();

        // /img/** → uploads/img/ (persistent, survives restarts) + classpath fallback
        registry.addResourceHandler("/img/**")
                .addResourceLocations(
                    uploadsPath + "img/",
                    "classpath:/static/img/",
                    "file:src/main/resources/static/img/"
                );

        // /qr/** → uploads/qr/ (persistent, survives restarts) + classpath fallback
        registry.addResourceHandler("/qr/**")
                .addResourceLocations(
                    uploadsPath + "qr/",
                    "classpath:/static/qr/",
                    "file:src/main/resources/static/qr/"
                );
    }
}
