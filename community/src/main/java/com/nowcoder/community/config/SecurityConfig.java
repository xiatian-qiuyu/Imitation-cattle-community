package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        //忽略静态资源的拦截
        web.ignoring().antMatchers("/resources/**");
    }

    //授权
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/disscussPost/add",
                        "/comment/add/**",
                        "/message/letter/**",
                        "/message/notice/**",
                        "/like",
                        "/follow"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR,
                        AUTHORITY_USER
                )
                .antMatchers(
                        "/disscussPost/toTop",
                        "/disscussPost/refinement"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/disscussPost/delete",
                        "/data/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                //关闭csrf
                .and().csrf().disable();

        //权限不够时
        http.exceptionHandling()
                //没有登录
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        String xRequestWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestWith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"未登录！"));
                        }else{
                            response.sendRedirect(request.getContextPath()+"/toLogin");
                        }
                    }
                })
                //权限不足
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestWith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限!！"));
                        }else{
                            response.sendRedirect(request.getContextPath()+"/denied");
                        }
                    }
                });
        //覆盖logout的逻辑。
        http.logout().logoutUrl("/xxxLogout");
    }
}
