package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/jiaofei")
public class JiaoFei100Controller {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100009";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callback")
    @ResponseBody
    public String callBack(String APIID, String TradeType, String OutID,
                           String OrderID,String Account, String TotalPrice, String State,String Sign, String OrderInfo, String OutOrderNo) {

        logger.info("callback sourceString sign {}", Sign);
        //        渠道的订单号，这个渠道很特殊，订单号不是由我们传的
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setResponseCode(State);
        responseOrder.setChannelOrderId(OutID);
        if (StringUtils.isNotBlank(OutOrderNo)) {
            responseOrder.setOutChannelOrderId(OutOrderNo);
        }
        logger.info("JiaoFei100Controller callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);
        return "success";
    }

}
