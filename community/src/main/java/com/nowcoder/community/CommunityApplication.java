package com.nowcoder.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;

@SpringBootApplication
@MapperScan("com.nowcoder.community.mapper")
public class CommunityApplication {

	public static void main(String[] args) {
		System.out.println("http://localhost:8083/");
		SpringApplication.run(CommunityApplication.class, args);
	}

}
