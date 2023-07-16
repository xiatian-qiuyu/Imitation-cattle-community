package com.nowcoder.community.service.impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.mapper.DiscussPostMapper;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
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
public class DiscussPostServiceImpl implements DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Value("${caffeine.posts.max-size}")
    private int maxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private long expireSeconds;
    private LoadingCache<String, List<DiscussPost>> postListCache;
    private LoadingCache<Integer,Integer> postRowsCache;

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostServiceImpl.class);

    @PostConstruct
    public void init(){
        //初始化热门帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {

                    //@Nullable和@NonNull是checker framework的注解，用来标记方法和参数是否可以为null
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key==null||key.length()==0){
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String[] split = key.split(":");
                        if(split==null||split.length!=2){

                        }
                        int offset = Integer.parseInt(split[0]);
                        int limit = Integer.parseInt(split[1]);
                        logger.debug("first load postList from DB...");
                        //也可以在这里加入二级缓存，先从redis中取，如果没有再从数据库中取
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });

        //初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("first load postRows from DB...");
                        return discussPostMapper.selectDisscussPostRow(0);
                    }
                });
    }

    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode) {
        /**
         * 热门帖子从本地缓存中取
         */
        if(userId==0&&orderMode==1){
            System.out.println("load hot postList from cache...");
            return postListCache.get(offset+":"+limit);
        }
        logger.debug("load hot postList from DB...");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit,orderMode);

    }

    @Override
    public int findDisscussPostRow(int userId) {
        /**
         * 从缓存中获取帖子总数
         */
        if(userId==0){
            System.out.println("load post Rows from cache...");
            return postRowsCache.get(userId);
        }
        logger.debug("load post Rows from DB...");
        return discussPostMapper.selectDisscussPostRow(userId);
    }

    @Override
    public int addDisscussPost(DiscussPost discussPost) {
        String content = discussPost.getContent();
        String title = discussPost.getTitle();
        //Html转义
        discussPost.setTitle(HtmlUtils.htmlEscape(title));
        discussPost.setContent(HtmlUtils.htmlEscape(content));
        //敏感词过滤
        discussPost.setTitle(sensitiveFilter.filter(title));
        discussPost.setContent(sensitiveFilter.filter(content));
        //调用方法
        return discussPostMapper.inseretDiscussPost(discussPost);
    }

    @Override
    public DiscussPost selectDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    @Override
    public int updateCommentCount(int entityId, int commentCount) {
        return discussPostMapper.updateCommentCount(entityId,commentCount);
    }

    @Override
    public DiscussPost findDiscussPostsById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    @Override
    public int updateDiscussPostType(int id, int type) {
        return discussPostMapper.updateDiscussPostType(id, type);
    }

    @Override
    public int updateDiscussPostStatus(int id, int status) {
        return discussPostMapper.updateDiscussPostStatus(id, status);
    }

    @Override
    public void updateDiscussPostScore(int id, double score) {
        discussPostMapper.updateDiscussPostScore(id, score);
    }

}
