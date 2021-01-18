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

@Controller
@RequestMapping("/tongYun")
public class TongYunController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100041";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String UserId,String ShopId,String SysOrderId,String OrderId,
                           String State,String CreateTime,String CompleteTime,String Timestamp,String sign,String VoucherContent){

        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String key = configJSONObject.getString("key");

        String sourceString =ShopId+UserId+SysOrderId+OrderId+State+Timestamp;
        logger.info("source {}" , sourceString +"   " + sign);
        if (!StringUtils.equalsIgnoreCase(DigestUtils.md5Hex(sourceString+ key) , sign)){
            return "ok";
        }
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setOutChannelOrderId(StringUtils.substring(VoucherContent , 0 ,90));
        responseOrder.setChannelOrderId(OrderId);
        responseOrder.setResponseCode(State);
        logger.info("TongYunController callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "ok";
    }
}
