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

import java.sql.SQLOutput;

/**
 * @author Administrator
 * @create 2020/4/27 9:35
 */
@Controller
@RequestMapping("/ofpayJyk")
public class OfpayJykController {

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
        logger.info("ofpayJyk  callback :{}", JSON.toJSONString(responseOrder));
        logger.info("ofpayJyk  gascard_code :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100058",responseOrder);
        return "success";
    }
}
