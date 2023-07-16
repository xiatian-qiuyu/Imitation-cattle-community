package com.nowcoder.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@RequestMapping("/message")
public class MessageConteroller implements CommunityConstant {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    /**
     * 获取私信列表
     */
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));
        page.setPath("/message/letter/list");
        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        //会话列表不为空
        if(conversationList!=null){
            //遍历会话列表,获取每一个会话
            for (Message message:conversationList) {
                HashMap<String, Object> map = new HashMap<>();
                //1.会话
                map.put("conversation",message);
                //2.该会话的数量
                map.put("lettersCount",messageService.findLettersCount(message.getConversationId()));
                //3.该会话的未读数量
                map.put("unreadLetterCount",messageService.findUnReadLettersCount(user.getId(),message.getConversationId()));
                //4.会话的目标(我是当前用户，那么会话的目标就是另一个，无论是我发给对面还是对面发给我)
                int targetId = user.getId()==message.getFromId()? message.getToId():message.getFromId();
                User target  = userService.findUserById(targetId);
                map.put("target",target);
                //添加到会话列表
                conversations.add(map);
            }
        }
        //用户的所有未读数量
        int unreadLettersCount = messageService.findUnReadLettersCount(user.getId(),null);
        int noticeUnreadCount = messageService.findNoticeUnreadcount(user.getId(), null);

        model.addAttribute("unreadLetterCount",unreadLettersCount);
        model.addAttribute("conversations",conversations);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/letter";
    }

    /**
     * 获取私信详情
     */
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){
        page.setLimit(20);
        page.setRows(messageService.findLettersCount(conversationId));
        //modelAndView为空的原因可能是html模板没有写对，分页导致的modelAndView为空很可能是因为page的path写错了
        page.setPath("/message/letter/detail/"+conversationId);
        User user = hostHolder.getUser();
        //私信列表,支持分页
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters=new ArrayList<>();
        if(letterList!=null){
            for(Message letter:letterList){
                HashMap<String, Object> map = new HashMap<>();
                //如果是当前用户发的，获取我的信息
                if(user.getId()==letter.getFromId()){
                    map.put("meSend",user);
                    map.put("youSend",null);
                }else{
                    //否则获取对方的信息
                    map.put("youSend",userService.findUserById(letter.getFromId()));
                    map.put("meSend",null);
                }
//                map.put("fromUser",userService.findUserById(letter.getFromId()));
                map.put("letter",letter);
                letters.add(map);
            }
            //取出其中一条私信，获取目标
//            Message message = letterList.get(0);
//            int targetId = hostHolder.getUser().getId()==message.getFromId()?message.getToId():message.getFromId();
//            User user  = userService.findUserById(targetId);
        }
        User target = getTarget(conversationId);
        //将未读的消息改为已读
        List<Integer> unreadIds = getUnreadMessage(letterList);
        if(!unreadIds.isEmpty()){
            messageService.readMessage(unreadIds);
        }
        model.addAttribute("target",target);
        model.addAttribute("letters",letters);
        return "site/letter-detail";
    }

    /**
     * 获取未读的消息的id列表的封装方法
     * @param letterList
     * @return
     */
    private List<Integer> getUnreadMessage(List<Message> letterList) {
        List<Integer> unreadIds=new ArrayList<>();
        if(letterList!=null){
            for(Message message:letterList){
                if(message.getToId()==hostHolder.getUser().getId()){
                    unreadIds.add(message.getId());
                }
            }
        }
        return unreadIds;
    }


    /**
     * 获取私信目标的封装方法
     * @param conversationId
     * @return
     */
    private User getTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        return hostHolder.getUser().getId()==id0?userService.findUserById(id1):userService.findUserById(id0);
    }

    /**
     * 发送信息
     * @param toName
     * @param content
     * @return
     */
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetters(String toName,String content){
        User target = userService.findUserByName(toName);
        if(StringUtils.isEmpty(content)){
            return CommunityUtil.getJSONString(1,"消息不能为空");
        }
        if(toName==null){
            return CommunityUtil.getJSONString(1,"目标不能为空");
        }
        if(target==null){
            return CommunityUtil.getJSONString(1,"用户不存在");
        }
        Message message = new Message();
        message.setContent(content);
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setCreateTime(new Date());
        if(message.getFromId()<message.getToId()){
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }else{
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    //系统通知
    @GetMapping("/notice/list")
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();
        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if(message!=null){
            Map<String,Object> messageVo = new HashMap<>();
            //将之前的转义字符转换回来
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("message",message);
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityid",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));
            int count = messageService.findNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVo.put("count",count);
            int unreadCount = messageService.findNoticeUnreadcount(user.getId(),TOPIC_COMMENT);
            messageVo.put("unreadCount",unreadCount);

            model.addAttribute("commentNotice",messageVo);
        }
        //查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if(message!=null){
            Map<String,Object> messageVo = new HashMap<>();
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("message",message);
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));
            int count = messageService.findNoticeCount(user.getId(),TOPIC_LIKE);
            messageVo.put("count",count);
            int unreadCount = messageService.findNoticeUnreadcount(user.getId(),TOPIC_LIKE);
            messageVo.put("unreadCount",unreadCount);

            model.addAttribute("likeNotice",messageVo);
        }

        //查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if(message!=null){
            Map<String,Object> messageVo = new HashMap<>();
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("message",message);
            messageVo.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));
            int count = messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("count",count);
            int unreadCount = messageService.findNoticeUnreadcount(user.getId(),TOPIC_FOLLOW);
            messageVo.put("unreadCount",unreadCount);

            model.addAttribute("followNotice",messageVo);
        }
        //查询未读消息的数量
        int letterUnreadCount = messageService.findUnReadLettersCount(user.getId(), null);
        int noticeUnreadCount = messageService.findNoticeUnreadcount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return"/site/notice";
    }

    //查询某个主题的通知详情列表
    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic,Model model,Page page){
        User user = hostHolder.getUser();
        page.setLimit(10);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));
        page.setPath("/message/notice/detail/"+topic);

        List<Message> noticeList= messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        if(noticeList!=null&&!noticeList.isEmpty()){
            for (Message notice:noticeList) {
                HashMap<String, Object> map = new HashMap<>();
                //通知
                map.put("notice",notice);
                //内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                //通知作者
                map.put("fromUser",userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);

        //读消息(设置已读,打开了消息详情就设置已读状态)
        List<Integer> ids = getUnreadMessage(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }

    /**
     * 撤回消息
     */
    @PostMapping("/letter/withdraw")
    @ResponseBody
    public String withdrawMessage(int messageId){
        messageService.withdrawMessage(messageId);
        return CommunityUtil.getJSONString(0);
    }

}
