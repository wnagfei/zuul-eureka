package com.golaxy.cloud.FIlter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

/**
 * 打印请求参数及统计执行时长过滤器
 * @Version V1.0
 */
@Component
public class LoggingFilter extends ZuulFilter {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    public static final String START_TIME_KEY = "start_time";

    public LoggingFilter() {
        logger.info("Loaded GlobalFilter [Logging]");
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        logger.info("send {} request to {}", request.getMethod(),  request.getRequestURL().toString());
        long startTime = System.currentTimeMillis();
        RequestContext.getCurrentContext().set(START_TIME_KEY, startTime);
        ctx.setSendZuulResponse(true);
        ctx.setResponseStatusCode(200);
        ctx.set("isSuccess", true);
        return null;
    }
}