package com.recharge.domain;

import java.util.Date;

/**
 * 具体的购卡记录
 * @author qi.cao
 */
public class BuyCardInfo {

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 商品编号
     */
    private String productId;

    /**
     * 商品名
     */
    private String productName;

    /**
     * 购买时间
     */
    private Date buyTime;

    /**
     * 状态
     */
    private String status;

    /**
     * 供货商编号
     */
    private String supId;

    /**
     * 供货商名称
     */
    private String supName;

    /**
     * 卡信息
     */
    private String cardInfo;

    public static final String KEY_CARD_NO = "cardNo";

    public static final String KEY_CARD_PWD = "cardPwd";

    public static final String KEY_CARD_EXP_TIME = "expTime";

    public static final String STATUS_UNKOWN = "0";

    public static final String STATUS_FAIL = "1";

    public static final String STATUS_SUCCESS = "2";

    public String getSupName() {
        return supName;
    }

    public void setSupName(String supName) {
        this.supName = supName;
    }

    public String getSupId() {
        return supId;
    }

    public void setSupId(String supId) {
        this.supId = supId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    private String phone;

    public String getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(String cardInfo) {
        this.cardInfo = cardInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Date getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(Date buyTime) {
        this.buyTime = buyTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
