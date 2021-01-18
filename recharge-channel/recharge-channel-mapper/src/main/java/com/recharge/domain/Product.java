package com.recharge.domain;

/**
 * 商品信息
 */
public class Product {

    /**
     * 商品编号
     */
    private String id;

    /**
     * 商品名称
     */
    private String name;

    private com.recharge.domain.ProductSupRelation productSupRelation;

    /**
     * 状态
     */
    private boolean enabled;

    private Integer categoryId;

    private String categoryName;

    public static final int CATEGORY_ID_PLATFORM_CARD =10;

    /**
     * 平台卡密
     */
    public static final int CATEGORY_ID_PLATFORM_KHAMWI =8;
    /**
     * 第三方电子卡
     */
    public static final int THIRLD_PARTY_CARD=9;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public com.recharge.domain.ProductSupRelation getProductSupRelation() {
        return productSupRelation;
    }

    public void setProductSupRelation(com.recharge.domain.ProductSupRelation productSupRelation) {
        this.productSupRelation = productSupRelation;
    }
}
