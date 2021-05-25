package cc.winfo.oauth2.server.service;
import cc.winfo.oauth2.server.constant.GrantTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @Description: 自定义client详细信息类
 * @ProjectName: spring-parent
 * @Version: 1.0
 */
@Service
public class OAuth2ClientDetailsService implements ClientDetailsService {

    private ClientDetailsService clientDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * 被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器调用一次，类似于Serclet的inti()方法。
     * 被@PostConstruct修饰的方法会在构造函数之后，init()方法之前运行。
     */
    @PostConstruct
    public void init() {
        InMemoryClientDetailsServiceBuilder inMemoryClientDetailsServiceBuilder = new InMemoryClientDetailsServiceBuilder();
        inMemoryClientDetailsServiceBuilder
                // 授权码模式
                .withClient("auth_code")
                .secret(passwordEncoder.encode("secret"))
                .authorizedGrantTypes(GrantTypeEnum.getGrantTypes())
                .redirectUris("http://localhost:9003/auth_user/get_auth_code")
                .scopes("insert", "update", "del", "select", "replace")
                .and()

                // 密码模式
                .withClient("winfo-cloud")
                .resourceIds("winfo")
                .authorizedGrantTypes(GrantTypeEnum.PASSWORD.getGrant_type(), GrantTypeEnum.REFRESH_TOKEN.getGrant_type())
                .secret(passwordEncoder.encode("secret"))
                .scopes("all")
                .and()

                // 客户端模式
                .withClient("client")
                .secret(passwordEncoder.encode("secret"))
                .authorizedGrantTypes("client_credentials", "refresh_token")
                .scopes("insert", "del", "update");

                // 简单模式
//                .and()
//                .withClient("client_implicit")
//                .authorizedGrantTypes("implicit")
//                .redirectUris("http://localhost:9003/auth_user/get_auth_code")
//                .scopes("del", "update");
        try {
            clientDetailsService = inMemoryClientDetailsServiceBuilder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        if (clientId == null) {
            throw new ClientRegistrationException("客户端不存在");
        }
        return clientDetailsService.loadClientByClientId(clientId);
    }
}
