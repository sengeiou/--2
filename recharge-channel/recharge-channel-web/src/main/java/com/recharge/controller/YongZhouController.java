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
 * @create 2021/3/8 14:18
 */
@Controller
@RequestMapping("/YongZhou")
public class YongZhouController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String channelId="100144";

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String UserId,String ShopId,String SysOrderId,String OrderId,String State,
                           String CreateTime,String CompleteTime,String Timestamp
            ,String sign,String VoucherType,String VoucherContent) {
        ResponseOrder responseOrder = new ResponseOrder();
        if (StringUtils.isNotBlank(VoucherType)) {
            responseOrder.setOutChannelOrderId(StringUtils.substring(VoucherContent, 0, 90));
        }
        responseOrder.setChannelOrderId(OrderId);
        responseOrder.setResponseCode(State);
        logger.info("泳州话费回调信息(responseOrder):{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);
        return "ok";
    }
}
