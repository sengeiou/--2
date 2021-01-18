package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author user
 * @create 2020/11/6 14:16
 */
@Controller
@RequestMapping("/boRui")
public class BoRuiController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(@RequestBody List<JSONObject> list) {
        String status =(String) list.get(0).get("Status");
        String outTradeNo = (String) list.get(0).get("OutTradeNo");
        String  receiptNum=(String) list.get(0).get("ReceiptNum");
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(outTradeNo);
        responseOrder.setResponseCode(status);
        responseOrder.setOutChannelOrderId(StringUtils.substring(receiptNum, 0 ,90));
        logger.info("BoRui callback :{}", JSON.toJSONString(responseOrder));
        logger.info("BoRui remark1 :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100104", responseOrder);
        return "ok";
    }

}
