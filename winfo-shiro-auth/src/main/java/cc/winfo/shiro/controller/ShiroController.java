package cc.winfo.shiro.controller;

import cc.winfo.shiro.mapper.SysPermissionMapper;
import cc.winfo.shiro.util.ShiroUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * Shrio 测试方法控制层
 */
@RestController
@RequestMapping("/login")
public class ShiroController {
    private static Logger LOGGER = LoggerFactory.getLogger(ShiroController.class);

    @Resource
    private SysPermissionMapper sysPermissionMapper;

    /**
     * 登录测试
     * http://localhost:7011/userLogin?userName=admin&passWord=admin
     */
    @RequestMapping("/userLogin")
    public String userLogin(
            @RequestParam(value = "userName") String userName,
            @RequestParam(value = "passWord") String passWord) {
        try {
            Subject subject = ShiroUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(userName, passWord);
            subject.login(token);
            LOGGER.info("登录成功");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "登录成功";
    }

    /**
     * 服务器每次重启请求该接口之前必须先请求上面登录接口
     * http://localhost:7011/menu/list 获取所有菜单列表
     * 权限要求：sys:user:shiro
     */
    @RequestMapping("/menu/list")
    @RequiresPermissions("sys:user:shiro")
    public List list() {
        return sysPermissionMapper.getSysPermission();
    }

    /**
     * 用户没有该权限，无法访问
     * 权限要求：ccc:ddd:bbb
     */
    @RequestMapping("/menu/list2")
    @RequiresPermissions("ccc:ddd:bbb")
    public List list2() {
        return sysPermissionMapper.getSysPermission();
    }

    /**
     * 退出测试
     */
    @RequestMapping("/userLogOut")
    public String logout() {
        ShiroUtils.logout();
        return "success";
    }
}