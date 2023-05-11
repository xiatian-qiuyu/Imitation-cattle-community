package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DiscussPostService {
    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit);
    int findDisscussPostRow(int userId);

}
