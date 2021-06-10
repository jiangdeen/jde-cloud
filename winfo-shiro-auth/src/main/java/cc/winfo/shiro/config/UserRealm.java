package cc.winfo.shiro.config;

import cc.winfo.shiro.entity.SysLogin;
import cc.winfo.shiro.entity.SysPermission;
import cc.winfo.shiro.mapper.SysLoginMapper;
import cc.winfo.shiro.mapper.SysPermissionMapper;
import cc.winfo.shiro.util.ShiroUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Shiro 认证实体
 */
@Component
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private SysLoginMapper sysLoginMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    /**
     * 授权(验证权限时调用)
     * 获取用户权限集合
     */
    @Override
    public AuthorizationInfo doGetAuthorizationInfo
    (PrincipalCollection principals) {
        SysLogin user = (SysLogin) principals.getPrimaryPrincipal();
        if (user == null) {
            throw new UnknownAccountException("账号不存在");
        }
        List<String> permsList;
        // 默认用户拥有最高权限
        List<SysPermission> menuList = sysPermissionMapper.getSysPermission();
        permsList = new ArrayList<>(menuList.size());
        for (SysPermission menu : menuList) {
            permsList.add(menu.getPerms());
        }
        // 用户权限列表
        Set<String> permsSet = new HashSet<>();
        for (String perms : permsList) {
            if (StringUtils.isEmpty(perms)) {
                continue;
            }
            permsSet.addAll(Arrays.asList(perms.trim().split(",")));
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        return info;
    }

    /**
     * 认证(登录时调用)
     * 验证用户登录
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken authToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authToken;
        //查询用户信息
        SysLogin user = sysLoginMapper.getUserByName(token.getUsername());
        //账号不存在
        if (user == null) {
            throw new UnknownAccountException("账号或密码不正确");
        }
        //账号锁定
        if (user.getDisAble().intValue() == 0) {
            throw new LockedAccountException("账号已被锁定,请联系管理员");
        }
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo
                (user, user.getPassword(),
                        ByteSource.Util.bytes(user.getUsername()),
                        getName());
        return info;
    }

    @Override
    public void setCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
        HashedCredentialsMatcher shaCredentialsMatcher = new HashedCredentialsMatcher();
        shaCredentialsMatcher.setHashAlgorithmName(ShiroUtils.hashAlgorithmName);
        shaCredentialsMatcher.setHashIterations(ShiroUtils.hashIterations);
        super.setCredentialsMatcher(shaCredentialsMatcher);
    }




}