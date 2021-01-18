package com.recharge.domain;

/**
 * Created by qi.cao on 2016/5/17.
 * 渠道类
 */
public class Channel {

    /**
     * 供货商ID
     */
    private String channelId;

    /**
     * 供货商名称
     */
    private String channelName;

    /**
     * 取单数
     */
    private int getNum;

    private int state;

    /**
     * 配置信息
     */
    private String configInfo;

    /**
     * 分组
     */
    private String groupId;
    /**
     * 拓展字段1
     */
    private String remark;
    /**
     * 拓展字段1
     */
    private String remark2;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark2() {
        return remark2;
    }

    public void setRemark2(String remark2) {
        this.remark2 = remark2;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

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

    public int getGetNum() {
        return getNum;
    }

    public void setGetNum(int getNum) {
        this.getNum = getNum;
    }

    public String getConfigInfo() {
        return configInfo;
    }

    public void setConfigInfo(String configInfo) {
        this.configInfo = configInfo;
    }
}
