package com.recharge.domain;

public class BuyCardRechargeInfo extends RechargeInfo{

    private String actId;

    private String buyNumber;

    private String phone;

    /**
     * 民生直充需要提供额外的队列编号**/
    private String queueId;

    /**
     * 民生直充需要提供额外的商品编号**/
    private String productSn;

    /**用于存储民生卡密信息*/
    private BuyCardInfo buyCardInfo;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getActId() {
        return actId;
    }

    public void setActId(String actId) {
        this.actId = actId;
    }

    public String getBuyNumber() {
        return buyNumber;
    }

    public void setBuyNumber(String buyNumber) {
        this.buyNumber = buyNumber;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getProductSn() {
        return productSn;
    }

    public void setProductSn(String productSn) {
        this.productSn = productSn;
    }

    public BuyCardInfo getBuyCardInfo() {
        return buyCardInfo;
    }

    public void setBuyCardInfo(BuyCardInfo buyCardInfo) {
        this.buyCardInfo = buyCardInfo;
    }
}
