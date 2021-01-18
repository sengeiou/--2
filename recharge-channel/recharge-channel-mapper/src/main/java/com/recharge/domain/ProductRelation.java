package com.recharge.domain;

/**
 * Created by qi.cao on 2017/4/27.
 * 渠道商品的一个映射
 */
public class ProductRelation {

    /**
     * 自己平台的商品名
     */
    private String productName;

    /**
     * 渠道的商品id
     */
    private String channelProductId;

    /**
     * 渠道ID
     */
    private String channelId;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getChannelProductId() {
        return channelProductId;
    }

    public void setChannelProductId(String channelProductId) {
        this.channelProductId = channelProductId;
    }
}
