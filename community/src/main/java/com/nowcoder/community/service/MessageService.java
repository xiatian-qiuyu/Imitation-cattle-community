package com.nowcoder.community.service;

import com.nowcoder.community.entity.Message;

import java.util.List;


public interface MessageService {
    //查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    List<Message> findConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int findConversationCount(int userId);

    //查询某个会话的私信列表
    List<Message> findLetters(String conversationId,int offset,int limit);

    //查询某个会话的包含的私信数量
    int findLettersCount(String conversationId);

    //查询未读的私信数量(1.当前用户所有会话的未读私信数量 2.某个会话的未读私信数量)
    int findUnReadLettersCount(int userId,String conversationId);

    //添加信息
    int addMessage(Message message);


    //读信息，将状态改为已读
    int readMessage(List<Integer> ids);

    //删除信息
    int withdrawMessage(int id);

    //查询某个主题下的最新通知
    Message findLatestNotice(int userId,String topic);

    //查询某个主题的通知数量
    int findNoticeCount(int userId,String topic);

    //查询未读的通知数量(动态sql,null则查所有主题)
    int findNoticeUnreadcount(int userId,String topic);

    //查询某个主题的通知列表
    List<Message> findNotices(int userId,String topic,int offset,int limit);
}
