package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 * @create 2020/11/2 17:10
 */
@Controller
@RequestMapping("/xingChenHF")
public class XingChenHfController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String orderNo,String mchOrderNo,String state,String msg,String sign) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(mchOrderNo);
        responseOrder.setResponseCode(state);
        logger.info("XingChenHf callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("100097", responseOrder);
        return "ok";
    }
}
