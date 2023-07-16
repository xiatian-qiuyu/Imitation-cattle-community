package com.nowcoder.community.service;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;


public interface UserService {
    //根据id查询用户
    User findUserById(int id);

    //根据名称查询用户
    User findUserByName(String username);

    //通过邮箱查询用户
    User findByEmail(String email);

    //注册用户
    Map<String,Object> Register(User user);

    //激活
    int activation(int id,String code);

    Map<String,Object> login(String username,String passsword,int expiredSeconds);

    void logout(String ticket);

    // 修改头像
    int updateHeader(int id, String headerUrl);

    // 修改密码
    Map<String,Object> updatePassword(String email, String password);

    LoginTicket findLoginTicket(String ticket);

    void updatePassword(int id, String newPassword);

    Collection<?extends GrantedAuthority> getAuthorities(int userId);
}
