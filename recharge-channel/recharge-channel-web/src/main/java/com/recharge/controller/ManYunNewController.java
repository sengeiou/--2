package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.ManYun;
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
 * @create 2021/1/4 14:46
 */
@Controller
@RequestMapping("/manYunNewJyk")
public class ManYunNewController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(ManYun manYun){
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(manYun.getOrderNo());
        responseOrder.setResponseCode(manYun.getStatus());
        if (StringUtils.isNotBlank(manYun.getSerialNumber())) {
            responseOrder.setOutChannelOrderId(StringUtils.substring(manYun.getSerialNumber(), 0, 90));
        }
        logger.info("ManYunJykNew callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("100124", responseOrder);
        return "success";
    }
}
