package cc.winfo.shiro.mapper;


import cc.winfo.shiro.entity.SysLogin;
import org.apache.ibatis.annotations.Param;

public interface SysLoginMapper {

    int deleteByPrimaryKey(String id);

    int insert(SysLogin record);

    int insertSelective(SysLogin record);

    SysLogin selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysLogin record);

    int updateByPrimaryKey(SysLogin record);

    SysLogin getUserByName(@Param("userName") String userName);


}