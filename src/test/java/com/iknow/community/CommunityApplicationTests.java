package com.iknow.community;

import com.iknow.community.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommunityApplicationTests {

	@Autowired
	UserMapper userMapper;

	@Test
	void contextLoads() {
		System.out.println(userMapper.selectById(1));
	}

}
