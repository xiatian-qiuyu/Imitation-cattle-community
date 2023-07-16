package com.nowcoder.community.util;
/*
* 常量接口
* */
public interface CommunityConstant {
    //激活成功
    int ACTIVATION_SUCCESS = 0;
    //重复激活
    int ACTIVATION_REPEAT = 1;
    //激活失败
    int ACTIVATION_FAILURE = 2;

    //60天
    int REMEMBER_EXPIRED_SECONDS=3600*24*60;
    //2小时
    int DEFAULT_EXPIRED_SECONDS=3600;

    /**
     * 帖子
     */
    int ENTITYTYPE_POST = 1;
    /**
     * 评论
     */
    int ENTITYTYPE_COMMENT = 2;

    /**
     * 用户
     */
    int ENTITYTYPE_USER = 3;

    /**
     * 系统用户
     */
    int SYSTEM_USER = 1;

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题：点赞
     */
    String TOPIC_LIKE = "like";
    /**
     * 主题：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题：发帖
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 主题：分享
     */
    String TOPIC_SHARE = "share";
    /**
     * 权限: 普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     * 权限: 管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限: 版主
     */
    String AUTHORITY_MODERATOR = "moderator";
}
