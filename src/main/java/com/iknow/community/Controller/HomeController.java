package com.iknow.community.Controller;

import com.iknow.community.bean.DiscussPost;
import com.iknow.community.bean.Page;
import com.iknow.community.bean.User;
import com.iknow.community.service.DiscussPostService;
import com.iknow.community.service.LikeService;
import com.iknow.community.service.UserServiceImpl;
import com.iknow.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant{

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @RequestMapping("/test")
    public String test(){
        return "/test";
    }

    @RequestMapping(path = "/",method = RequestMethod.GET)
    public String index(){
        return "forward:/index";
    }

    /**
     * 分页功能实现
     * 查询当前页数据
     * 查询数据库中所有数据
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,
                               @RequestParam(defaultValue = "0",name = "orderMode") int orderMode){
         //设置总的条数
        page.setRows(discussPostService.findDiscussPostRow(0));
        // 设置访问路径
        page.setPath("/index?orderMode="+orderMode);
        // 获取当前一页的数据，存在list，返回到页面中，进行展示
        List<Map<String,Object>> list = new ArrayList<>();
        List<DiscussPost> posts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        if (posts!=null){
            for (DiscussPost post :posts){
                Map<String ,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userServiceImpl.findUserById(post.getUserId());
                map.put("user",user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);
                list.add(map);
            }
        }
        model.addAttribute("discussPosts",list);
        model.addAttribute("orderMode",orderMode);
        return "/index";
    }

    /**
     * 返回500错误页面
     * @return
     */
    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

    @RequestMapping(path = "/denied",method = RequestMethod.GET)
    public String getDeniedPage(){
        return "/error/404";
    }
}
