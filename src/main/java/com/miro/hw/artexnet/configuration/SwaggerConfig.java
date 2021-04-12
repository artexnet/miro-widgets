package com.miro.hw.artexnet.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${api.settings.title}")
    private String apiTitle;

    @Value("${api.settings.description}")
    private String apiDescription;

    @Value("${api.settings.version}")
    private String version;

    @Value("${contants.name}")
    private String contactName;

    @Value("${contants.url}")
    private String url;

    @Value("${contants.email}")
    private String email;

    @Bean
    public Docket apiV1() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("miro-widgets")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.miro.arturm.api"))
                .paths(regex("/api/.*"))
                .build()
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(apiTitle)
                .description(apiDescription)
                .termsOfServiceUrl("https://miro.com/legal/terms-of-service/")
                .contact(new Contact(contactName, url, email))
                .version(version)
                .build();
    }

}
