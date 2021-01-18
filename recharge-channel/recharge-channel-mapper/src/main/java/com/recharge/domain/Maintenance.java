package com.recharge.domain;

import java.util.Date;

public class Maintenance {

    public static final Integer STATUS_ON = 1;

    public static final Integer STATUS_OFF = 0;

    /**
     * 维护的渠道ID
     */
    private String channelId;

    /**
     * 维护渠道名
     */
    private String channelName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 生效时间
     */
    private Date effectTime;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getEffectTime() {
        return effectTime;
    }

    public void setEffectTime(Date effectTime) {
        this.effectTime = effectTime;
    }
}
