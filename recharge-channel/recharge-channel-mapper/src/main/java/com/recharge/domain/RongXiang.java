package com.recharge.domain;

/**
 * @author Administrator
 * @create 2021/1/21 16:16
 */
public class RongXiang {
    private String ChannelID;
    private String User;
    private String BusiType;
    private String Data;
    private String Sign;
    private String Para1;
    private String Para2;

    public String getPara1() {
        return Para1;
    }

    public void setPara1(String para1) {
        Para1 = para1;
    }

    public String getPara2() {
        return Para2;
    }

    public void setPara2(String para2) {
        Para2 = para2;
    }

    public String getChannelID() {
        return ChannelID;
    }

    public void setChannelID(String channelID) {
        ChannelID = channelID;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public String getBusiType() {
        return BusiType;
    }

    public void setBusiType(String busiType) {
        BusiType = busiType;
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
    }

    public String getSign() {
        return Sign;
    }

    public void setSign(String sign) {
        Sign = sign;
    }
}
