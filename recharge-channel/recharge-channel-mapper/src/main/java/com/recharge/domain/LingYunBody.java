package com.recharge.domain;

import java.math.BigDecimal;

/**
 * @author Administrator
 * @create 2021/3/8 15:47
 */
public class LingYunBody {
    private String completed_time;
    private String failure_code;
    private String notify_id;
    private String notify_time;
    private String order_id;
    private String order_status;
    private String order_time;
    private String platform_order_id;
    private String product_id;
    private String remark;
    private String settle_amount;
    private String trade_amount;

    public String getSettle_amount() {
        return settle_amount;
    }

    public void setSettle_amount(String settle_amount) {
        this.settle_amount = settle_amount;
    }

    public String getTrade_amount() {
        return trade_amount;
    }

    public void setTrade_amount(String trade_amount) {
        this.trade_amount = trade_amount;
    }

    public String getCompleted_time() {
        return completed_time;
    }

    public void setCompleted_time(String completed_time) {
        this.completed_time = completed_time;
    }

    public String getFailure_code() {
        return failure_code;
    }

    public void setFailure_code(String failure_code) {
        this.failure_code = failure_code;
    }

    public String getNotify_id() {
        return notify_id;
    }

    public void setNotify_id(String notify_id) {
        this.notify_id = notify_id;
    }

    public String getNotify_time() {
        return notify_time;
    }

    public void setNotify_time(String notify_time) {
        this.notify_time = notify_time;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getOrder_time() {
        return order_time;
    }

    public void setOrder_time(String order_time) {
        this.order_time = order_time;
    }

    public String getPlatform_order_id() {
        return platform_order_id;
    }

    public void setPlatform_order_id(String platform_order_id) {
        this.platform_order_id = platform_order_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
