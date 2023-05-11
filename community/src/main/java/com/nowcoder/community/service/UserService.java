package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Service;


public interface UserService {
    User findUserById(int id);
    //根据名称查询用户
    User findUserByName(String username);
    //通过邮箱查询用户
    User findByEmail(String email);
}
