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
 * @author user
 * @create 2020/9/21 15:36
 */
@Controller
@RequestMapping("/yiJun")
public class YiJunController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String UserId, String ShopId, String SysOrderId, String OrderId, String State,
                           String CreateTime, String CompleteTime, String Timestamp,String sign,String VoucherType,String VoucherContent) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(OrderId);
        responseOrder.setResponseCode(State);
        responseOrder.setOutChannelOrderId(VoucherContent);
        logger.info("yiJun callback :{}", JSON.toJSONString(responseOrder));
        logger.info("yiJun remark1 :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100086", responseOrder);
        return "ok";
    }
}
