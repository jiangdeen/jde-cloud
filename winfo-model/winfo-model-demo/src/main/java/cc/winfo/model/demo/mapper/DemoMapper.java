package cc.winfo.model.demo.mapper;

import cc.winfo.model.demo.bean.Demo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DemoMapper {

    List<Demo> getDemo(@Param("name") String name);

    int addDemo(Demo demo);

    int updateDemo(Demo demo);

    int delDemo(@Param("id") String id);

}
