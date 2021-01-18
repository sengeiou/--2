package com.recharge.domain;

import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;

/**
 * Created by qi.cao on 2016/5/11.
 */
public class ProductSupRelation {

    /**
     * 商品编号
     */
    private String productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 供货商编号
     */
    private String supUid;

    /**
     * 供货商名称
     */
    private String supName;

    /**
     * 权重
     */
    private String weight;

    /**
     * 等级
     */
    private String level;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 成本价
     */
    private BigDecimal cost;

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSupUid() {
        return supUid;
    }

    public void setSupUid(String supUid) {
        this.supUid = supUid;
    }

    public String getSupName() {
        return supName;
    }

    public void setSupName(String supName) {
        this.supName = supName;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
