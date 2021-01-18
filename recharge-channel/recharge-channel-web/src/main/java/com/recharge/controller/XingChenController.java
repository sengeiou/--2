package com.recharge.controller;

import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/xingChen")
public class XingChenController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100030";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String CoopId,String TranId,String OrderNo,String OrderStatus,
                           String OrderSuccessTime,String FailedCode,String FailedReason,String Sign){

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(TranId);
        responseOrder.setResponseCode(OrderStatus);
        channelService.callBack(channelId , responseOrder);

        return "<?xml version=\"1.0\" encoding=\"gb2312\"?>\n" +
                "<response>\n" +
                "<orderSuccess>T</orderSuccess >\n" +
                "<failedCode></failedCode>\n" +
                "<failedReason> </failedReason>\n" +
                "</response>\n";
    }
}
