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

@Controller
@RequestMapping("/jiuzhoutuoda")
public class JiuZhouController {
    @Autowired
    private ChannelService channelService;

    private String channelId = "100034";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String UserId , String ShopId ,String SysOrderId ,
                           String OrderId ,String State ,String CreateTime,String CompleteTime,String Timestamp,String sign,String VoucherType,String VoucherContent){
        logger.info("request param, UserId:{} , ShopId:{} ,SysOrderId:{} , OrderId :{},State :{}," +
                        "CreateTime:{},CompleteTime:{},Timestamp:{},sign:{},VoucherType:{},VoucherContent:{}" , UserId , ShopId ,SysOrderId ,
                OrderId ,State ,CreateTime,CompleteTime,Timestamp,sign,VoucherType,VoucherContent);
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setResponseCode(State);
        responseOrder.setOutChannelOrderId(VoucherContent);
        responseOrder.setChannelOrderId(OrderId);
        logger.info("JiuZhouController callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "ok";
    }
}
