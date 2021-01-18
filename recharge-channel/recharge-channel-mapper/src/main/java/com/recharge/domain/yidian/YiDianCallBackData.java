package com.recharge.domain.yidian;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author Administrator
 * @create 2021/1/11 19:55
 */
public class YiDianCallBackData {
    private String clientorderno;
    private String createtime;
    private BigDecimal orderamount;
    private String orderno;
    private String orderremark;
    private Integer orderstatus;
    private ArrayList<YiDianCallBackOrderDetail> orderdetail;

    public BigDecimal getOrderamount() {
        return orderamount;
    }

    public void setOrderamount(BigDecimal orderamount) {
        this.orderamount = orderamount;
    }

    public ArrayList<YiDianCallBackOrderDetail> getOrderdetail() {
        return orderdetail;
    }

    public void setOrderdetail(ArrayList<YiDianCallBackOrderDetail> orderdetail) {
        this.orderdetail = orderdetail;
    }

    public String getClientorderno() {
        return clientorderno;
    }

    public void setClientorderno(String clientorderno) {
        this.clientorderno = clientorderno;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }



    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }

    public String getOrderremark() {
        return orderremark;
    }

    public void setOrderremark(String orderremark) {
        this.orderremark = orderremark;
    }

    public Integer getOrderstatus() {
        return orderstatus;
    }

    public void setOrderstatus(Integer orderstatus) {
        this.orderstatus = orderstatus;
    }
}
