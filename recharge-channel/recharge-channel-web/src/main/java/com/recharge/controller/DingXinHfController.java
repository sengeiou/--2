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
@RequestMapping("/dingXinHf")
public class DingXinHfController {

    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userid,String flowNo,String orderid,String state,String account,String userkey){
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(orderid);
        responseOrder.setResponseCode(state);
        responseOrder.setOutChannelOrderId(flowNo);
        logger.info("dingXinHf callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("100035",responseOrder);
        return "OK";
    }
}
