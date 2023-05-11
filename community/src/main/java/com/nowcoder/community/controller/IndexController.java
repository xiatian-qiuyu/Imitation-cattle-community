package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;

    //首页分页显示帖子
    @RequestMapping(path={"/","/index"})
    public String getIndexPage(Model model, Page page){
       // 方法调用之前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
       // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDisscussPostRow(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        // discussposts是一个list，里面的每一个元素都是一个map，map里面有两个键值对
        // 一个是post，一个是user，post对应的是帖子，user对应的是发帖人
        List<Map<String,Object>> discussposts = new ArrayList<>();
        if(list!=null){
            //遍历list，将每一个帖子和发帖人的信息都放到map里面，然后将map放到discussposts里面
            for (DiscussPost post:list) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                discussposts.add(map);
            }
        }
        model.addAttribute("discussposts",discussposts);
        return "index";
    }

}
