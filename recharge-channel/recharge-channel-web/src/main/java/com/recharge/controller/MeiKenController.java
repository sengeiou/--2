package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.Channel;
import com.recharge.service.ChannelService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author user
 * @create 2020/8/21 14:51
 */
@Controller
@RequestMapping("/meiken")
public class MeiKenController {
    @Autowired
    private ChannelService channelService;

    private String channelId = "100081";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userId,String bizId,String ejId,String downstreamSerialno,String status,String sign,String voucher,String voucherType ){

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(downstreamSerialno);
        responseOrder.setResponseCode(status);
        responseOrder.setOutChannelOrderId(voucher);
        channelService.callBack(channelId,responseOrder);
        logger.info("meiken callback :{}", JSON.toJSONString(responseOrder));
        logger.info("meiken remark1 :{}", responseOrder.getOutChannelOrderId());
        return "success";
    }
}
