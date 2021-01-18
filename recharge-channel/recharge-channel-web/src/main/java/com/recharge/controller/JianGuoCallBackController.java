package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CAOQI on 2017/8/23.
 */
@Controller
@RequestMapping("/jianGuo")
public class JianGuoCallBackController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100006";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callback")
    @ResponseBody
    public String callBack(@RequestParam("orderno")String orderno,
                           @RequestParam("orderstatus")String status,
                           @RequestParam("rechargeresult")String rechargeresult){
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setResponseCode(status);
        responseOrder.setChannelOrderId(orderno);

        Map<String,String> resultInfo = new HashMap<>();
        resultInfo.put("order_success","充值成功");
        resultInfo.put("order_failed","充值失败");

        responseOrder.setResponseMsg(resultInfo.get(status));
        logger.info("jianGuo callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "OK";
    }

}
