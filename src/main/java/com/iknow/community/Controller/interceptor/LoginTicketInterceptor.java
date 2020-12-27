package com.iknow.community.Controller.interceptor;

import com.iknow.community.bean.LoginTicket;
import com.iknow.community.bean.User;
import com.iknow.community.service.UserServiceImpl;
import com.iknow.community.util.CookieUtil;
import com.iknow.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    UserServiceImpl userServiceImpl;

    @Autowired
    HostHolder hostHolder;

    /**
     * 登录之前校验，检查是否存在登录凭证，同时存在凭证就获取当前用户信息，并且保存在ThreadLocal中
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从Cookie 中获取凭证
        String ticket = CookieUtil.getValue(request,"ticket");

        if (ticket!=null){
            LoginTicket loginTicket = userServiceImpl.findLoginTicket(ticket);
            // 检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
                // 根据凭证查询用户
                User user = userServiceImpl.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户
                hostHolder.setUser(user);
                // 构建用户认证的结果，并存入SecurityContext,以便于Security进行授权.
                Authentication authentication = new UsernamePasswordAuthenticationToken(user,user.getPassword(), userServiceImpl.getAuthorities(user.getId()));
                SecurityContextHolder.setContext((new SecurityContextImpl(authentication)));
            }
        }
        return true;
    }

    /**
     * 在模板渲染之前，将登录信息保存到ModelAndView中，便于渲染页面使用
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user!=null && modelAndView !=null){
            modelAndView.addObject("loginUser",user);
        }
    }

    /**
     * 网页渲染完成后，将保存在ThreadLocal中的信息清除
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        hostHolder.clear();
    }
}
