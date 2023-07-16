package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class QuzrtzTest {

    @Autowired
    private Scheduler Scheduler;

    @Test
    public void deleteJob()  {
//         删除任务
        try {
            boolean result = Scheduler.deleteJob(new JobKey("postScoreRefreshJob", "communityJobGroup"));
            System.out.println(result);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
