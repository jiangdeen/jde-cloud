package cc.winfo.model.demo.mapper;


import cc.winfo.model.demo.bean.GpsInfo;

public interface GpsInfoMapper {

    int deleteByPrimaryKey(String imei);

    int insert(GpsInfo record);

    int insertSelective(GpsInfo record);

    GpsInfo selectByPrimaryKey(String imei);

    int updateByPrimaryKeySelective(GpsInfo record);

    int updateByPrimaryKey(GpsInfo record);
}