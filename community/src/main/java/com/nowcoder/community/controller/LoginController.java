package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;

    //生成验证码
    @Autowired
    private Producer kaptchaProduce;
    //这个Producer是我在配置类中配置的，这里直接用就行了

    @Value("${server.servlet.context-path}")
    private String pathContext;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private RedisTemplate redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @GetMapping("/register")
    public String toRegister() {
        return "/site/register";
    }

    @GetMapping("/toLogin")
    public String toLogin() {
        return "/site/login";
    }

    //注册
    @PostMapping("/register")
    public String register(User user, Model model) {
        Map<String, Object> map = userService.Register(user);
        if (map.isEmpty()) {
            model.addAttribute("msg", "注册成功，已向你发送了一封邮箱，请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "site/register";
        }
    }

    //激活
    @GetMapping("/activation/{userId}/{activationCode}")
    public String activation(@PathVariable("userId") int userId, @PathVariable("activationCode") String activationCode, Model model) {
        int result = userService.activation(userId, activationCode);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功!");
            model.addAttribute("target", "/toLogin");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "你已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，激活码错误！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    //生成验证码
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        String text = kaptchaProduce.createText();
        System.out.println("验证码为：" + text);
        BufferedImage image = kaptchaProduce.createImage(text);
        // 将图片输出给浏览器
        response.setContentType("img/png");

        String kaptchaOwner = CommunityUtil.gennerateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(pathContext);
        response.addCookie(cookie);
        //将验证码加入redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);
        /* session.setAttribute("kaptcha", text);*/
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // ImageIO是javax.imageio.ImageIO类，该类提供了一个静态方法write()来写入图片流，
            // 该方法有三个参数，第一个参数是图片对象，第二个参数是图片格式，第三个参数是输出流。
            ImageIO.write(image, "png", outputStream);

        } catch (IOException e) {
            logger.error("验证码生成错误!" + e.getMessage());
        }

    }

    @PostMapping("/login")
    public String login(/*HttpSession session,*/
                        HttpServletResponse response,
                        Model model,
                        String username,
                        String code,
                        String password,
                        boolean rememberMe,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
      /*  String kaptcha = (String) session.getAttribute("kaptcha");*/
        String kaptcha = null;
        if(!StringUtils.isEmpty(kaptchaOwner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(kaptchaKey);
        }
        //检查验证码
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(kaptcha) || !code.equalsIgnoreCase(kaptcha)) {
            model.addAttribute("codeMsg", "验证码错误！");
            return "/site/login";
        }
        //设置超时时间
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        //调用方法
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        //判断登录凭证
        if (map.containsKey("ticket")) {
            //创建cookie
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(pathContext);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    //退出
    @GetMapping("/logout")
    public String Logout(@CookieValue("ticket") String ticket) {
        System.out.println("ticket=" + ticket);
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/toLogin";
    }


    //忘记密码
    @GetMapping("/forget")
    public String forget() {
        return "/site/forget";
    }

    //获取验证码
    @GetMapping("/forget/code")
    @ResponseBody
    public String getCode(String email, HttpSession session) {
        System.out.println("-----》调用ajax请求获取到email=" + email);
        if (StringUtils.isEmpty(email)) {
            return CommunityUtil.getJSONString(1, "邮箱不能为空！", null);
        }
        //发送邮件
        Context context = new Context();
        context.setVariable("email", email);
        //生成验证码
        String varifyCode = CommunityUtil.gennerateUUID().substring(0, 4);
        context.setVariable("verifyCode", varifyCode);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "找回密码", content);
        System.out.println("验证码为：" + varifyCode);
        //将验证码存入session
        session.setAttribute("varifyCode", varifyCode);
        //返回结果
        return CommunityUtil.getJSONString(0, null, null);
    }

    //重置密码
    @PostMapping("/forget/reset_password")
    public String resetPassword(Model model, HttpSession session, String code, String email, String password) {
        String varifyCode = (String) session.getAttribute("varifyCode");
        if (StringUtils.isEmpty(varifyCode) || StringUtils.isEmpty(code) || !code.equalsIgnoreCase(varifyCode)) {
            model.addAttribute("codeMsg", "验证码错误！");
        }
        Map<String, Object> map = userService.updatePassword(email, password);
        if (map.containsKey("user")) {
            return "redirect:/toLogin";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }

}
