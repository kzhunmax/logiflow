package com.logiflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
@EnableSpringDataWebSupport(
		pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
)
public class LogiFlowApplication {

	static void main(String[] args) {
		SpringApplication.run(LogiFlowApplication.class, args);
	}

}
