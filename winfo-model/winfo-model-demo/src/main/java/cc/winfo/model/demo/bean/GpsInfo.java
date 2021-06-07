package cc.winfo.model.demo.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class GpsInfo implements Serializable {
    private String imei;

    private String content;

    private Date addDate;

    private static final long serialVersionUID = 1L;

}