package com.nowcoder.community.service;

import java.text.SimpleDateFormat;
import java.util.Date;

public interface DataService {
    public void recordUV(String ip);

    //统计指定日期范围内的UV
    public long calculateUV(Date start, Date end);

    // 将指定用户计入DAU
    public void recordDAU(int userId);
    //统计指定范围内的DAU
    public long calculateDAU(Date start,Date end);
}
