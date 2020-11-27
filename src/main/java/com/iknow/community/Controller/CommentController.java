package com.iknow.community.Controller;

import com.iknow.community.bean.Comment;
import com.iknow.community.bean.DiscussPost;
import com.iknow.community.bean.Event;
import com.iknow.community.event.EventProducer;
import com.iknow.community.service.CommentService;
import com.iknow.community.service.DiscussPostService;
import com.iknow.community.util.CommunityConstant;
import com.iknow.community.util.HostHolder;
import com.iknow.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant{

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {

        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        commentService.addComment(comment);

        // 触发评论事件，有人评论了帖子，系统发送站内信，通知用户被评论
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost targetDiscussPost = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(targetDiscussPost.getUserId());
        }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment targetComment = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(targetComment.getUserId());
        }
        eventProducer.fireEvent(event);

        if (comment.getEntityType() == ENTITY_TYPE_POST){
            //触发发帖事件
             event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScore();
            redisTemplate.opsForSet().add(redisKey,discussPostId);
        }


        return "redirect:/discuss/detail/" + discussPostId;
    }

}










