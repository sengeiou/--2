package com.recharge.domain;

/**
 * @author Administrator
 * @create 2021/3/8 15:45
 */
public class LingYun {
    private LingYunBody body;
    private String merchant_id;
    private String sign;
    private String time;

    public LingYunBody getBody() {
        return body;
    }

    public void setBody(LingYunBody body) {
        this.body = body;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public String getMerchant_id() {
        return merchant_id;
    }

    public void setMerchant_id(String merchant_id) {
        this.merchant_id = merchant_id;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

}
