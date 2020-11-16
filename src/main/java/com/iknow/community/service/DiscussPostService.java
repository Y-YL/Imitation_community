package com.iknow.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.iknow.community.bean.DiscussPost;
import com.iknow.community.mapper.DiscussPostMapper;
import com.iknow.community.util.SensitiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static Logger logger = LoggerFactory.getLogger(DiscussPostService.class);
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //Caffeine核心接口 ：Cache, LoadingCache,AsynLoadCache

    // 帖子列表缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer,Integer> postRowsCache;

    /**
     * 初始化缓存
     */
    @PostConstruct
    public void init(){
        // 初始化缓存列表
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public List<DiscussPost> load(String key) throws Exception {
                        if (key == null || key.length() == 0){
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length!=2){
                            throw new IllegalArgumentException("参数错误!");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public Integer load(Integer key) throws Exception {

                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }


    /**
     * 分页查询数据
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode){
         //只缓存访问首页 热门的帖子数据
        if (userId == 0 && orderMode == 1 ){
            return postListCache.get(offset + ":" + limit);
        }
//        logger.info("load post list from DB.");
        return  discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    /**
     * 查询总共条数
     * @param userId
     * @return
     */
    public int findDiscussPostRow(int userId){
        if (userId == 0){
            return postRowsCache.get(userId);
        }
//        logger.info("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 添加发帖内容
     * @param discussPost
     * @return
     */
    public int addDiscussPost(DiscussPost discussPost){
        if (discussPost == null){
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 转义 HTML 标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    /**
     * 根据id查询帖子
     * @param discussPostId
     * @return
     */
    public DiscussPost findDiscussPostById(int discussPostId){
        return discussPostMapper.selectDiscussPostById(discussPostId);
    }

    /**
     * 更新评论数量
     * @param commentCount
     * @return
     */
    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    /**
     * 加精、删除，更改帖子状态
     * @param id
     * @param status
     * @return
     */
    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id,status);
    }

    /**
     * 置顶、更改帖子类型
     * @param id
     * @param type
     * @return
     */
    public int updateType(int id,int type){
        return discussPostMapper.updateType(id,type);
    }

    /**
     * 更新帖子分数（热度）
     * @param id
     * @param score
     * @return
     */
    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id,score);
    }
}
