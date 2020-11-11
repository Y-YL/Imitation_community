package com.iknow.community.mapper;

import com.iknow.community.bean.Comment;
import com.iknow.community.bean.DiscussPost;
import com.iknow.community.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(@Param("id") int id, @Param("status") int status);

    int updateHeader(@Param("id") int id,@Param("headerUrl") String headerUrl);

    int updatePassword(@Param("id") int id,@Param("password") String password,@Param("salt")String salt);

    List<DiscussPost> selectDiscussPostsByUserId(@Param("userId")int userId,@Param("offset")int offset,@Param("limit")int limit);

    int selectDiscussPostsRowsByUserId(int userId);

    List<Comment> selectCommentsByUserId(@Param("userId")int userId,@Param("offset")int offset,@Param("limit")int limit);

    int selectCommentsRowsByUserId(int userId);
}
