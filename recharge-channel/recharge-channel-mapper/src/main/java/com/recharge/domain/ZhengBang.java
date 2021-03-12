package com.recharge.domain;

/**
 * @author Administrator
 * @create 2021/3/12 9:29
 */
public class ZhengBang {
   private String agentAcct;
   private String orderId;
   private String status;
   private String voucher;
   private String sign;

    public String getAgentAcct() {
        return agentAcct;
    }

    public void setAgentAcct(String agentAcct) {
        this.agentAcct = agentAcct;
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

    public String getVoucher() {
        return voucher;
    }

    public void setVoucher(String voucher) {
        this.voucher = voucher;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
