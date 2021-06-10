package cc.winfo.shiro.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
@Data
public class SysPermission implements Serializable {
    private String id;

    private String pid;

    private String name;

    private String url;

    private String perms;

    private String type;

    private String icon;

    private BigDecimal orderNum;

    private static final long serialVersionUID = 1L;

}