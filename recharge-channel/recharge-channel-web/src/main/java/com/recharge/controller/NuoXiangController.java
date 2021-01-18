package com.recharge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;

/**
 * 诺祥
 * 
 * @author liudong
 *
 */
@Controller
@RequestMapping("/nuoxiang")
public class NuoXiangController {

	@Autowired
	private ChannelService channelService;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 诺祥
	 * @return
	 */
	@RequestMapping("/callback")
	@ResponseBody
	public String callBack(String State,String UserId,String Sign,
			String Timestamp,String SysOrderId,String OrderId,String VoucherType,
			String VoucherContent) {
		 ResponseOrder responseOrder = new ResponseOrder();
		 String psOrderId = OrderId.substring(UserId.length(),OrderId.length());
		 responseOrder.setChannelOrderId(psOrderId);
	     responseOrder.setResponseCode(State);
	     logger.info("nuoxiang call back State:{} , SysOrderId:{} , UserId:{} , OrderId:{} , psOrderId:{}",State,SysOrderId,UserId,OrderId,psOrderId);
	     channelService.callBack("100089", responseOrder); //渠道
	     //responseOrder.setOutChannelOrderId(VoucherContent);
	     //String checkMd5 = DigestUtils.md5Hex(UserId+SysOrderId+orderId+State+Timestamp+)
	     return "ok";
	}
	public static void main(String[] args) {
		String SysOrderId = "100171600679796250";
		String UserId ="10017";
		String orderId = SysOrderId.substring(UserId.length(),SysOrderId.length());
		System.out.println(orderId);
	}
}
