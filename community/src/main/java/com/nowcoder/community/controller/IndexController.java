package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nowcoder.community.util.CommunityConstant.ENTITYTYPE_POST;

@Controller
public class IndexController {
    @Autowired
    private UserService userService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;

    //首页分页显示帖子
    @RequestMapping(path={"/","/index"})
    public String getIndexPage(Model model, Page page, @RequestParam(name="orderMode",defaultValue = "0") int orderMode){
       // 方法调用之前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
       // 所以,在thymeleaf中可以直接访问Page对象中的数据.

        page.setRows(discussPostService.findDisscussPostRow(0));
        page.setPath("/index?orderMode="+orderMode);
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);

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
                long likeCount = likeService.findEntityLikeCount(ENTITYTYPE_POST, post.getId());
                map.put("likeCount",likeCount);
                discussposts.add(map);

            }
        }
        model.addAttribute("discussposts",discussposts);
        //将orderMode传给前端，用于判断是按照时间排序还是按照热度排序
        model.addAttribute("orderMode",orderMode);
        return "index";
    }

    /**
     * 500错误页面
     * @return
     */
    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }

    @GetMapping( "/denied")
    public String getDeniedPage() {
        return "/error/404";
    }
}
