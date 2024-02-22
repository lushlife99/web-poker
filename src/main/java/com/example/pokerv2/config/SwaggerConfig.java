package com.example.pokerv2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("포커 게임 API")
                        .description("포커 게임 서비스에 필요한 서버를 제공한다.")
                        .version("1.0.0"));
    }

}