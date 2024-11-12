package com.authorizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CajuAuthorizerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CajuAuthorizerApplication.class, args);
	}

}
