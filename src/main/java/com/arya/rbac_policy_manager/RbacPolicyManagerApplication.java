package com.arya.rbac_policy_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RbacPolicyManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RbacPolicyManagerApplication.class, args);
		System.out.println("Working fine");
	}

}
