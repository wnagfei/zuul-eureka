package com.golaxy.cloud;

import com.golaxy.cloud.FIlter.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;

@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@RestController
public class GatewayServerApplication
{
    @Value("${authorization}")
    private String tokenUrl;
    @Bean
    public AccessTokenFilter accessTokenFilter(){
        return new AccessTokenFilter();
    }
    @Bean
    public LoggingFilter loggingFilter(){ return new LoggingFilter(); }
    @Bean
	public TimeCostPostFilter timeCostPostFilter(){return new TimeCostPostFilter();}
    @Bean
    public MyFallbackProvider myFallbackProvider(){return new MyFallbackProvider();}
    @Bean
	public XForwardedForFilter xForwardedForFilter(){return new XForwardedForFilter();}
	@Autowired
    private HttpServletRequest request;
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(GatewayServerApplication.class);
	/**
	 * 文件上传配置
	 * @return
	 */
	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		//  单个数据大小
		factory.setMaxFileSize("300MB"); // KB,MB
		/// 总上传数据大小
		factory.setMaxRequestSize("1000MB");
		return factory.createMultipartConfig();
	}
	@GetMapping("/hello")
    public String hello (){
//        Request ctx = RequestContext.getCurrentContext();
//        HttpServletRequest request = ctx.getRequest();
        String remoteAddr = request.getRemoteAddr();
        logger.info("用户的ip为remoteAddr：{}",remoteAddr);
         String ip = request.getHeader("X_FORWARDED_FOR");
//        ctx.getZuulRequestHeaders().put("HTTP_X_FORWARDED_FOR", remoteAddr);
        return ip;
    }

	public static void main(String[] args) {
		SpringApplication.run(GatewayServerApplication.class, args);
	}
	/**
	 *      解决跨域问题
	 * @return
	 */
	@Bean
	public CorsFilter corsFilter() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		// 允许cookies跨域
		config.setAllowCredentials(true);
		// #允许向该服务器提交请求的URI，*表示全部允许
		config.addAllowedOrigin("*");
		// #允许访问的头信息,*表示全部
		config.addAllowedHeader("*");
		// 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
		config.setMaxAge(18000L);
		// 允许提交请求的方法，*表示全部允许，也可以单独设置GET、PUT等
		config.addAllowedMethod("*");
		config.addAllowedMethod("HEAD");
		// 允许Get的请求方法
		config.addAllowedMethod("GET");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("DELETE");
		config.addAllowedMethod("PATCH");
		source.registerCorsConfiguration("/**", config);
		FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
		bean.setOrder(0);
		return new CorsFilter(source);
	}

}
