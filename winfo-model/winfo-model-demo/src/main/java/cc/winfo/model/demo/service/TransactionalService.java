package cc.winfo.model.demo.service;

import cc.winfo.model.demo.bean.Demo;
import cc.winfo.model.demo.bean.GpsInfo;
import cc.winfo.model.demo.mapper.DemoMapper;
import cc.winfo.model.demo.mapper.GpsInfoMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
@DS("test1")
@Service
public class TransactionalService {

    @Autowired
    private DemoMapper demoMapper;

    @Autowired
    private GpsInfoMapper gpsInfoMapper;

    @Transactional(isolation = Isolation.DEFAULT)
    public String addDemo(Demo demo) {

        demoMapper.addDemo(demo);
        GpsInfo gpsInfo = new GpsInfo();
        gpsInfo.setImei("19950816");
        gpsInfo.setContent("这个是内容!");
        gpsInfo.setAddDate(new Date());
        gpsInfoMapper.insertSelective(gpsInfo);
        return "我想去前线";
    }


}
