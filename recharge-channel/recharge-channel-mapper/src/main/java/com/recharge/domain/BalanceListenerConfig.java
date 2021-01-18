package com.recharge.domain;

import java.math.BigDecimal;
import java.util.Date;

public class BalanceListenerConfig {

    /**
     * 渠道id
     */
    private String channelId;

    /**
     * 报警金额
     */
    private BigDecimal warnAmt;

    /**
     * 最新一次告警时间
     */
    private Date warnTime;

    public Date getWarnTime() {
        return warnTime;
    }

    public void setWarnTime(Date warnTime) {
        this.warnTime = warnTime;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public BigDecimal getWarnAmt() {
        return warnAmt;
    }

    public void setWarnAmt(BigDecimal warnAmt) {
        this.warnAmt = warnAmt;
    }
}
