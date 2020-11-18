package com.iknow.community.Controller;

import com.iknow.community.bean.*;
import com.iknow.community.event.EventProducer;
import com.iknow.community.service.CommentService;
import com.iknow.community.service.DiscussPostService;
import com.iknow.community.service.LikeService;
import com.iknow.community.service.UserService;
import com.iknow.community.util.CommunityConstant;
import com.iknow.community.util.CommunityUtil;
import com.iknow.community.util.HostHolder;
import com.iknow.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant{

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 发布新帖子
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(404,"请登录后继续操作!");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"发布成功!");
    }


    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model,
                                 Page page){

        // 获取帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        // 获取发帖人信息
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("post",post);
        model.addAttribute("user",user);
        // 获取该贴子的点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);
        // 获取该帖子的点赞状态
        int likeStatus =  hostHolder.getUser()==null? 0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",likeCount);
        // 评论分页信息
        page.setPath("/discuss/detail/"+discussPostId);
        page.setLimit(5);
        page.setRows(post.getCommentCount());

        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST,
                post.getId(),page.getOffset(),page.getLimit());
        // Vo -- view object  显示的对象
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if (commentList != null){
            for (Comment comment:commentList){
                //评论VO
                Map<String,Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment",comment);
                // 该条评论的作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                // 获取该评论的点赞数量
                 likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                 commentVo.put("likeCount",likeCount);
                // 获取该评论的点赞状态
                 likeStatus = hostHolder.getUser()==null? 0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                 commentVo.put("likeStatus",likeCount);

                // 回复评论
                // 查询所有的回复
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT,
                        comment.getId(),0,Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if (replyVoList != null){
                    for (Comment reply:replyList){
                        Map<String,Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply",reply);
                        // 回复作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        // 回复的目标
                        User target = reply.getTargetId()==0? null:userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);

                        // 获取该回复的点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        // 获取该回复的点赞状态
                        likeStatus =  hostHolder.getUser()==null? 0: likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeCount);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }

    // 置顶
    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int discussPostId){
        // 置顶，更新帖子类型
        discussPostService.updateType(discussPostId,1);
        // 修改在elasticsearch服务器中的状态
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }

    // 加精
    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int discussPostId){
        // 加精，更新帖子状态
        discussPostService.updateStatus(discussPostId,1);
        // 修改在elasticsearch服务器中的状态
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
        eventProducer.fireEvent(event);
        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScore();
        redisTemplate.opsForSet().add(redisKey,discussPostId);
        return CommunityUtil.getJSONString(0);
    }

    // 删除帖子
    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int discussPostId){
        // 删除，更新帖子状态
        discussPostService.updateStatus(discussPostId,2);
        // 修改在elasticsearch服务器中的状态
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }

}
