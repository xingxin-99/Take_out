package com.star.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.star.reggie.common.BaseContext;
import com.star.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter
@Slf4j
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long ids = Thread.currentThread().getId();
        log.info("线程id:{}",ids);

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response =(HttpServletResponse) servletResponse;
//      放行的urls
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"

        };

        boolean check = matchUrl(urls,request.getRequestURI());
        if(check){
            filterChain.doFilter(request,response);
            return;
        }
        if(request.getSession().getAttribute("employee")!=null){
            Long id =  (Long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(id);
            filterChain.doFilter(request,response);
            return;
        }
        if(request.getSession().getAttribute("user")!=null){
            Long id =  (Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(id);
            filterChain.doFilter(request,response);
            return;
        }
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }
    public boolean matchUrl(String[] urls,String reqUrl){
        for(String url : urls){
            if(PATH_MATCHER.match(url,reqUrl))
                return true;
        }
        return false;
    }
}
