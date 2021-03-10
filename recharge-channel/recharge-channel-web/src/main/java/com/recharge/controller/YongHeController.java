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
 * @create 2021/3/10 16:01
 */
@Controller
@RequestMapping("/YongHe")
public class YongHeController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String channelId="100150";

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String szAgentId,String szOrderId,String szPhoneNum,String nDemo,String fSalePrice,
                           String nFlag,String szRtnMsg,String szVerifyString) {
        ResponseOrder responseOrder = new ResponseOrder();
        if (StringUtils.isNotBlank(szRtnMsg)) {
            responseOrder.setOutChannelOrderId(StringUtils.substring(szRtnMsg, 0, 90));
        }
        responseOrder.setChannelOrderId(szOrderId);
        responseOrder.setResponseCode(nFlag);
        logger.info("永禾话费回调信息(responseOrder):{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);
        return "ok";
    }
}
