package com.recharge.domain;

/**
 * @author Administrator
 * @create 2020/12/3 10:08
 */
public class NaiXueCode {
    private String code;

    private String exchangeStatus;

    private String validStart;

    private String validEnds;

    public String getExchangeStatus() {
        return exchangeStatus;
    }

    public void setExchangeStatus(String exchangeStatus) {
        this.exchangeStatus = exchangeStatus;
    }

    public String getValidStart() {
        return validStart;
    }

    public void setValidStart(String validStart) {
        this.validStart = validStart;
    }

    public String getValidEnds() {
        return validEnds;
    }

    public void setValidEnds(String validEnds) {
        this.validEnds = validEnds;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
