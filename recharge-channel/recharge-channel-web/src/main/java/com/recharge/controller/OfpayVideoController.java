package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author ZhouYuhong
 * @date 2020/4/29 14:28
 */
@Controller
@RequestMapping("/ofpayVideo")
public class OfpayVideoController {

    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callback")
    @ResponseBody
    public String callBack(@RequestParam("ret_code")String retCode,
                           @RequestParam("sporder_id")String sporderId,
                           @RequestParam("ordersuccesstime")String ordersuccesstime,
                           @RequestParam("err_msg")String errMsg,
                           String gascard_code){

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setResponseCode(retCode);
        responseOrder.setChannelOrderId(sporderId);
        responseOrder.setResponseMsg(errMsg);
        responseOrder.setOutChannelOrderId(gascard_code);
        logger.info("ofpayVideo  callback :{}", JSON.toJSONString(responseOrder));
        logger.info("ofpayVideo  gascard_code :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100059",responseOrder);
        return "success";
    }
}
