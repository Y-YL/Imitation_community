package com.iknow.community.Controller;

import com.google.code.kaptcha.Producer;
import com.iknow.community.annotation.LoginRequired;
import com.iknow.community.bean.Comment;
import com.iknow.community.bean.DiscussPost;
import com.iknow.community.bean.Page;
import com.iknow.community.bean.User;
import com.iknow.community.service.*;
import com.iknow.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Controller
@RequestMapping(path = "/user")
public class UserController implements CommunityConstant{

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Producer kaptchaProduce;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        // 如果上传文件为空
        if (headerImage == null){
            model.addAttribute("error","您还没有选择图片上传!");
            return "/site/setting";
        }
        // 获取文件原始名
        String fileName = headerImage.getOriginalFilename();
        // 获取上传文件的后缀名
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 后缀名为空
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式错误!");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName  = CommunityUtil.generateUUID()+suffix;
        // 确定文件存放路径
        File dest = new File(uploadPath+"/"+fileName);
        System.out.println(dest);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常!");
        }

        // 更新当前用户头像访问路径
        // http://localhost:8080/community/user/header/xxx.png
        User user  = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userServiceImpl.updateHeaderUrl(user.getId(),headerUrl);
        // 更新后重定向到首页，刷新页面
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response){
        // 服务器存放路径
        filename = uploadPath + "/" +filename;
        // 文件后缀
        String  suffix = filename.substring(filename.lastIndexOf("."));
        response.setContentType("image/"+suffix);
        try {
            ServletOutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(filename);
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer))!=-1){
                os.write(buffer,0,b);
        }

        } catch (IOException e) {
        logger.error("读取头像失败!"+e.getMessage());
        }
    }

    @RequestMapping(path = "/updatepwd",method = RequestMethod.POST)
    public String updatePassword(@CookieValue("ticket")String ticket, String oldPassword, String newPassword, Model model){
        User user = hostHolder.getUser();
        oldPassword = CommunityUtil.md5(oldPassword+user.getSalt());
        if (!oldPassword.equals(user.getPassword())){
            model.addAttribute("oldPasswordMsg","原密码输入错误!");
            return "/site/setting";
        }
        // 更新数据库内容
        userServiceImpl.updatePassword(user.getId(),newPassword);
        // 密码修改成功，退出登录
        userServiceImpl.logout(ticket);
        model.addAttribute("target","/index");
        model.addAttribute("msg","您的密码修改成功!");
        // 跳转信息提示页面
        return "/site/operate-result";
    }

    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId")int userId,Model model){

        User user = userServiceImpl.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }
        // 用户
        model.addAttribute("user",user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page,Model model){
        User user = userServiceImpl.findUserById(userId);

        if (user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/user/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String,Object>> userList = followService.findFollowees(userId,page.getOffset(),page.getLimit());

        if (userList != null){
            for (Map<String,Object> map:userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);

        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page,Model model){
        User user = userServiceImpl.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/user/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String,Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());

        if (userList != null){
            for (Map<String,Object> map:userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);

        return "/site/follower";
    }

    private boolean hasFollowed(int userId){
        if (hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }

    @RequestMapping(path = "/mypost/{userId}",method = RequestMethod.GET)
    public String getMyPosts(@PathVariable("userId") int userId,Model model,Page page){
        page.setRows(userServiceImpl.findDiscussPostsRowsByUserId(userId));
        page.setPath("/myposts/"+userId);
        User user = hostHolder.getUser();
        List<DiscussPost> posts = discussPostService.findDiscussPosts(userId,page.getOffset(),page.getLimit(),0);//userService.findDiscussPostsByUserId(userId,page.getOffset(),page.getLimit());
        model.addAttribute("posts",posts);
        model.addAttribute("discussPostCounts",page.getRows());
        model.addAttribute("user",user);
        return "/site/my-post";
    }

    @RequestMapping(path = "/myreply/{userId}",method = RequestMethod.GET)
    public String getMyReply(@PathVariable("userId") int userId,Model model,Page page){
        User user = hostHolder.getUser();
        page.setRows(commentService.findCommentsRowsByUserId(userId));
        page.setPath("/myreply/"+userId);
        List<Comment> comments = commentService.findCommentsByUserId(userId,page.getOffset(),page.getLimit());//userService.findCommentsByUserId(userId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> list = new ArrayList<>();
        if (comments!=null){
            for(Comment comment:comments){
                Map<String,Object> map = new HashMap<>();
                map.put("reply",comment);
                map.put("replyDiscussPostTitle",discussPostService.findDiscussPostById(comment.getEntityId()).getTitle());
                list.add(map);
            }
        }
        model.addAttribute("replys",list);
        model.addAttribute("user",user);
        model.addAttribute("replyCounts",page.getRows());
        return "/site/my-reply";
    }


    @RequestMapping(path = "/forget",method = RequestMethod.GET)
    public String getForgetPage(){

        return "/site/forget";
    }

    @RequestMapping(path = "/resetpwd",method = RequestMethod.POST)
    public String resetPassword(String email,String password,String code,Model model){
        if (StringUtils.isBlank(email)){
            model.addAttribute("resetPwdMsg","邮箱不能为空!!!");
            return "/site/forget";
        }
        User user = userServiceImpl.findUserByEmail(email);
        String redisKey = RedisKeyUtil.getResetpasswordKaptcha(email);
        String  kaptchaCode = (String)redisTemplate.opsForValue().get(redisKey);
        if (code.equals(kaptchaCode)){
            userServiceImpl.updatePassword(user.getId(),password);
            model.addAttribute("msg","密码修改成功!");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("codeMsg","验证码错误");
            return "/site/forget";
        }
        return "/site/operate-result";
    }


    @RequestMapping(path = "/sendresetpwdkaptcha",method = RequestMethod.POST)
    @ResponseBody
    public String sendResetPasswordKaptcha(String email){
        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", email);
        // 生成验证码
        String code = kaptchaProduce.createText();
        context.setVariable("code", code);
        //存入redis,设置有效时间 5 分钟
        String redisKey = RedisKeyUtil.getResetpasswordKaptcha(email);
        System.out.println(redisKey);
        redisTemplate.opsForValue().set(redisKey,code,60*5, TimeUnit.SECONDS);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "忘记密码", content);
        return CommunityUtil.getJSONString(0);
    }


}
