package com.recharge.mapper;

import com.recharge.domain.Maintenance;
import org.apache.ibatis.annotations.Param;

public interface IMaintenanceMapper {

    Maintenance selectById(@Param("channelId") String channelId);
}
