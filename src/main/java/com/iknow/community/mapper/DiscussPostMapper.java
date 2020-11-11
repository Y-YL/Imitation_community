package com.iknow.community.mapper;

import com.iknow.community.bean.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    // 分页相关方法  offset 起始行
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit,@Param("orderMode") int orderMode);

    // 查询帖子条数
    int selectDiscussPostRows(@Param("userId") int userId);

    // 插入新的发帖内容
    int insertDiscussPost(DiscussPost discussPost);

    // 查询帖子详细内容
    DiscussPost selectDiscussPostById(int discussPostId);

    int updateCommentCount(@Param("id") int id,@Param("commentCount") int commentCount);

    // 加精、删除
    int updateStatus(@Param("id") int id,@Param("status") int status);

    // 置顶
    int updateType(@Param("id") int id,@Param("type") int type);

    //  更新分数
    int updateScore(@Param("id") int id,@Param("score") double score);
}
