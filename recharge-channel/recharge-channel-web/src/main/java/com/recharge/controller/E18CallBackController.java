package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
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
@RequestMapping("/e18")
public class E18CallBackController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100023";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callback")
    @ResponseBody
    public String callBack(@RequestParam("number")String number,
                           @RequestParam("code")String code,
                           @RequestParam("sign")String sign){
        if (!StringUtils.equals(DigestUtils.md5Hex(number + code ),sign)){
            return "false";
        }
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setResponseCode(code);
        responseOrder.setChannelOrderId(number);
        logger.info("jianGuo callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "0006";
    }
}
