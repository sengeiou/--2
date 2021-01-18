package com.recharge.domain.yidian;

import java.util.ArrayList;

/**
 * @author Administrator
 * @create 2021/1/11 19:57
 */
public class YiDianCallBackOrderDetail {
    private String actid;
    private Float saleprice;
    private String skuname;
    private Object codedetail;

    public Object getCodedetail() {
        return codedetail;
    }

    public void setCodedetail(Object codedetail) {
        this.codedetail = codedetail;
    }

    public String getActid() {
        return actid;
    }

    public void setActid(String actid) {
        this.actid = actid;
    }

    public Float getSaleprice() {
        return saleprice;
    }

    public void setSaleprice(Float saleprice) {
        this.saleprice = saleprice;
    }

    public String getSkuname() {
        return skuname;
    }

    public void setSkuname(String skuname) {
        this.skuname = skuname;
    }


}
