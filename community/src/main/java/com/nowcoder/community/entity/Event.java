package com.nowcoder.community.entity;

import org.apache.kafka.common.protocol.types.Field;

import java.util.HashMap;
import java.util.Map;

/**
 * 封装事件对象
 */
public class Event {
    //触发事件的用户
    private int userId;
    //触发事件的类型(评论，点赞，关注的实体类型，如：帖子，评论，用户)
    private int entityType;
    private int entityId;
    //触发事件的实体的作者（用来通知作者，评论，点赞，关注的对象）
    private int entityUserId;
    //主题，用来区分不同的事件
    private String topic;

    //用来存帖子Id，当有人评论帖子时把这个加到message的content上
    //用于拓展，存储其他信息
    private Map<String,Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    //返回this可以方便调用，如：xxx.setUserId(xx).setEntityType(xxx).setxxx
    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key,Object value) {
        this.data.put(key, value);
        return this;
    }
}
