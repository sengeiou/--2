package com.recharge.domain;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by qi.cao on 2016/4/21.
 */
public class RechargeOrder<T> {

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 外部订单号
     */
    private String extOrderId;

    /**
     * 用户code
     */
    private String merchantId;

    private String merchantName;

    /**
     * 商品id
     */
    private String productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 充值号码
     */
    private String rechargeNumber;

    /**
     * 销售价
     */
    private BigDecimal salePrice;

    /**
     * 成本价
     */
    private BigDecimal cost;

    /**
     * 支付状态
     */
    private int payState;

    /**
     * 支付失败原因
     */
    private String payFailedReason;

    /**
     * 支付流水ID
     */
    private String payId;

    /**
     * 渠道充值ID
     */
    private String rechargeId;

    /**
     * 充值状态
     */
    private Integer rechargeState;

    /**
     * 失败原因
     */
    private String rechargeFailedReason;

    /**
     * 回调商户url
     */
    private String notifyUrl;

    /**
     * 订单类型
     */
    private int orderType;

    /**
     * 创建时间
     */
    @JSONField(format ="yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 充值结束时间
     */
    @JSONField(format ="yyyy-MM-dd HH:mm:ss")
    private Date rechargeEndTime;

    /**
     * 当前状态超时时间
     */
    private Date timeOutTime;

    /**
     * 商户等级
     */
    private String level;

    /**
     * 供货商ID
     */
    private String supId;

    /**
     * 供货商名称
     */
    private String supName;

    /**
     * 供货商列表
     */
    private String supList;

    /**
     * 充值信息的json
     */
    private String rechargeInfo;

    /**
     * 交易凭证
     */
    private String exchangeTraded;

    /**
     * 充值信息的对象
     */
    private RechargeInfo rechargeInfoObj;

    private String sellId;

    private String sellName;


    public String getSellId() {
        return sellId;
    }

    public void setSellId(String sellId) {
        this.sellId = sellId;
    }

    public String getSellName() {
        return sellName;
    }

    public void setSellName(String sellName) {
        this.sellName = sellName;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getRechargeNumber() {
        return rechargeNumber;
    }

    public void setRechargeNumber(String rechargeNumber) {
        this.rechargeNumber = rechargeNumber;
    }

    public String getExchangeTraded() {
        return exchangeTraded;
    }

    public void setExchangeTraded(String exchangeTraded) {
        this.exchangeTraded = exchangeTraded;
    }

    public String getExtOrderId() {
        return extOrderId;
    }

    public void setExtOrderId(String extOrderId) {
        this.extOrderId = extOrderId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
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

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public int getPayState() {
        return payState;
    }

    public void setPayState(int payState) {
        this.payState = payState;
    }

    public String getPayFailedReason() {
        return payFailedReason;
    }

    public void setPayFailedReason(String payFailedReason) {
        this.payFailedReason = payFailedReason;
    }

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId;
    }

    public String getRechargeId() {
        return rechargeId;
    }

    public void setRechargeId(String rechargeId) {
        this.rechargeId = rechargeId;
    }

    public Integer getRechargeState() {
        return rechargeState;
    }

    public void setRechargeState(Integer rechargeState) {
        this.rechargeState = rechargeState;
    }

    public String getRechargeFailedReason() {
        return rechargeFailedReason;
    }

    public void setRechargeFailedReason(String rechargeFailedReason) {
        this.rechargeFailedReason = rechargeFailedReason;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getRechargeEndTime() {
        return rechargeEndTime;
    }

    public void setRechargeEndTime(Date rechargeEndTime) {
        this.rechargeEndTime = rechargeEndTime;
    }

    public String getSupId() {
        return supId;
    }

    public void setSupId(String supId) {
        this.supId = supId;
    }

    public String getSupName() {
        return supName;
    }

    public void setSupName(String supName) {
        this.supName = supName;
    }

    public String getSupList() {
        return supList;
    }

    public void setSupList(String supList) {
        this.supList = supList;
    }

    public String getRechargeInfo() {
        return rechargeInfo;
    }

    public void setRechargeInfo(String rechargeInfo) {
        this.rechargeInfo = rechargeInfo;
    }

    public Date getTimeOutTime() {
        return timeOutTime;
    }

    public void setTimeOutTime(Date timeOutTime) {
        this.timeOutTime = timeOutTime;
    }

    public RechargeInfo getRechargeInfoObj(Class<RechargeInfo> clazz) {
        if (this.rechargeInfoObj!=null){
            return rechargeInfoObj;
        }
        return JSONObject.parseObject(rechargeInfo,clazz);
    }

    public RechargeInfo getRechargeInfoObj() {
            return rechargeInfoObj;
    }

    public void setRechargeInfoObj(RechargeInfo rechargeInfoObj) {
        this.rechargeInfoObj = rechargeInfoObj;
        this.rechargeInfo = JSONObject.toJSONString(rechargeInfoObj);
    }
}
