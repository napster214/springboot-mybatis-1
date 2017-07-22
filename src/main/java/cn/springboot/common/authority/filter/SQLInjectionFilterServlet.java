package cn.springboot.common.authority.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * @Description 防止SQL注入
 * @author 王鑫 
 * @date Mar 24, 2017 7:42:29 PM  
 */
//@WebFilter(urlPatterns = "/*", filterName = "SQLInjection", initParams = { @WebInitParam(name = "regularExpression", value = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|" + "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)") })
@SuppressWarnings("ALL")
public class SQLInjectionFilterServlet implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SQLInjectionFilterServlet.class);

    private String regularExpression;

    public final static String XSS_ERROR_PATH = "/WEB-INF/views/common/error.jsp";

    public SQLInjectionFilterServlet() {

    }

    public void init(FilterConfig filterConfig) throws ServletException {
        regularExpression = filterConfig.getInitParameter("regularExpression");
        log.info("######### regularExpression={}", regularExpression);
    }

    /*
     * 如果需要输入“'”、“;”、“--”这些字符 可以考虑将这些字符转义为html转义字符 “'”转义字符为：&#39; “;”转义字符为：&#59;
     * “--”转义字符为：&#45;&#45;
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String requestUrl = req.getRequestURL().toString();
        String contextPath = req.getContextPath();
        requestUrl = requestUrl.substring(requestUrl.indexOf(contextPath) + contextPath.length());// 获取剥离contextPath的url路径

        Map parametersMap = request.getParameterMap();
        Iterator it = parametersMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String[] value = (String[]) entry.getValue();
            for (int i = 0; i < value.length; i++) {
                if (null != value[i] && value[i].matches(regularExpression)) {
                    log.info("#疑似SQL注入攻击！参数名称：{}；录入信息:{}", entry.getKey(), value[i]);
                    RequestDispatcher rd = request.getRequestDispatcher(XSS_ERROR_PATH);
                    rd.forward(request, response);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    public void destroy() {

    }

}