package com.recharge.bean;

/**
 * Created by qi.cao on 2016/5/20.
 */
public class ResponseOrder {

    /**
     * 内部订单号 center
     */
    private String orderId;

    /**
     * channel 的 渠道订单号
     */
    private String channelOrderId;

    /**
     * 渠道内部的订单号
     */
    private String outChannelOrderId;

    /**
     * 渠道响应码
     */
    private String responseCode;

    /**
     * 响应的中文
     */
    private String responseMsg;

    public String getOutChannelOrderId() {
        return outChannelOrderId;
    }

    public void setOutChannelOrderId(String outChannelOrderId) {
        this.outChannelOrderId = outChannelOrderId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getChannelOrderId() {
        return channelOrderId;
    }

    public void setChannelOrderId(String channelOrderId) {
        this.channelOrderId = channelOrderId;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }
}
