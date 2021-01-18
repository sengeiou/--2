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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/jiaofei")
public class JiaoFei100JykController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100010";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/jyk/callback")
    @ResponseBody
    public String callBack(@RequestParam("APIID") String APIID,@RequestParam("TradeType")String TradeType,@RequestParam("OutID")String OutID,
                           @RequestParam("OrderID")String OrderID,
                           @RequestParam("Account")String Account,@RequestParam("TotalPrice")String TotalPrice,@RequestParam("State")String State
            ,@RequestParam("Sign")String Sign){
        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String apiKey = configJSONObject.getString("apiKey");
        String sourceString = "APIID="+APIID+"&Account="+Account+"&OrderID="+OrderID+"&OutID="+OutID+"&State="+State+"&TradeType="+TradeType
                +"&TotalPrice="+TotalPrice;

        logger.info("callback sourceString {}",sourceString);
        if (StringUtils.equals(DigestUtils.md5Hex(sourceString+apiKey),Sign)){
            //        渠道的订单号，这个渠道很特殊，订单号不是由我们传的
            ResponseOrder responseOrder = new ResponseOrder();
            responseOrder.setResponseCode(State);
            responseOrder.setOutChannelOrderId(OutID);
            responseOrder.setOutChannelOrderId(OrderID);
            logger.info("JiaoFei100Controller callback :{}", JSON.toJSONString(responseOrder));
            channelService.callBack(channelId,responseOrder);
            return "success";
        }
        return "fail";
    }

}
