package com.recharge.mapper;

import com.recharge.domain.BalanceListenerConfig;

import java.util.List;

public interface IBalanceListenerConfigMapper {

    /**
     * 查出所有30分钟之前告警过的渠道配置
     * @return
     */
    public List<BalanceListenerConfig> queryAll();

    int updateTime(String channelId);
}
