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
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 * @create 2021/3/17 9:31
 */
@Controller
@RequestMapping("/jztdNew")
public class JiuZhouTuoDaNewController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String UserId,String ShopId,String SysOrderId,String OrderId,String State,String Timestamp,String Sign,String VoucherType,String VoucherContent) {

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(OrderId);
        responseOrder.setResponseCode(State);
        responseOrder.setOutChannelOrderId(StringUtils.substring(VoucherContent, 0 ,90));
        logger.info("jztd callback :{}", JSON.toJSONString(responseOrder));
        logger.info("jztd remark1 :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100153", responseOrder);
        return "success";
    }
}
