package com.test.reactboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com")
@SpringBootApplication
public class ReactBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactBootApplication.class, args);
	}
}
