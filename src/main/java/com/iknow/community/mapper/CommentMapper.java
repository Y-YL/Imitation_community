package com.iknow.community.mapper;

import com.iknow.community.bean.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 查询评论
     * @param entityType 评论类型
     * @param entityId 评论Id
     * @param offset 查询起始行
     * @param limit  查询多少条数据
     * @return
     */
    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset,@Param("limit") int limit);

    /**
     * 查询评论的总条数 - 用于分页
     * @param entityType
     * @param entityId
     * @return
     */
    int selectCountByEntity(@Param("entityType") int entityType,@Param("entityId") int entityId);

    /**
     * 增加评论
     * @param comment
     * @return
     */
    int insertComment(Comment comment);

    Comment selectCommentById(int id);

    List<Comment> selectCommentsByUserId(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit);

    int selectCommentsRowsByUserId(int userId);
}
