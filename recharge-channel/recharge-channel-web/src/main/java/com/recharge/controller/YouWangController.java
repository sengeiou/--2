package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.YouWang;
import com.recharge.service.ChannelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 * @create 2021/3/1 11:06
 */
@Controller
@RequestMapping("/youWang")
public class YouWangController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String channelId="100140";

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(@RequestBody(required = false) YouWang youWang) {
        ResponseOrder responseOrder = new ResponseOrder();
        logger.info("游网回调信息(youWang):{}", JSON.toJSONString(youWang));
        if (StringUtils.isNotBlank(youWang.getOperatorOid())) {
            responseOrder.setOutChannelOrderId(StringUtils.substring(youWang.getOperatorOid(), 0, 90));
        }
        responseOrder.setChannelOrderId(youWang.getMerOrderid());
        responseOrder.setResponseCode(youWang.getResult());
        logger.info("游网回调信息(responseOrder):{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);
        return "SUCCESS";
    }
}
