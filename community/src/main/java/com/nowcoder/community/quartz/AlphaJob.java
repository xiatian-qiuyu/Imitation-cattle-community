package com.nowcoder.community.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 定时任务
 */
public class AlphaJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 这里写定时任务的逻辑
        System.out.println(Thread.currentThread().getName() + ": execute a quartz job.");
    }
}
