package cc.winfo.auth2.server.config;

import cc.winfo.auth2.server.handler.UserAccessDeniedHandler;
import cc.winfo.auth2.server.handler.UserAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * @Description: @EnableResourceServer注解实际上相当于加上OAuth2AuthenticationProcessingFilter过滤器，优先级顺序order=3-order的值越小，类的优先级越高
 * @ProjectName: spring-parent
 * @Package: com.yaomy.security.oauth2.config.ResServerConfig
 * @Date: 2019/7/9 13:28
 * @Version: 1.0
 */
//@Configuration
//@EnableResourceServer
public class ResourceServerConfigurer extends ResourceServerConfigurerAdapter {

    @Autowired
    private UserAuthenticationEntryPoint userAuthenticationEntryPoint;
    @Autowired
    private UserAccessDeniedHandler userAccessDeniedHandler;

    @Autowired
    private TokenStore tokenStore;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
       /* resources
                .tokenServices(tokenServices())
                //资源ID
                .resourceId(propertyService.getProperty("spring.security.oauth.resource.id"))
                //用来解决匿名用户访问无权限资源时的异常
                .authenticationEntryPoint(userAuthenticationEntryPoint)
                //访问资源权限相关异常处理
                .accessDeniedHandler(userAccessDeniedHandler);*/
       super.configure(resources);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
       super.configure(http);
    }

    /**
     * @Description 令牌服务
     * @Date 2019/7/15 18:07
     * @Version  1.0
     */
    @Bean
    public DefaultTokenServices tokenServices(){
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore);
        return defaultTokenServices;
    }
}
