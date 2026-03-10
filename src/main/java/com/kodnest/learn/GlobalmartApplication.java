package com.kodnest.learn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class GlobalmartApplication {

	public static void main(String[] args) {
		SpringApplication.run(GlobalmartApplication.class, args);
	}

}
