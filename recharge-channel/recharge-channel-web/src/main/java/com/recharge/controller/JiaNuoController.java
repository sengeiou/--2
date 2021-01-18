package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.JiaNuo;
import com.recharge.service.ChannelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/jiaNuo")
public class JiaNuoController {

    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(@RequestBody JiaNuo jiaNuo){
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(jiaNuo.getOrderNo());
        responseOrder.setResponseCode(jiaNuo.getOrderStatus());
        logger.info("jiaNuo callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("100032", responseOrder);
        return "{'code':0}";
    }

}
