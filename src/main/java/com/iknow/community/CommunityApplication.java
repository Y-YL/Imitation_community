package com.iknow.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	/**
	 *
	 */
	@PostConstruct
	public void init(){
		// 解决Netty启动冲突的问题
		// 原因：availableProcessors  redis和elasticsearch都会依赖，但是当redis已经启动了一个
		// availableProcessors后，elasticsearch会检测，发现存在就会报错，所以设置下面属性，可以避免报错
		System.setProperty("es.set.netty.runtime.available.processors","false");


	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
