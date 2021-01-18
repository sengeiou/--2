package com.recharge.domain;

import com.alibaba.fastjson.annotation.JSONField;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 平台卡信息存储
 * 这个跟workorder 中的提卡不是一回事，那个是界面导入，界面导出
 * 这个是导入到库里面后api 提卡
 */
public class PlatformCardInfo {
    public static final String STATUS_UN_SALE = "0";

    public static final String STATUS_SALE = "1";
    private Integer id;

    private String productId;

    private String productName;

    private Date buyTime;

    private String supId;

    private String supName;

    private String cardNo;

    private String cardPwd;

    private String status;

    private String orderId;

    private Integer count;

    private  String customerId;
    public static final String PUBLIC_STOCK="0";

    private Integer isSelectedLock;

    private Integer encVersion;

    private BigDecimal cost;

    public Integer getIsSelectedLock() {
        return isSelectedLock;
    }

    public void setIsSelectedLock(Integer isSelectedLock) {
        this.isSelectedLock = isSelectedLock;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Integer getEncVersion() {
        return encVersion;
    }

    public void setEncVersion(Integer encVersion) {
        this.encVersion = encVersion;
    }

    /**
     * 过期时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(Date buyTime) {
        this.buyTime = buyTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getCardPwd() {
        return cardPwd;
    }

    public void setCardPwd(String cardPwd) {
        this.cardPwd = cardPwd;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }
}
