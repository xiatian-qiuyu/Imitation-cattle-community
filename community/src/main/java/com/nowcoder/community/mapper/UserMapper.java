package com.nowcoder.community.mapper;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

//@Repository是一个比@Component更加具体的注解，用于标注数据访问组件，即DAO组件。
//@Mapper是MyBatis框架提供的注解，用于标注数据访问组件，即DAO组件。
//@Mapper和@Repository都要标注吗？还是说只要标注一个就可以了？

@Repository
@Mapper
public interface UserMapper {
    //根据id查询用户
    User selectUserById(int id);
    //根据名称查询用户
    User selectUserByName(String username);
    //通过邮箱查询用户
    User selectByEmail(String email);
}
