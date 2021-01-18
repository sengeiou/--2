package com.recharge.domain.condition;


public class MerchantBuyCardCondition {

	/**
	 * 
	*/
	private Integer id;

	/**
	 * 订单编号
	*/
	private String orderId;

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
	private String supId;
	/**
	 * 客户编号
	*/
	private String merchantId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
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

	public String getSupId() {
		return supId;
	}

	public void setSupId(String supId) {
		this.supId = supId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
}