package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

public interface ElasticsearchService {
    //保存帖子
    void saveDiscussPost(DiscussPost post);

    //删除帖子
    void deleteDiscussPost(int id);

    //搜索帖子
    //返回的是Page对象，里面包含了帖子的信息
    Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit);
}
