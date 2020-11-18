package com.iknow.community.util;

import com.iknow.community.bean.User;
import org.springframework.stereotype.Component;

/**
 * ThreadLocal 中保存当前登录用户的信息
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
