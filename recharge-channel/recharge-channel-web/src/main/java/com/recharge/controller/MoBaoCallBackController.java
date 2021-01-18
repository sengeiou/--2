package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/moBao")
public class MoBaoCallBackController {
    @Autowired
    private ChannelService channelService;

    private String channelId = "100029";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String mobile,String spcode,String content,
                           String linkid,String status,String fee,String date,
                           String datetime){

        logger.info("accept callback : {} ,{},{},{},{},{},{},{}" , mobile ,spcode,content,linkid,status,fee,date,datetime);
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(linkid);
        responseOrder.setResponseCode(status);
        logger.info("moBao callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "OK";
    }
}
