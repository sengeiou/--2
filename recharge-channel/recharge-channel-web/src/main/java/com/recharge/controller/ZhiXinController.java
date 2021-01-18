package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
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
 * @create 2020/3/24 18:27
 */
@Controller
@RequestMapping("/zhixin")
public class ZhiXinController {
    @Autowired
    private ChannelService channelService;

    private String channelId = "100055";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String client_order_no,String up_order_no,String product_type,
                           String phone_no,String deduction_amount,String recharge_status,String elecardID,String sign){
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(client_order_no);
        responseOrder.setResponseCode(recharge_status);
        responseOrder.setOutChannelOrderId(StringUtils.substring(elecardID, 0 ,90));
        logger.info("anchangHf callback :{}", JSON.toJSONString(responseOrder));
        logger.info("anchangHf elecardID :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack(channelId,responseOrder);
        return "true";
    }
}
