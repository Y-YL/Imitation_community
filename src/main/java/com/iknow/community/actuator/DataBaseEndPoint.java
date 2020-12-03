package com.iknow.community.actuator;

import com.iknow.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Spring actuator
 * 自定义端点
 */
@Component
@Endpoint(id = "database")
public class DataBaseEndPoint {

    private static final Logger logger = LoggerFactory.getLogger(DataBaseEndPoint.class);

    @Autowired
    private DataSource dataSource;

    @ReadOperation
    public String checkConnection(){
        try {
            Connection con = dataSource.getConnection();
            return CommunityUtil.getJSONString(0,"获取连接成功!");
        } catch (SQLException e) {
            logger.error("获取连接失败:"+e.getMessage());
            return CommunityUtil.getJSONString(1,"获取连接失败!");
        }
    }


}
