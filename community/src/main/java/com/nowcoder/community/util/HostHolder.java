package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 保存用户信息，用于代替session对象
 */
@Component
public class HostHolder {
    /**
     * ThreadLocal叫做线程变量，意思是ThreadLocal中填充的变量属于当前线程，
     * 它是以LocalThread为key，变量为value的键值对形式存在的。
     * 该变量对其他线程而言是隔离的，也就是说该变量是当前线程独有的变量。
     * ThreadLocal为变量在每个线程中都创建了一个副本，
     * 那么每个线程可以访问自己内部的副本变量。
     */

    // 创建一个线程本地变量，用于保存用户信息
    private ThreadLocal<User> users = new ThreadLocal<>();
    //set方法，用于保存用户信息
    public void setUser(User user) {
        users.set(user);
    }
    //get方法，用于获取用户信息
    public User getUser(){
        return users.get();
    }
    //clear方法，用于清理用户信息
    public void clear(){
        users.remove();
    }
    //上面这三个方法，都是用于操作线程本地变量的，这样就可以在多线程的情况下，保证每个线程都有自己的变量，不会出现线程安全问题
    //这里的线程安全问题，是指多个线程同时访问同一个变量，会出现数据不一致的问题
    //这里的clear方法，是为了防止内存泄漏，因为ThreadLocal是存放在内存中的，如果不清理，会一直存在内存中，造成内存泄漏
}
