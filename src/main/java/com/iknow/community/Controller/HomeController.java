package com.iknow.community.Controller;

import com.iknow.community.bean.DiscussPost;
import com.iknow.community.bean.Page;
import com.iknow.community.bean.User;
import com.iknow.community.service.DiscussPostService;
import com.iknow.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    UserService userService;
    @Autowired
    DiscussPostService discussPostService;

    @RequestMapping("/test")
    public String test(){
        return "/test";
    }


    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
         //设置总的条数
        page.setRows(discussPostService.findDiscussPostRow(0));
        // 设置访问路径
        page.setPath("/index");
        // 获取当前一页的数据，存在list，返回到页面中，进行展示
        List<Map<String,Object>> list = new ArrayList<>();
        List<DiscussPost> posts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        System.out.println(page.getLimit()+" "+page.getOffset());
        if (posts!=null){
            for (DiscussPost post :posts){
                Map<String ,Object> map = new HashMap<>();
                map.put("post",post);
                System.out.println(post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                list.add(map);
            }
        }
        model.addAttribute("discussPosts",list);
        return "index";
    }

}
