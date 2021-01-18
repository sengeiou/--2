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
@RequestMapping("/aiBao")
public class AiBaoController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100043";

    @Resource(name = "channelMap")
    private Map<String, AbsChannelRechargeService> rechargeServiceMap;

    @Autowired
    private AiBaoRechargeService aiBaoRechargeService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(Integer goodsPrice, String goodsOperator, String goodsName, String goodsId, String userData,
                           Integer goodsDenomination, String accountNo, String cardId, String outOrderId,
                           Integer payMoney, Integer num, String orderId, String orderRes, String isRefund, Date ctime) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(outOrderId);
        responseOrder.setResponseCode(orderRes);
        logger.info("AiBaoController callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);
        return "success";
    }


   



}
