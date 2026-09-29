package com.garagedesk;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class GarageDeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(GarageDeskApplication.class, args);
    }

    @Bean
    public CommandLineRunner printWebsiteLinks(Environment env) {
        return args -> {
            String port = env.getProperty("server.port", "8080");
            System.out.println("\n" +
                "====================================================================================\n" +
                "  🚀 GarageDesk Application is LIVE and READY!\n" +
                "------------------------------------------------------------------------------------\n" +
                "  🌐 Web Dashboard:            http://localhost:" + port + "\n" +
                "  📑 Swagger API Documentation: http://localhost:" + port + "/swagger-ui.html\n" +
                "  🗄️ H2 Database Console:       http://localhost:" + port + "/h2-console\n" +
                "====================================================================================\n"
            );
        };
    }
}
