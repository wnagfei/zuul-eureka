package com.golaxy.cloud.FIlter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AccessTokenFilter extends ZuulFilter {
  @Value("${authorization}")
  private String tokenUrl;

  @Value("${logoutUrl}")
  private String logoutUrl;

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AccessTokenFilter.class);

  @Override
  public Object run() {
    RequestContext ctx = RequestContext.getCurrentContext();
    HttpServletRequest request = ctx.getRequest();
    logger.info(String.format("%s AccessTokenFilter request to %s", request.getMethod(), request.getRequestURL().toString()+"?"+request.getQueryString()));
    String token = request.getParameter("token");
    String key = request.getParameter("key");
    String msgStr = "";
    if (StringUtils.isNotBlank(key)&&"LED".equals(key)){
        ctx.setSendZuulResponse(true); // 对该请求进行路由
        ctx.setResponseStatusCode(200);
        ctx.set("isSuccess", true); // 设值，让下一个Filter看到上一个Filter的状态
        return null;
    }
    if (StringUtils.isNotBlank(token)) {
      logger.info("token的值为：{}", token);
      HttpClient client = new HttpClient();
      HttpMethod getmethod = new GetMethod(tokenUrl + token);
      try {
        client.executeMethod(getmethod);
        msgStr = getmethod.getResponseBodyAsString();
        logger.info("状态码：{}",String.valueOf(getmethod.getStatusCode()));
        if (getmethod.getStatusCode() != 200) {
            logger.info(String.format("%s造成过期的url to %s", request.getMethod(), request.getRequestURL().toString()+"?"+request.getQueryString()));
          ctx.setSendZuulResponse(false);
          ctx.setResponseBody("{\"code\":\"20002\"}"); // 返回错误内容
          ctx.set("isSuccess", false);
          return null;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {
      logger.info("token值为空");
      ctx.setSendZuulResponse(false);
      ctx.setResponseBody("{\"code\":\"20002\"}"); // 返回错误内容
      ctx.set("isSuccess", false);
        return null;
    }
    logger.info("返回的信息为:{}", msgStr);
    logger.info("携带吗:{}？", msgStr.contains("id"));
    if (msgStr.contains("id") == true || key != null) { // 如果请求的参数不为空，则通过
      logger.info("开始路由");
      Gson g = new Gson();
      JsonObject obj = g.fromJson(msgStr, JsonObject.class);
      String username = obj.get("id").toString().replaceAll("\"", "");
      logger.info("username:{}", username);
      ctx.addZuulRequestHeader("username", username);
      ctx.setSendZuulResponse(true); // 对该请求进行路由
      ctx.setResponseStatusCode(200);
      ctx.set("isSuccess", true); // 设值，让下一个Filter看到上一个Filter的状态
        return null;
    } else {
      ctx.setSendZuulResponse(false); // 过滤该请求，不对其进行路由
      ctx.setResponseStatusCode(401); // 返回错误码
      ctx.setResponseBody("{\"code\":\"20002\"}"); // 返回错误内容
      ctx.set("isSuccess", false);
      logger.info("二次判断token过期");
        return null;
    }
  }

  @Override
  public boolean shouldFilter() {
    // 是否执行该过滤器，此处为true，说明需要过滤
    RequestContext ctx = RequestContext.getCurrentContext();
    HttpServletRequest request = ctx.getRequest();
    if (request.getRequestURL().toString().contains("/moodle/")||request.getRequestURL().toString().contains("/login")) {
      return false;
    }
    return true;
  }

  @Override
  public int filterOrder() {
    return 0; // 优先级为0，数字越大，优先级越低
  }

  @Override
  public String filterType() {
    return "route"; // 前置过滤器
  }
}
