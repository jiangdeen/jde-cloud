package cc.winfo.model.demo.task;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class Mytask {

    @Scheduled(fixedDelay = 5000)
    public void run(){
        System.out.println("=======================1");
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void run1(){
        System.out.println("========================2");
    }

}
