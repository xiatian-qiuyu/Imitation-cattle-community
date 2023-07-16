package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.mapper.DiscussPostMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 压力测试
     */

    //添加30万条帖子
    @Test
    public void addPost(){
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setTitle("互联网求职计划");
            post.setContent("狠狠赚一笔！");
            post.setCreateTime(new java.util.Date());
            post.setScore(Math.random()*2000);
            post.setUserId(111);
            post.setStatus(0);
            discussPostMapper.inseretDiscussPost(post);
        }
    }
    @Test
    public void testCache(){
        //第一次查询
        System.out.println(discussPostMapper.selectDiscussPosts(0,0,10,1));
        //第二次查询
        System.out.println(discussPostMapper.selectDiscussPosts(0,0,10,1));
        //第三次查询
        System.out.println(discussPostMapper.selectDiscussPosts(0,0,10,1));
        //第四次查询
        System.out.println(discussPostMapper.selectDiscussPosts(0,0,10,0));
    }
}
