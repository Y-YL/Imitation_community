package com.iknow.community.service;

import com.iknow.community.bean.DiscussPost;
import com.iknow.community.mapper.DiscussPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    DiscussPostMapper discussPostMapper;

   public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit){
      return  discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }

   public int findDiscussPostRow(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

}
