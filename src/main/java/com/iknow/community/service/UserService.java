package com.iknow.community.service;

import com.iknow.community.bean.Comment;
import com.iknow.community.bean.DiscussPost;
import com.iknow.community.bean.LoginTicket;
import com.iknow.community.bean.User;
import com.iknow.community.mapper.UserMapper;
import com.iknow.community.util.CommunityConstant;
import com.iknow.community.util.CommunityUtil;
import com.iknow.community.util.MailClient;
import com.iknow.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 通过id查询用户
     * @param userId
     * @return
     */
    public User findUserById(int userId) {
        //        return userMapper.selectById(userId);
        User user = getUserFromCache(userId);
        if (user == null){
             user = initUserCache(userId);
        }
        return user;
    }

    /**
     * 注册账号
     * @param user
     * @return
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }
        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册");
        }
        // 设置注册用户信息
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));// 设置salt
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));//设置加密后的密码
        user.setType(0);// 普通用户
        user.setStatus(0);//未激活
        user.setActivationCode(CommunityUtil.generateUUID());//激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        System.out.println(user);
        userMapper.insertUser(user);

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 链接格式 http://localhost:8080/community/activation/{id}/code
        String activeUrl = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("activeUrl", activeUrl);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    /**
     * 激活账号
     *
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return CommunityConstant.ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            //  数据发生改变，清理缓存
            clearUserCache(userId);
            return CommunityConstant.ACTIVATION_SUCCESS;
        } else {
            return CommunityConstant.ACTIVATION_FAILURE;
        }
    }

    /**
     * 登录校验
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(username)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        //验证账号,使用用户名登录
        User user = userMapper.selectByName(username);
        if (user == null) {
            // 使用email作为账户登录
            user = userMapper.selectByEmail(username);
            if (user == null) {
                map.put("usernameMsg", "该账号不存在");
                return map;
            }
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        // 获取loginTicket的RedisKey
        String loginTicketKey = RedisKeyUtil.getLoginTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(loginTicketKey,loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 退出登录
     * @param ticket
     */
    public void logout(String ticket){
        String loginTicketKey = RedisKeyUtil.getLoginTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(loginTicketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(loginTicketKey,loginTicket);
    }

    /**
     * 查询登录凭证
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket){
        String loginTicketKey = RedisKeyUtil.getLoginTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(loginTicketKey);
    }

    /**
     * 更新头像
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeaderUrl(int userId, String headerUrl)
    {

        int rows = userMapper.updateHeader(userId,headerUrl);
        // 更新头像，数据发生改变
        clearUserCache(userId);
        return rows;
    }

    /**
     * 更改密码
     * @param userId
     * @param password
     * @return
     */
    public int updatePassword(int userId,String password){
        // 旧密码验证成功，设置新密码以及salt
        String salt = CommunityUtil.generateUUID().substring(0, 5);
        //设置加密后的密码
        password = CommunityUtil.md5(password + salt);
        // 清除缓存
        clearUserCache(userId);
        return userMapper.updatePassword(userId,password,salt);
    }

    /**
     * 通过用户名查找用户
     * @param username
     * @return
     */
    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    /**
     * 1. 优先从缓存中取值
     * @param userId
     * @return
     */
    public User getUserFromCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    /**
     *  2. 取不到值时，初始化缓存
     * @return
     */
    public User initUserCache(int userId){
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * 3. 当数据发生变动，清除缓存
     * @param userId
     */
    public void clearUserCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
              switch (user.getType()){
                  case 1:
                      return AUTHORITY_ADMIN;
                  case 2:
                      return AUTHORITY_MODERATOR;
                      default:
                          return AUTHORITY_USER;
              }
            }
        });
        return list;
    }

    /**
     * 获取当前用户所有的帖子
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<DiscussPost> findDiscussPostsByUserId(int userId,int offset,int limit){
        return userMapper.selectDiscussPostsByUserId(userId,offset,limit);
    }

    /**
     * 获取当前用户所有的回复
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Comment> findCommentsByUserId(int userId,int offset,int limit){
        return userMapper.selectCommentsByUserId(userId,offset,limit);
    }

    /**
     * 获取当前用户所有的帖子数量
     * @param userId
     * @return
     */
    public int findDiscussPostsRowsByUserId(int userId){
        return userMapper.selectDiscussPostsRowsByUserId(userId);
    }

    /**
     * 获取当前用户所有的评论数量
     * @param userId
     * @return
     */
    public int findCommentsRowsByUserId(int userId){
        return userMapper.selectCommentsRowsByUserId(userId);
    }

    public User findUserByEmail(String email){
        return userMapper.selectByEmail(email);
    }
}
