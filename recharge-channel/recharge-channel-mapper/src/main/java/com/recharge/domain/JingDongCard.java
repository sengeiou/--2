package com.recharge.domain;

/**
 * @author Administrator
 * @create 2020/12/17 9:44
 */
public class JingDongCard {
    private String id;
    private Long amount;
    private String pwdKey;
    private Long actived;
    private Boolean neverExpires;

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getActived() {
        return actived;
    }

    public void setActived(Long actived) {
        this.actived = actived;
    }

    public Boolean getNeverExpires() {
        return neverExpires;
    }

    public void setNeverExpires(Boolean neverExpires) {
        this.neverExpires = neverExpires;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getPwdKey() {
        return pwdKey;
    }

    public void setPwdKey(String pwdKey) {
        this.pwdKey = pwdKey;
    }

}
