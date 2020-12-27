package com.iknow.community.service;

import com.iknow.community.bean.Comment;
import com.iknow.community.bean.DiscussPost;
import com.iknow.community.bean.LoginTicket;
import com.iknow.community.bean.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface UserService {
    /**
     * 通过id查询用户
     * @param userId
     * @return
     */
    public User findUserById(int userId);

    /**
     * 注册账号
     * @param user
     * @return
     */
    public Map<String, Object> register(User user);
    /**
     * 激活账号
     *
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId, String code);

    /**
     * 登录校验
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds);

    /**
     * 退出登录
     * @param ticket
     */
    public void logout(String ticket);

    /**
     * 查询登录凭证
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket);

    /**
     * 更新头像
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeaderUrl(int userId, String headerUrl);

    /**
     * 更改密码
     * @param userId
     * @param password
     * @return
     */
    public int updatePassword(int userId,String password);

    /**
     * 通过用户名查找用户
     * @param username
     * @return
     */
    public User findUserByName(String username);

    /**
     * 1. 优先从缓存中取值
     * @param userId
     * @return
     */
    public User getUserFromCache(int userId);

    /**
     *  2. 取不到值时，初始化缓存
     * @return
     */
    public User initUserCache(int userId);

    /**
     * 3. 当数据发生变动，清除缓存
     * @param userId
     */
    public void clearUserCache(int userId);

    public Collection<? extends GrantedAuthority> getAuthorities(int userId);

    /**
     * 获取当前用户所有的帖子
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<DiscussPost> findDiscussPostsByUserId(int userId, int offset, int limit);

    /**
     * 获取当前用户所有的回复
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Comment> findCommentsByUserId(int userId, int offset, int limit);

    /**
     * 获取当前用户所有的帖子数量
     * @param userId
     * @return
     */
    public int findDiscussPostsRowsByUserId(int userId);

    /**
     * 获取当前用户所有的评论数量
     * @param userId
     * @return
     */
    public int findCommentsRowsByUserId(int userId);

    /**
     * 通过Email查询用户
     * @param email
     * @return
     */
    public User findUserByEmail(String email);
}
