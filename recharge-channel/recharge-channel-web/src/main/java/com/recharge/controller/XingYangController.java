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
 * @create 2021/1/5 19:15
 */
@Controller
@RequestMapping("/xingYang")
public class XingYangController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String UserId, String ShopId, String SysOrderId, String OrderId, String State,
                           String CreateTime, String CompleteTime, String Timestamp, String sign, String VoucherType, String VoucherContent) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(OrderId);
        responseOrder.setResponseCode(State);
        if (StringUtils.isNotBlank(VoucherType)) {
            responseOrder.setOutChannelOrderId(VoucherContent);
        }
        logger.info("xingYang callback :{}", JSON.toJSONString(responseOrder));
        logger.info("xingYang remark1 :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100128", responseOrder);
        return "ok";
    }
}
