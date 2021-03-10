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
 * @create 2021/2/1 17:10
 */
@Controller
@RequestMapping("/ziXuan")
public class ZiXuanController {
    @Autowired
    private ChannelService channelService;

    private String channelId = "";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String szAgentId,String szOrderId,String szPhoneNum,
                           String nDemo,String fSalePrice,String nFlag,String szRtnMsg,String szVerifyString){
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(szOrderId);
        responseOrder.setResponseCode(nFlag);
        if (StringUtils.isEmpty(szRtnMsg)){
            responseOrder.setOutChannelOrderId(StringUtils.substring(szRtnMsg, 0 ,90));
        }
        logger.info("ZiXuan callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "ok";
    }


}
