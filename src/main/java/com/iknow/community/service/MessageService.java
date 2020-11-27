package com.iknow.community.service;

import com.iknow.community.bean.Message;
import com.iknow.community.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    //查询当前会话列表，针对每个会话，返回一个最新的私信
    public List<Message> findConversations(int userId,int offset, int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    }

    // 查询当前用户的会话数量
    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    // 查询某个会话所包含的私信列表
    public List<Message> findLetters(String conversationId, int offset, int limit){
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    // 查询某个会话中包含私信数量
    public int findLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    // 查询未读私信数量
    public int findLetterUnreadCount(int userId, String conversationId){
        return  messageMapper.selectLetterUnreadCount(userId,conversationId);
    }

    // 新增消息
    public int addMessage(Message message){
        return messageMapper.insertMessage(message);
    }

    // 更新消息状态
    public int readMessage(List<Integer> ids,int status){
        return messageMapper.updateMessageStatus(ids,status);
    }

    // 查询某个实体最新的通知
    public Message findLatestNotice(int userId,String topic){
        return messageMapper.selectLatestNotice(userId,topic);
    }
    // 查询某个实体通知数量
    public int findNoticeCount(int userId,String topic){
        return messageMapper.selectNoticeCount(userId,topic);
    }
    // 查询实体未读通知数量
    public int findNoticeUnreadCount(int userId,String topic){
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }

    public List<Message> findNotices(int userId,String topic,int offset,int limit){
        return messageMapper.selectNotices(userId,topic,offset,limit);
    }
}

