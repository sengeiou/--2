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

/**
 * Created by qi.cao on 2017/6/21.
 */
@Controller
@RequestMapping("/jiFenHui")
public class JiFenHuiCallBackController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100004";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callback")
    @ResponseBody
    public String callBack(@RequestBody String requestBody){
        JSONObject jsonObject = JSONObject.parseObject(requestBody);

//        渠道的订单号，这个渠道很特殊，订单号不是由我们传的
        String orderId = jsonObject.getString("ORDER_ID");
        String orderStatus = jsonObject.getString("ORDER_STATUS");
        String orderDesc = jsonObject.getString("ORDER_DESC");
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setResponseCode(orderStatus);
        responseOrder.setOutChannelOrderId(orderId);
        responseOrder.setResponseMsg(orderDesc);
        logger.info("jifenhui callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "SUCCESS";
    }
}
