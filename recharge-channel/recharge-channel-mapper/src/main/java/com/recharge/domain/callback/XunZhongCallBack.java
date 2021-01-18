package com.recharge.domain.callback;

/**
 * 讯众callback类
 * @author 222
 *
 */
public class XunZhongCallBack {
	
	private String requestId;//		2016010000000219	为验证码发送成功后返回的requestId唯一标识
	private String appid	;//cbc6ceb21fd34664b6e452ca2aa40b3f	用户登录云通信平台后，所创建的应用编号appid，若想调用当前流量充值接口，则此应用必须包含有流量功能，否则调用失败。
	private String orderSts		;//1	充值结果错误码 1：充值成功 -1：充值失败
	private String remark	;//充值成功	充值结果状态描述（交易成功时候，remark表示是运营商流水号。其他情况就是参考意义）
	private String mobile; //188****0804	充值手机
	private String oriAmount;//		2.94	消费金额
	private String customParm;//	
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getOrderSts() {
		return orderSts;
	}
	public void setOrderSts(String orderSts) {
		this.orderSts = orderSts;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getOriAmount() {
		return oriAmount;
	}
	public void setOriAmount(String oriAmount) {
		this.oriAmount = oriAmount;
	}
	public String getCustomParm() {
		return customParm;
	}
	public void setCustomParm(String customParm) {
		this.customParm = customParm;
	}
	
	

}
