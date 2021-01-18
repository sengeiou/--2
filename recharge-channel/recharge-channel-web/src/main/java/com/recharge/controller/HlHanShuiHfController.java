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

@RequestMapping("/hailanHanShui")
@Controller
public class HlHanShuiHfController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100040";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String code,String message,String orderId,String billNo,String createdAt,String topOrderNo,String key){

        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String channelKey = configJSONObject.getString("key");

        StringBuffer sb = new StringBuffer();
        if(StringUtils.isNotEmpty(billNo)){
            sb.append("billNo"+StringUtils.defaultString(billNo));
        }
        if(StringUtils.isNotEmpty(code)){
            sb.append("code"+StringUtils.defaultString(code));
        }
        if(StringUtils.isNotEmpty(createdAt)){
            sb.append("createdAt"+StringUtils.defaultString(createdAt));
        }

        if(StringUtils.isNotEmpty(message)){
            sb.append("message"+StringUtils.defaultString(message));
        }

        if(StringUtils.isNotEmpty(orderId)){
            sb.append("orderId"+StringUtils.defaultString(orderId));
        }

        if(StringUtils.isNotEmpty(topOrderNo)){
            sb.append("topOrderNo"+StringUtils.defaultString(topOrderNo));
        }

        logger.info("source {}" , sb.toString() + " "+key);

        if (!StringUtils.equals(DigestUtils.md5Hex(sb.toString() + channelKey).toUpperCase() , key)){
            logger.warn("sign check error");
            return "success";
        }

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(orderId);
        responseOrder.setResponseCode(code);
        responseOrder.setResponseMsg(message);
        channelService.callBack(channelId,responseOrder);
        return "success";
    }
}
