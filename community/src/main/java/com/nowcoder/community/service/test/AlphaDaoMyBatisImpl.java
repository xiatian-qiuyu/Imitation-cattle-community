package com.nowcoder.community.service.test;

import com.nowcoder.community.mapper.AlphaDao;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class AlphaDaoMyBatisImpl implements AlphaDao {
    @Override
    public String select() {
        return "MyBatis";
    }
}
