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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/ygjyk")
public class YgJykCallBackController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100008";

    private Logger logger = LoggerFactory.getLogger(getClass());

/*    public static void main(String[] args) {
        String[] a = new String[]{"cp_code","cp_tran_no","prod_code","prod_num","trana_no","tran_price","tran_state","time"};
        Arrays.sort(a);
        System.out.println(JSON.toJSONString(a));
    }*/

    @RequestMapping("/callback")
    @ResponseBody
    public String callBack(@RequestParam("OrderID")String OrderID,
                           @RequestParam("MerchantOrderID")String MerchantOrderID,
                           @RequestParam("State")String State,
                           @RequestParam("StateInfo")String StateInfo,
                           @RequestParam("Sign")String Sign){

        logger.info("yg response {},{},{},{},{}",OrderID,MerchantOrderID,State,StateInfo,Sign);
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setResponseCode(State);
        responseOrder.setChannelOrderId(MerchantOrderID);
        responseOrder.setOutChannelOrderId(OrderID);
        responseOrder.setResponseMsg(StateInfo);
        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());

        String sourceString  = OrderID + MerchantOrderID + State ;
        logger.info("加密原串是："+sourceString);
        if (!StringUtils.equals(Sign, DigestUtils.md5Hex(sourceString+configJSONObject.getString("md5Key")).toUpperCase())){
            return "fail";
        }

        channelService.callBack(channelId,responseOrder);
        return "OK";
    }


    public static void main(String[] args) {

        System.out.println(DigestUtils.md5Hex("20180101172346316680020493514563PS2018010117232459222D7818B1773057B8").toUpperCase());
    }
}
