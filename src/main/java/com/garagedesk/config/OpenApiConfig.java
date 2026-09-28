package com.garagedesk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI garageDeskOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GarageDesk — Vehicle Service Job Card and Bay Scheduling System")
                        .description("Backend REST API for managing vehicle job cards, service bay scheduling with occupancy conflict prevention, " +
                                "workflow status tracking with quality-check enforcement, and automated bill generation.\n\n" +
                                "**Sri Eshwar College of Engineering** — Project Leap (Java & DBMS Assessment)\n" +
                                "**Question 67** | **Student Reg No: 060**")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GarageDesk Project Team")
                                .email("student060@sece.ac.in"))
                        .license(new License().name("Educational Use Only")));
    }
}
