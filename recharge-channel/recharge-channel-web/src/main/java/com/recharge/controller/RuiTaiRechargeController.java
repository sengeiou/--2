package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.Channel;
import com.recharge.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/ruiTai")
public class RuiTaiRechargeController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100025";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userId,String orderId,String serialno,String orderStatus,
                           String sign){

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(serialno);
        responseOrder.setResponseCode(orderStatus);
        logger.info("haoChong callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);

        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<response>\n" +
                "\t<status>success</status>\n" +
                "<code>00</code>\n" +
                "</response>\n";
    }
}
