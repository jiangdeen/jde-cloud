package cc.winfo.oauth2.server.config;

import cc.winfo.oauth2.server.service.OAuth2ClientDetailsService;
import cc.winfo.oauth2.server.service.UserAuthDetailsService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import java.util.Map;

/**
 * @Author: winfo-jiangde
 * @Description: oauth 配置核心类
 * @Date: 2021/4/28 15:44
 * @Version: 1.0
 */
@SuppressWarnings("all")
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfigurer extends AuthorizationServerConfigurerAdapter {

    /**
     * redis工厂，默认使用lettue
     */
    @Autowired
    public RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserAuthDetailsService userAuthDetailsService;

    @Autowired
    private OAuth2ClientDetailsService oAuth2ClientDetailsService;




    /**
     * 用来配置令牌端点（Token Endpoint）的安全约束
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()")
                /**
                 * 主要是让/oauth/token支持client_id和client_secret做登陆认证
                 * 如果开启了allowFormAuthenticationForClients，那么就在BasicAuthenticationFilter之前
                 * 添加ClientCredentialsTokenEndpointFilter,使用ClientDetailsUserDetailsService来进行
                 * 登陆认证
                 */
                .allowFormAuthenticationForClients();
        //oauth/token端点过滤器
//                .addTokenEndpointAuthenticationFilter(oAuthTokenAuthenticationFilter);
    }

    /**
     * 用来配置客户端详情服务（ClientDetailsService），
     * 客户端详情信息在这里初始化，
     * 你可以把客户端详情信息写死也可以写入内存或者数据库中
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 使用oatuh2里面提供的 ClientDetailsService初始化配置
        clients.withClientDetails(this.oAuth2ClientDetailsService);
    }

    /**
     * 用来配置授权（authorization）以及令牌（token)的访问端点和令牌服务（token services）
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                //通过authenticationManager开启密码授权
                .authenticationManager(authenticationManager)
                //自定义refresh_token刷新令牌对用户信息的检查，以确保用户信息仍然有效
                .userDetailsService(userAuthDetailsService)
                //token相关服务
                .tokenServices(tokenServices())
                /**
                 pathMapping用来配置端点URL链接，第一个参数是端点URL默认地址，第二个参数是你要替换的URL地址
                 上面的参数都是以“/”开头，框架的URL链接如下：
                 /oauth/authorize：授权端点。----对应的类：AuthorizationEndpoint.java
                 /oauth/token：令牌端点。----对应的类：TokenEndpoint.java
                 /oauth/confirm_access：用户确认授权提交端点。----对应的类：WhitelabelApprovalEndpoint.java
                 /oauth/error：授权服务错误信息端点。
                 /oauth/check_token：用于资源服务访问的令牌解析端点。
                 /oauth/token_key：提供公有密匙的端点，如果你使用JWT令牌的话。
                 */
                .pathMapping("/oauth/confirm_access", "/custom/confirm_access");
                //自定义异常转换处理类
//                .exceptionTranslator(webResponseExceptionTranslator);
    }



    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        //token持久化容器
        tokenServices.setTokenStore(tokenStore());
        //客户端信息
        tokenServices.setClientDetailsService(this.oAuth2ClientDetailsService);
        //自定义token生成
        tokenServices.setTokenEnhancer(tokenEnhancer());
        //access_token 的有效时长 (秒), 默认 12 小时
        tokenServices.setAccessTokenValiditySeconds(60 * 1);
        //refresh_token 的有效时长 (秒), 默认 30 天
        tokenServices.setRefreshTokenValiditySeconds(60 * 2);
        //是否支持refresh_token，默认false
        tokenServices.setSupportRefreshToken(true);
        //是否复用refresh_token,默认为true(如果为false,则每次请求刷新都会删除旧的refresh_token,创建新的refresh_token)
        tokenServices.setReuseRefreshToken(false);
        return tokenServices;
    }

    //令牌增强
    @Bean
    public TokenEnhancer tokenEnhancer() {
        return ((accessToken, authentication) -> {
            if(accessToken instanceof DefaultOAuth2AccessToken){
                DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) accessToken;
//                token.setValue(getToken());

                //使用DefaultExpiringOAuth2RefreshToken类生成refresh_token，自带过期时间，否则不生效，refresh_token一直有效
                DefaultExpiringOAuth2RefreshToken refreshToken = (DefaultExpiringOAuth2RefreshToken)token.getRefreshToken();
                //OAuth2RefreshToken refreshToken = token.getRefreshToken();
                if(refreshToken instanceof DefaultExpiringOAuth2RefreshToken){
//                    token.setRefreshToken(new DefaultExpiringOAuth2RefreshToken(getToken(), refreshToken.getExpiration()));
                }
                Map<String, Object> additionalInformation = Maps.newHashMap();
                additionalInformation.put("client_id", authentication.getOAuth2Request().getClientId());
                //添加额外配置信息
                token.setAdditionalInformation(additionalInformation);
                return token;
            }
            return accessToken;
        });
    }

    @Bean
    public TokenStore tokenStore() {
        //使用redis存储token
        RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
        return redisTokenStore;
    }



}
