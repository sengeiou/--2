package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.service.recharge.iml.hf.AiBaoRechargeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping("/fuHan")
public class FuHanController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100047";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(Integer fee, String phone, String orderid, String status) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setOutChannelOrderId(orderid);
        responseOrder.setResponseCode(status);
        logger.info("FuHanController callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);
        return "ok";
    }

}
