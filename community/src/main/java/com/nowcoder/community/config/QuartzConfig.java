package com.nowcoder.community.config;

import com.nowcoder.community.quartz.AlphaJob;
import com.nowcoder.community.quartz.DeleteWkImageJobJob;
import com.nowcoder.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import java.util.Date;

/**
 * Quartz配置类
 * 配置->数据库->调用
 * 这个配置只在第一次被读取到，然后将相应配置初始化到数据库中
 * (需要在application.properties中配置spring.quartz相关配置，不然默认使用内存)
 */

@Configuration
public class QuartzConfig {
    //BeanFactory是Spring容器的顶层接口
    // FactoryBean可简化Bean的实例化过程
    // 1.通过FactoryBean封装Bean的实例化过程
    // 2.将FactoryBean装配到Spring容器里
    // 3.将FactoryBean注入给其他的Bean
    // 4.该Bean得到的是FactoryBean所管理的对象实例

    //任务实例
    // 配置JobDetail
    //@Bean
//    public JobDetailFactoryBean alphaJobDetail(){
//        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
//        //设置JobDetail的具体实现类
//        factoryBean.setJobClass(AlphaJob.class);
//        //设置JobDetail的名字和组名
//        factoryBean.setName("alphaJob");
//        factoryBean.setGroup("alphaJobGroup");
//        //设置JobDetail的持久性和可恢复性
//        factoryBean.setDurability(true);
//        factoryBean.setRequestsRecovery(true);
//        return factoryBean;
//    }

    //触发器
    //配置Trigger(SimpleTriggerFactoryBean,CronTriggerFactoryBean)
    //@Bean
//    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
//        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
//        factoryBean.setJobDetail(alphaJobDetail);
//        factoryBean.setName("alphaTrigger");
//        factoryBean.setGroup("alphaTriggerGroup");
//        factoryBean.setJobDataMap(new JobDataMap());
//        //每隔3秒执行一次
//        factoryBean.setRepeatInterval(3000);
//        return factoryBean;
//    }

    // 刷新帖子分数任务
    //配置JobDetail
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    //配置Trigger(SimpleTriggerFactoryBean,CronTriggerFactoryBean)
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 30);
        factoryBean.setStartDelay(1000 * 60 );
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    // 刷新wk图片任务
    //配置JobDetail
    @Bean
    public JobDetailFactoryBean deleteWkImageJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(DeleteWkImageJobJob.class);
        factoryBean.setName("deleteWkImageJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    //删除wk图片触发器，每隔4分钟执行删除任务。
    @Bean
    public SimpleTriggerFactoryBean deleteWkImageTrigger(JobDetail deleteWkImageJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(deleteWkImageJobDetail);
        factoryBean.setName("deleteWkImageJobTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 4);
        factoryBean.setStartDelay(1000 * 60 );
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}

//注意：
//由于在一开始配置的时候我并没有设置时间间隔，而是在数据库已经有数据后面才设置的
//所以配置的时间间隔等并没有生效，而是使用了数据库中的数据
//因为这个配置类只在第一次被读取到，然后将相应配置初始化到数据库中，后面就不会再读取了。