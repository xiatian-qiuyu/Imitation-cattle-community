package com.nowcoder.community.mapper;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface MessageMapper {
    //查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    List<Message> selectConversations(int userId,int offset,int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话的私信列表
    List<Message> selectLetters(String conversationId,int offset,int limit);

    //查询某个会话的包含的私信数量
    int selectLettersCount(String conversationId);

    //查询未读的私信数量(1.当前用户所有会话的未读私信数量 2.某个会话的未读私信数量)
    int selectUnReadLettersCount(int userId,String conversationId);

    //新增消息
    int insertMessage(Message message);

    //修改信息状态
    int updateMessageStatus(List<Integer> ids,int status);

    int updateMessageStatusById(int id, int status);

    //查询某个主题下的最新通知
    Message selectLatestNotice(int userId,String topic);

    //查询某个主题的通知数量
    int selectNoticeCount(int userId,String topic);

    //查询未读的通知数量(动态sql,null则查所有主题)
    int selectNoticeUnreadcount(int userId,String topic);

    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}


