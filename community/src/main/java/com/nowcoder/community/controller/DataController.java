package com.nowcoder.community.controller;

import com.nowcoder.community.service.DataService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;

/**
 * 数据统计
 */
@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    //统计页面
    @RequestMapping(path="/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String toGetData(){
        return "/site/admin/data";
    }

    //统计网站UV
    @PostMapping("/data/uv")
    @ResponseBody
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end){

        //@DateTimeFormat(pattern = "yyyy-MM-dd")的作用:
        // 将前端传过来的String类型(yyyy-MM-dd)的日期转换成Date类型
        long uv = dataService.calculateUV(start, end);

        HashMap<String, Object> map = new HashMap<>();
        map.put("uvStart",start);
        map.put("uvEnd",end);
        map.put("uvResult",uv);
        return CommunityUtil.getJSONString(0,null,map);
    }

    //统计网站DAU
    @PostMapping("/data/dau")
    @ResponseBody
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end){
        long dau = dataService.calculateDAU(start, end);

        HashMap<String, Object> map = new HashMap<>();
        map.put("uvStart",start);
        map.put("uvEnd",end);
        map.put("dauResult",dau);
        return CommunityUtil.getJSONString(0,null,map);
    }
}
