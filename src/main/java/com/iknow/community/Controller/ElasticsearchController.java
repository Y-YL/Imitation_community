package com.iknow.community.Controller;

import com.iknow.community.bean.DiscussPost;
import com.iknow.community.bean.Page;
import com.iknow.community.service.ElasticsearchService;
import com.iknow.community.service.LikeService;
import com.iknow.community.service.UserService;
import com.iknow.community.util.CommunityConstant;
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
public class ElasticsearchController implements CommunityConstant{

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        // 搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchPosts = elasticsearchService.searchDiscussPost(keyword, page.getCurrent()-1, page.getLimit());
        // 聚合数据
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (searchPosts!=null){
            for (DiscussPost post:searchPosts){
                Map<String,Object> map = new HashMap<>();
                // 帖子
                map.put("post",post);
                // 作者
                map.put("user",userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);

        // 分页信息
        page.setPath("/search?keyword="+keyword);
        page.setRows(searchPosts == null? 0: (int) searchPosts.getTotalElements());


        return "/site/search";
    }


}
