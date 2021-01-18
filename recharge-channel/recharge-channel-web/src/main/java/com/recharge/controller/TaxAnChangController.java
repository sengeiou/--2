package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.AnChang;
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
@RequestMapping("/taxAnchang")
public class TaxAnChangController {

    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(@RequestBody AnChang anChang) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(anChang.getOrderNo());
        responseOrder.setResponseCode(anChang.getStatus());
        responseOrder.setOutChannelOrderId(anChang.getThirdSeri());
        logger.info("taxAnchanghf callback :{}", JSON.toJSONString(responseOrder));
        logger.info("taxAnchanghf thirdSeri :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100067", responseOrder);
        return "0000";
    }
}
