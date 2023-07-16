package com.nowcoder.community;

import javax.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;

@SpringBootApplication
@MapperScan("com.nowcoder.community.mapper")
public class CommunityApplication {

	@PostConstruct
	public void init(){
		//解决Netty启动冲突
		//Netty4Utils.setAvailableProcessors
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		System.out.println("http://localhost:8083/community");
		SpringApplication.run(CommunityApplication.class, args);
	}

}
