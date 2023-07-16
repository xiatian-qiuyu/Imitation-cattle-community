package com.nowcoder.community.service.impl;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.mapper.MessageMapper;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.SensitiveFilter;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    @Override
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    @Override
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    @Override
    public int findLettersCount(String conversationId) {
        return messageMapper.selectLettersCount(conversationId);
    }

    @Override
    public int findUnReadLettersCount(int userId, String conversationId) {
        return messageMapper.selectUnReadLettersCount(userId, conversationId);
    }

    @Override
    public int addMessage(Message message) {
        //将html标签转义，防止恶意代码注入
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    @Override
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateMessageStatus(ids,1);
    }
    /**
     * 撤回消息
     */
    @Override
    public int withdrawMessage(int id) {
        return messageMapper.updateMessageStatusById(id,2);
    }

    @Override
    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    @Override
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    @Override
    public int findNoticeUnreadcount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadcount(userId, topic);
    }

    @Override
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId,topic,offset,limit);
    }
}
