package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private LikeService likeService;
    @GetMapping("/search")
    public String search(String keyword, Page page, Model model){
        org.springframework.data.domain.Page<DiscussPost> discussList = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(discussList!=null&&!discussList.isEmpty()){
            for (DiscussPost post:discussList){
                HashMap<String, Object> map = new HashMap<>();
                map.put("post",post);
                map.put("user",userService.findUserById(post.getUserId()));
                map.put("likeCount",likeService.findEntityLikeCount(ENTITYTYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);

        //分页信息
        page.setLimit(10);
        page.setPath("/search?keyword="+keyword);
        page.setRows(discussList==null?0: (int) discussList.getTotalElements());
        return "/site/search";
    }
}
