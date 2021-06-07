package cc.winfo.common.core.api;

import cc.winfo.common.core.api.enums.AppHttpStatus;
import lombok.Data;

import java.io.Serializable;


/**
 * 响应信息主体
 *
 * @author winfo-jiangde
 */
@Data
public class R<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code;

    private String msg;

    private T data;

    public static <T> R<T> ok() {
        return restResult(null, AppHttpStatus.OK);
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, AppHttpStatus.OK);
    }

    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, 0, msg);
    }

    public static <T> R<T> fail() {
        return restResult(null, AppHttpStatus.EXCEPTION);
    }

    public static <T> R<T> fail(String msg) {
        return restResult(null, -1, msg);
    }

    public static <T> R<T> fail(T data) {
        return restResult(data,  AppHttpStatus.EXCEPTION);
    }

    public static <T> R<T> fail(T data, String msg) {
        return restResult(data, -1, msg);
    }

    public static <T> R<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

    private static <T> R<T> restResult(T data, AppHttpStatus status) {
        R<T> apiResult = new R<>();
        apiResult.setCode(status.getStatus());
        apiResult.setData(data);
        apiResult.setMsg(status.getMessage());
        return apiResult;
    }


}
