package com.iknow.community.event;

import com.alibaba.fastjson.JSONObject;
import com.iknow.community.bean.DiscussPost;
import com.iknow.community.bean.Event;
import com.iknow.community.bean.Message;
import com.iknow.community.mapper.DiscussPostMapper;
import com.iknow.community.service.ElasticsearchService;
import com.iknow.community.service.MessageService;
import com.iknow.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant{

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
            logger.error("消息格式错误!");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        if (!event.getData().isEmpty()){
            for (Map.Entry<String,Object> entry:event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }


    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
            logger.error("消息格式错误!");
            return;
        }

        DiscussPost post = discussPostMapper.selectDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
            logger.error("消息格式错误!");
            return;
        }
        // 从elasticsearch服务器中删除该帖子
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if (event == null){
            logger.error("消息格式错误!");
            return;
        }
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");
        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;

        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功: "+cmd);
        } catch (IOException e) {
            logger.error("生成长图失败: "+cmd);
        }
    }

}
