package com.nowcoder.community.util;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;

/**
 * @Description 工具类
 */
public class CommunityUtil {
    // 随机数生成
    public static String gennerateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }
    // Md5加密
    public static String md5(String key){
        if(StringUtils.isEmpty(key)){
            return null;
        }
        //spring自带的工具类,DigestUtils可以对字符串进行加密
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
    /**
     * 生成JSON字符串的方法，方便我们在Controller中返回JSON数据
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        //创建json对象
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();

    }
    public static String getJSONString(int code, String msg){
        return getJSONString(code,msg,null);
    }
    public static String getJSONString(int code){
        return getJSONString(0,null,null);
    }
}
