package com.phoenix.clickpic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:3000",  // 개발 중
                    "http://localhost:8080",  // 개발 중
                    "https://4cutstudio.store",  // 배포 중
                    "https://main.dooslw8q55bm7.amplifyapp.com", // 추가 도메인
                    "https://clickpic-s3-bucket.s3.ap-northeast-2.amazonaws.com" // AWS S3
                )
               .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // OPTIONS 허용
                //.allowedHeaders("*")
                .allowCredentials(true)  // 쿠키, 인증 정보 포함
                .maxAge(3600);  // Preflight 요청 캐싱 시간 (1시간)
    }
}

/*
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
              //.allowedOrigins("https://main.dooslw8q55bm7.amplifyapp.com/")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
*/