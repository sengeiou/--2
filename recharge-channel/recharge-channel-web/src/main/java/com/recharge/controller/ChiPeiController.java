package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.Channel;
import com.recharge.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/chiPei")
public class ChiPeiController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100028";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String account,String clientOrderNo,String clientSubmitTime,
                           String orderStatus,String errorCode,String errorDesc){

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(clientOrderNo);
        responseOrder.setResponseCode(orderStatus);
        logger.info("chiPei callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "{\n" +
                "\"resultCode \":\"0000\",\n" +
                "\"resultMsg\":\"处理成功！\"\n" +
                "}\n";
    }
}
