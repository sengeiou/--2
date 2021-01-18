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
 * @create 2020/7/16 10:12
 */

@Controller
@RequestMapping("/xunyin")
public class XunYinController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String id, String userid, String status, String code, String ext,
                           String statemes, String operatorid, String sign) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(id);
        responseOrder.setResponseCode(status);
        responseOrder.setOutChannelOrderId(StringUtils.substring(operatorid, 0 ,90));
        logger.info("xunyin callback :{}", JSON.toJSONString(responseOrder));
        logger.info("xunyin remark1 :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100076", responseOrder);
        return "ok";
    }
}
