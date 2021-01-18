package com.recharge.controller;

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

@Controller
@RequestMapping("/juhe")
public class JuHeCallBackController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String channelId = "100013";

    @Autowired
    private ChannelService channelService;

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String sporder_id,String orderid,String sta,String sign,String err_msg){
        logger.info("request param. sporder_id:{},orderid:{},sta:{},sign:{},err_msg:{}",sporder_id , orderid ,sta ,sign
        ,err_msg);

        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String apiKey = configJSONObject.getString("apiKey");
        String signTemp = DigestUtils.md5Hex(apiKey+sporder_id+orderid);

        if (StringUtils.equals(signTemp , sign)){
            ResponseOrder responseOrder = new ResponseOrder();
            responseOrder.setChannelOrderId(orderid);
            responseOrder.setOutChannelOrderId(sporder_id);
            responseOrder.setResponseCode(sta);
            responseOrder.setResponseMsg(err_msg);


            channelService.callBack(channelId,responseOrder);
            return "ok";
        }else{
            return "fail";
        }
    }
}
