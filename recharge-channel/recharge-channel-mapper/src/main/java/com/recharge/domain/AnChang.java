package com.recharge.domain;

/**安畅回调接收的参数
 * @author Administrator
 * @create 2020/5/26 9:46
 */
public class AnChang {
    private String userId;
    private String memberId;
    private String upOrderNo;
    private String prodType;
    private String msg;
    private  String orderNo;
    private  String status;
    private  String thirdSeri;
    private String sign;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThirdSeri() {
        return thirdSeri;
    }

    public void setThirdSeri(String thirdSeri) {
        this.thirdSeri = thirdSeri;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getUpOrderNo() {
        return upOrderNo;
    }

    public void setUpOrderNo(String upOrderNo) {
        this.upOrderNo = upOrderNo;
    }

    public String getProdType() {
        return prodType;
    }

    public void setProdType(String prodType) {
        this.prodType = prodType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
