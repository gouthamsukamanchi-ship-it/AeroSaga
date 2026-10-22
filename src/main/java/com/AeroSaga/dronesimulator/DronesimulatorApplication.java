package com.AeroSaga.dronesimulator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.aerosaga.dronesimulator")
@EnableJpaRepositories(basePackages = "com.aerosaga.dronesimulator.repository")
public class DronesimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DronesimulatorApplication.class, args);
    }
}
