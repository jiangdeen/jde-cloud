package cc.winfo.common.log.annotation;

import cc.winfo.common.log.entity.BusinessType;
import cc.winfo.common.log.entity.OperatorType;

import java.lang.annotation.*;

/**
 * @Author: winfo-jiangde
 * @Description:
 * @Date: 2021/4/26 10:09
 * @Version: 1.0
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
 public @interface Log {
    /**
     * 模块
     */
     String title() default "";

    /**
     * 功能
     */
     BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作人类别
     */
     OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否保存请求的参数
     */
     boolean isSaveRequestData() default true;

}
