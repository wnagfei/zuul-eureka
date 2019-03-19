package com.golaxy.cloud.FIlter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class XForwardedForFilter extends ZuulFilter {
    private static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
    private static final Logger logger = LoggerFactory.getLogger(XForwardedForFilter.class);

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String remoteAddr = request.getRemoteAddr();
        logger.info("用户的ip为remoteAddr：{}",remoteAddr);
       // String ip = request.getHeader(HTTP_X_FORWARDED_FOR);
        ctx.getZuulRequestHeaders().put(HTTP_X_FORWARDED_FOR, remoteAddr);
        return null;
    }


    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }
}