package com.recharge.domain.yidian;

/**
 * @author Administrator
 * @create 2021/1/11 19:59
 */
public class YiDianCallBackTelephone {
    private String barcode;
    private String barpwd;
    private String barverify;
    private String shorturl;
    private String redeemcode;
    private String codeid;
    private Integer codetype;
    private String duedate;
    private String issuetime;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBarpwd() {
        return barpwd;
    }

    public void setBarpwd(String barpwd) {
        this.barpwd = barpwd;
    }

    public String getBarverify() {
        return barverify;
    }

    public void setBarverify(String barverify) {
        this.barverify = barverify;
    }

    public String getShorturl() {
        return shorturl;
    }

    public void setShorturl(String shorturl) {
        this.shorturl = shorturl;
    }

    public String getRedeemcode() {
        return redeemcode;
    }

    public void setRedeemcode(String redeemcode) {
        this.redeemcode = redeemcode;
    }

    public String getCodeid() {
        return codeid;
    }

    public void setCodeid(String codeid) {
        this.codeid = codeid;
    }

    public Integer getCodetype() {
        return codetype;
    }

    public void setCodetype(Integer codetype) {
        this.codetype = codetype;
    }

    public String getDuedate() {
        return duedate;
    }

    public void setDuedate(String duedate) {
        this.duedate = duedate;
    }

    public String getIssuetime() {
        return issuetime;
    }

    public void setIssuetime(String issuetime) {
        this.issuetime = issuetime;
    }
}
