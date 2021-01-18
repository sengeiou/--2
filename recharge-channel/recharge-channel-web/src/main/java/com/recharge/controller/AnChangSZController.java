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

@Controller
@RequestMapping("/anChangSZ")
public class AnChangSZController {

    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userid, String orderid, String sporderid, String merchantsubmittime, String resultno,
                           String sign, String parvalue, String fundbalance,String remark1) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(sporderid);
        responseOrder.setResponseCode(resultno);
        responseOrder.setOutChannelOrderId(StringUtils.substring(remark1, 0 ,90));
        logger.info("anchangszHf callback :{}", JSON.toJSONString(responseOrder));
        logger.info("anchangszHf remark1 :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100056", responseOrder);
        return "success";
    }
}
