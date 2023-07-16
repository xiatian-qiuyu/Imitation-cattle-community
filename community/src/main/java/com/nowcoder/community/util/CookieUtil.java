package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 获取Cookie的工具类
 */
public class CookieUtil {
    public static String getCookie(HttpServletRequest request,String name){
        if(request==null||name==null){
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for(Cookie cookie:cookies){
                if(cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void deleteCookie(HttpServletResponse response, String ticket) {
        if(response==null||ticket==null){
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie cookie = new Cookie("ticket","");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
