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
 * @create 2020/10/20 14:59
 */
@Controller
@RequestMapping("/yiGu")
public class YiGuController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public void callBack(String userid,String orderid,String sporderid,String merchantsubmittime,String resultno,String cert,String sign) {

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(sporderid);
        responseOrder.setResponseCode(resultno);
        if (!cert.isEmpty()) {
            responseOrder.setOutChannelOrderId(StringUtils.substring(cert, 0, 90));
        }
        logger.info("yiGu callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("100093", responseOrder);
    }
}
