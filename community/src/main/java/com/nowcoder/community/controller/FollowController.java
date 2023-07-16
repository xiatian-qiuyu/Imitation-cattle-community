package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;

    /**
     * 关注，取消关注
     * @param entityType
     * @param entityId
     * @return
     */
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(),entityType,entityId);
        HashMap<String, Object> map = new HashMap<>();
        boolean followStatus = false;
        if (hostHolder.getUser() != null) {
            followStatus = followService.followStatus(hostHolder.getUser().getId(), ENTITYTYPE_USER, entityId);
        }
        //粉丝数量
        long followerCount = followService.findFollowerCount(entityType, entityId);
        map.put("followerCount",followerCount);
        map.put("followStatus",followStatus);

        //触发关注事件
        if(followStatus){
            Event event = new Event()
                    .setTopic(TOPIC_FOLLOW)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityId);
        eventProducer.fileEvent(event);
        }
        return CommunityUtil.getJSONString(0,null,map);
    }

    /**
     * 关注列表
     */
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user==null){
            throw new RuntimeException("用户不存在!");
        }
        model.addAttribute("user",user);

        page.setLimit(10);
        page.setPath("/followees/"+userId);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITYTYPE_USER));
        List<Map<String, Object>> followeeList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if(followeeList!=null&& !followeeList.isEmpty()){
            for (Map<String, Object> map:followeeList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followees",followeeList);
        return "/site/followee";
    }
    private boolean hasFollowed(int userId){
        if(hostHolder.getUser()==null){
            return false;
        }
        return followService.followStatus(hostHolder.getUser().getId(), ENTITYTYPE_USER,userId);
    }

    /**
     *粉丝列表
     */
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user==null){
            throw new RuntimeException("用户不存在!");
        }
        model.addAttribute("user",user);
        page.setLimit(10);
        page.setPath("/followers/"+userId);
        page.setRows((int) followService.findFollowerCount(ENTITYTYPE_USER,userId));

        List<Map<String, Object>> followerList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(followerList!=null&& !followerList.isEmpty()){
            for (Map<String, Object> map:followerList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followers",followerList);
        return "/site/follower";
    }
}
