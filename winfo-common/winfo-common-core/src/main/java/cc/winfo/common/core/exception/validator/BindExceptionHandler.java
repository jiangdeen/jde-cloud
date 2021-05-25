package cc.winfo.common.core.exception.validator;

import cc.winfo.common.core.api.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: winfo-jiangde
 * @Description: 参数校验异常处理器
 * @Date: 2021/4/14 11:07
 * @Version: 1.0
 */

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // 当使用多个切面拦截时,别忘了顺序
public class BindExceptionHandler {


    /**
     * 参数校验异常
     *
     * @return
     * @throws Exception
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R MethodArgumentNotValidHandler(MethodArgumentNotValidException method) {
        BindingResult bindingResult = method.getBindingResult();
        log.error("参数校验异常:" + method.getMessage());
        return paramValid(bindingResult);
    }

    /**
     * 参数校验异常
     *
     * @return
     * @throws Exception
     */
    @ExceptionHandler(value = BindException.class)
    public R MethodArgumentNotValidHandler(BindException bind) {
        BindingResult bindingResult = bind.getBindingResult();
        log.error("参数校验异常:" + bind.getMessage());
        return paramValid(bindingResult);
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public R MissingServletRequestParameterException(MissingServletRequestParameterException bind) {
        log.error("参数校验异常:" + bind.getMessage());
        return R.fail(bind.getMessage());
    }

    private R paramValid(BindingResult bindingResult) {
        return R.fail(bindingResult.getAllErrors().stream().map(it -> {
                    FieldError is = (FieldError) it;
                    Map<String, String> map = new HashMap<>();
                    map.put("field", is.getField());
                    map.put("defaultMessage", is.getDefaultMessage());
                    return map;
                }).collect(Collectors.toList()),
                "参数校验异常"
        );
    }
}
