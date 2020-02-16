package com.iknow.community;

import com.iknow.community.bean.DiscussPost;
import com.iknow.community.mapper.DiscussPostMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MapperTest {

    @Autowired
    DiscussPostMapper discussPostMapper;
    @Test
    void testSelDisPost(){

        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for(DiscussPost post:list){
            System.out.println(post);
        }

    }

}
