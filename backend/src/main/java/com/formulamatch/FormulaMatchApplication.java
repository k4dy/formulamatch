package com.formulamatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FormulaMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FormulaMatchApplication.class, args);
    }
}
