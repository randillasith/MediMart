package org.pgno20.medimart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Expose the local uploads folder as /uploads/**
        Path uploadDir = Paths.get("uploads");
        String uploadUri = uploadDir.toUri().toString();

        if (!uploadUri.endsWith("/")) {
            uploadUri += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadUri);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/images/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/catalog").setViewName("forward:/catalog.html");
        // registry.addViewController("/medicines").setViewName("forward:/medicines.html");
    }
}
