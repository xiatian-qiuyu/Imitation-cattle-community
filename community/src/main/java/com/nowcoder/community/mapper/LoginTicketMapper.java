package com.nowcoder.community.mapper;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
@Deprecated
public interface LoginTicketMapper {

    // 根据ticket查询登录凭证
    LoginTicket selectByTicket(String ticket);

    // 根据ticket更新登录凭证的状态

    void updateStatus(String ticket, int status);

    // 插入登录凭证
    //使用自动生成的主键,映射到实体中
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);
}
