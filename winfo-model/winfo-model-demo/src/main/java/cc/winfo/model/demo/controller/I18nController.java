package cc.winfo.model.demo.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "国际化测试")
@RestController
@RequestMapping("/i8n")
public class I18nController {

    @Autowired
    MessageSource messageSource;
    @GetMapping("/whoAmi")
    public String hello() {
        return messageSource.getMessage("who_am_i", null, LocaleContextHolder.getLocale());
    }

}
