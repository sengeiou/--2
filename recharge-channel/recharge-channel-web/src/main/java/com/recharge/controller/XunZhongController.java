package com.recharge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.callback.XunZhongCallBack;
import com.recharge.service.ChannelService;

/**
 * 讯众
 * 
 * @author liudong
 *
 */
@Controller
@RequestMapping("/xunzhong")
public class XunZhongController {

	@Autowired
	private ChannelService channelService;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 讯众回调
	 * @return
	 */
	@RequestMapping("/callback")
	@ResponseBody
	public String callBack(@RequestBody XunZhongCallBack callback) {
		logger.info("xunzhong callback :{}", JSON.toJSONString(callback));
		ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(callback.getCustomParm());
        responseOrder.setResponseCode(callback.getOrderSts()); //1：充值成功 -1：充值失败
        logger.info("xunzhong orderId :{}", responseOrder.getOutChannelOrderId());
        //建立供货商
        channelService.callBack("100088", responseOrder);
		return "ok";
	}
}
