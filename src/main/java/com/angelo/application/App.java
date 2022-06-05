package com.angelo.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = App.BEAN_BASE_PACKAGE)
@EnableScheduling
@EnableJpaRepositories(basePackages = App.DAO_PACKAGE)
@EntityScan(basePackages = App.DAO_PACKAGE)
public class App 
{
	public static final String BEAN_BASE_PACKAGE = "com.angelo";
	public static final String DAO_PACKAGE = "com.angelo.dao";
	
    public static void main(String[] args ) {
    	 SpringApplication.run(App.class, args);
    }
}
