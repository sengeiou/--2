package com.recharge.domain;

/**
 * @author user
 * @create 2020/10/13 10:21
 */
public class JiaNuo {
    private String UserId;
    private String BizType;
    private String OrderNo;
    private String AccountVal;
    private String OrderStatus;
    private String ProductData;
    private String Time;
    private String Sign;

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getBizType() {
        return BizType;
    }

    public void setBizType(String bizType) {
        BizType = bizType;
    }

    public String getOrderNo() {
        return OrderNo;
    }

    public void setOrderNo(String orderNo) {
        OrderNo = orderNo;
    }

    public String getAccountVal() {
        return AccountVal;
    }

    public void setAccountVal(String accountVal) {
        AccountVal = accountVal;
    }

    public String getOrderStatus() {
        return OrderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        OrderStatus = orderStatus;
    }

    public String getProductData() {
        return ProductData;
    }

    public void setProductData(String productData) {
        ProductData = productData;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getSign() {
        return Sign;
    }

    public void setSign(String sign) {
        Sign = sign;
    }
}
