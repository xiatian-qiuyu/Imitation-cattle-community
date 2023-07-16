package com.nowcoder.community.service.test;

import com.nowcoder.community.mapper.AlphaDao;
import org.springframework.stereotype.Repository;

@Repository("alphaHibernate")
public class AlphaDaoHibernateImpl implements AlphaDao {
    @Override
    public String select() {
        return "Hibernate";
    }
}
