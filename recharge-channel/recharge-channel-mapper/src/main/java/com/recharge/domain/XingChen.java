package com.recharge.domain;

/**
 * @author user
 * @create 2020/8/21 10:42
 */
public class XingChen {
    private String main_order_id;
    private String order_state;
    private String order_memo ;
    private String stock_merchant_id;
    private String out_order_id;
    private String orderInfo;


    public String getMain_order_id() {
        return main_order_id;
    }

    public void setMain_order_id(String main_order_id) {
        this.main_order_id = main_order_id;
    }

    public String getOrder_state() {
        return order_state;
    }

    public void setOrder_state(String order_state) {
        this.order_state = order_state;
    }

    public String getOrder_memo() {
        return order_memo;
    }

    public void setOrder_memo(String order_memo) {
        this.order_memo = order_memo;
    }

    public String getStock_merchant_id() {
        return stock_merchant_id;
    }

    public void setStock_merchant_id(String stock_merchant_id) {
        this.stock_merchant_id = stock_merchant_id;
    }

    public String getOut_order_id() {
        return out_order_id;
    }

    public void setOut_order_id(String out_order_id) {
        this.out_order_id = out_order_id;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
}
