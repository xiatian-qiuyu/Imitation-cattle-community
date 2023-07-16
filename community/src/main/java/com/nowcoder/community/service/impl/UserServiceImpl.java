package com.nowcoder.community.service.impl;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.mapper.LoginTicketMapper;
import com.nowcoder.community.mapper.UserMapper;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService, CommunityConstant {

/*    @Autowired
    private LoginTicketMapper loginTicketMapper;*/
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context")
    private String contextPath;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public User findUserById(int id) {
//        return userMapper.selectUserById(id);
        User user = getCache(id);
        if(user==null){
            user = initCache(id);
        }
        return user;
    }

    @Override
    public User findUserByName(String username) {
        return userMapper.selectUserByName(username);
    }

    @Override
    public User findByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    /*注册*/
    @Override
    public Map<String, Object> Register(User user) {
        HashMap<String, Object> map = new HashMap<>();
        if(StringUtils.isEmpty(user)){
            //IllegalArgumentException是一个运行时异常，当方法的参数不合法时，抛出该异常。
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(StringUtils.isEmpty(user.getUsername())){
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if(StringUtils.isEmpty(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isEmpty(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        //
        User userByName = userMapper.selectUserByName(user.getUsername());
        if(userByName!=null){
            map.put("usernameMsg","账号已存在");
            return map;
        }
        User userByEmail = userMapper.selectByEmail(user.getEmail());
        if(userByEmail!=null){
            map.put("emailMsg","邮箱已注册");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.gennerateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setStatus(0);
        user.setType(0);
        //使用工具类生成随机激活码
        user.setActivationCode(CommunityUtil.gennerateUUID());
        //生成随机头像路径(这里是牛客网自带的，如http://images.nowcoder.com/head/%1t.png)
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.inseretUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        // http://localhost:8083/community/activation/101/code
        String url =domain+"/"+contextPath+"/"+"activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活邮箱账号",content);

        return map;
    }

    /*激活账号*/
    public int activation(int id,String activationCode){
        User user = userMapper.selectUserById(id);
        if(user.getActivationCode().equals(activationCode)){
            userMapper.updateStatus(id,1);
            clearCache(id);
            return ACTIVATION_SUCCESS;
        }
        else if (user.getStatus()==1) {
            return ACTIVATION_REPEAT;
        }else {
            return  ACTIVATION_FAILURE;
        }
    }


    public Map<String,Object> login(String username,String passsword,int expiredSeconds){
        HashMap<String, Object> map = new HashMap<>();
        //空值处理
        if(StringUtils.isEmpty(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isEmpty(passsword)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        User user = userMapper.selectUserByName(username);
        if(user==null){
            map.put("passwordMsg","账号不存在！");
            return map;
        }
        // 检查用户状态
        if(user.getStatus()==0){
            map.put("usernameMsg","账号未激活");
            return map;
        }
        //验证密码
        String password = CommunityUtil.md5(passsword+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码错误！");
            return map;
        }
        //生成凭证
        LoginTicket loginTicket = new LoginTicket();
        String ticket  =CommunityUtil.gennerateUUID();
        loginTicket.setTicket(ticket);
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);//0表示有效，1表示无效
        //System.currentTimeMillis()返回当前时间的毫秒数
        //expiredSeconds为什么要乘以1000，因为System.currentTimeMillis()返回的是毫秒数，而expiredSeconds是秒数
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds* 1000L));

        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
        /*loginTicketMapper.insertLoginTicket(loginTicket);*/
        map.put("ticket",ticket);
        return map;
    }

    //退出，修改登录凭证
    @Override
    public void logout(String ticket) {
        /*loginTicketMapper.updateStatus(ticket,1);*/
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket  = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
    }

    @Override
    public int updateHeader(int id, String headerUrl) {
//        return userMapper.updateHeader(id,headerUrl);
        int rows = userMapper.updateHeader(id, headerUrl);
        clearCache(id);
        return rows;
    }

    @Override
    public Map<String, Object> updatePassword(String email,String password) {
        HashMap<String, Object> map = new HashMap<>();
        if(StringUtils.isEmpty(email)){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        if(StringUtils.isEmpty(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        //验证邮箱
        User user = userMapper.selectByEmail(email);
        if(user==null){
            map.put("emailMsg", "该邮箱尚未注册!");
            return map;
        }
        userMapper.updatePassword(user.getId(),CommunityUtil.md5(password+user.getSalt()));
        clearCache(user.getId());
        map.put("user",user);
        return map;
    }

    @Override
    public LoginTicket findLoginTicket(String ticket) {
        /*return userMapper.selectLoginTicket(ticket);*/
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return loginTicket;
    }


    /**
     * 修改密码
     * @param id
     * @param newPassword
     * @return
     */
    @Override
    public void updatePassword(int id, String newPassword) {
        userMapper.updatePassword(id, newPassword);
        clearCache(id);
    }


    //1.优先从缓存中取值
    private User getCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }
    //2.取不到时初始化缓存数据,即从数据库中取值然后存入缓存
    private User initCache(int userId){
        User user = userMapper.selectUserById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user,3600, TimeUnit.SECONDS);
        return user;
    }
    //3.数据变更时清除缓存数据
    private void clearCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    /**
     *  根据用户id查询权限集合
     * @param userId
     * @return
     */


    //为什么我的项目一启动就会调用这个方法
    //因为在SecurityConfig中配置了这个方法，所以一启动就会调用这个方法
    @Override
    public Collection<?extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);
        List<GrantedAuthority> list= new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
