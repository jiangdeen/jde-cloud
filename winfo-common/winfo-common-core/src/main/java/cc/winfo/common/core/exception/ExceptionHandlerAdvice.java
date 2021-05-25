package cc.winfo.common.core.exception;

import cc.winfo.common.core.api.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @description: 异常切面类 <br>
 * @date: 2019/8/29 10:08 <br>
 * @author: winfo-jiangde <br>
 * @version: 1.0 <br>
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    /**
     * 捕获处理全局异常
     *
     * @param ex
     * @return ResultVO 返回自定义类型
     */
    @ExceptionHandler(value = Exception.class)
    public R globalException(Exception ex) {
        log.error("globalException:" + ex.getMessage(), ex);
        return R.fail(ex.getMessage());
    }


    /**
     * 捕获 BaseException
     *
     * @param ex
     * @return ResultVO 包装返回类
     */
    @ExceptionHandler(value = BaseException.class)
    public R applicationRuntimeException(BaseException ex) {
        log.error("CoreRuntimeException:" + ex.getMessage(), ex);
        return R.fail(ex.getMessage());
    }


}
