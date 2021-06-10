package cc.winfo.shiro.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Data
public class SysLogin implements Serializable {
    private String id;

    private String username;

    private String password;

    private BigDecimal disAble;

    private Date updateDate;

    private static final long serialVersionUID = 1L;

}