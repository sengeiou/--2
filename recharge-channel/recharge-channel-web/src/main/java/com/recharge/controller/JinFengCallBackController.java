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


/**
 * @author Administrator
 * @create 2020/12/7 11:09
 */
@Controller
@RequestMapping("/jinFeng")
public class JinFengCallBackController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ChannelService channelService;
    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String R0_biztype, String R1_agentcode, String R2_mobile, String R3_parvalue, String R4_trxamount,
                           String R5_productcode, String R6_requestid, String R7_trxid, String R8_returncode,
                           String R9_extendinfo, String R10_trxDate, String hmac) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(R6_requestid);
        responseOrder.setResponseCode(R8_returncode);
        String[] split = R7_trxid.split("-");
        responseOrder.setOutChannelOrderId(split[0]);
        logger.info("jinFeng callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("100112", responseOrder);
        return "success";
    }
}
