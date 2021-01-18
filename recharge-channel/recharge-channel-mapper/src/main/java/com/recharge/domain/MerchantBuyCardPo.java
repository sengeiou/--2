package com.recharge.domain;

import java.util.Date;

public class MerchantBuyCardPo {

	/**
	 * 
	*/
	private Integer id;
	/**
	 * 订单编号
	*/
	private String orderId;
	/**
	 * 卡号
	*/
	private String cardNo;
	/**
	 * 密码
	*/
	private String cardPwd;
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
	/**
	 * 添加时间
	*/
	private Date addTime;

	//过期时间
	private Date expireTime;

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

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

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getCardPwd() {
		return cardPwd;
	}

	public void setCardPwd(String cardPwd) {
		this.cardPwd = cardPwd;
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

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
}