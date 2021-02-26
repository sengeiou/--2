package com.recharge.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by qi.cao on 2016/5/17.
 */
public class ChannelOrder {

    private String id;

    /**
     * 从center 过来的订单号
     */
    private String orderId;

    /**
     * 渠道id
     */
    private String channelId;

    /**
     * 渠道名
     */
    private String channelName;

    /**
     * 渠道订单号(我们平台)
     */
    private String channelOrderId;

    /**
     * 渠道内部订单号（渠道平台）
     */
    private String outChannelOrderId;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品名
     */
    private String productName;

    /**
     * 订单类型
     */
    private Integer orderType;

    /**
     * 状态
     */
    private String state;

    /**
     * 渠道响应码
     */
    private String responseCode;

    /**
     * 渠道响应中文
     */
    private String responseMsg;

    private Date orderTime;

    /**
     * 查询次数
     */
    private Integer queryCount;

    /**
     * 充值账号
     */
    private String rechargeNumber;

    private String channelOrderIdMapping;

    public String getChannelOrderIdMapping() {
        return channelOrderIdMapping;
    }

    public void setChannelOrderIdMapping(String channelOrderIdMapping) {
        this.channelOrderIdMapping = channelOrderIdMapping;
    }

    private BigDecimal cost;

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getRechargeNumber() {
        return rechargeNumber;
    }

    public void setRechargeNumber(String rechargeNumber) {
        this.rechargeNumber = rechargeNumber;
    }

    public Integer getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(Integer queryCount) {
        this.queryCount = queryCount;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public String getOutChannelOrderId() {
        return outChannelOrderId;
    }

    public void setOutChannelOrderId(String outChannelOrderId) {
        this.outChannelOrderId = outChannelOrderId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public String getChannelOrderId() {
        return channelOrderId;
    }

    public void setChannelOrderId(String channelOrderId) {
        this.channelOrderId = channelOrderId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }
}
