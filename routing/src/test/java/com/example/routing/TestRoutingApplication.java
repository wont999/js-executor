package com.example.routing;

import org.springframework.boot.SpringApplication;

public class TestRoutingApplication {

	public static void main(String[] args) {
		SpringApplication.from(RoutingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
