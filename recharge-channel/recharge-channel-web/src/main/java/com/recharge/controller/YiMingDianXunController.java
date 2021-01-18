package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/yiMingDianXun")
public class YiMingDianXunController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100020";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(@RequestBody String requestBody){

        logger.info("{}",requestBody);
        JSONObject requestObj = JSONObject.parseObject(requestBody);
        String req_plus = requestObj.getString("req_plus");
        String result_status = requestObj.getString("result_status");
        String result_msg = requestObj.getString("result_msg");
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setResponseCode(result_status);
        responseOrder.setResponseMsg(result_msg);
        responseOrder.setChannelOrderId(req_plus);
        logger.info("YiMingDianXunController callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "OK";
    }
}
