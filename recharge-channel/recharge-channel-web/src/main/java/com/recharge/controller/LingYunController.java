package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.LingYun;
import com.recharge.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 * @create 2021/3/8 15:22
 */
@Controller
@RequestMapping("/LingYun")
public class LingYunController {
    @Autowired
    private ChannelService channelService;
    //渠道编号
    private String channelId = "100149";
    //日志
    private Logger logger = LoggerFactory.getLogger(getClass());


    @RequestMapping("/callBack")
    @ResponseBody
    public String callback(@RequestBody LingYun lingYun) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(lingYun.getBody().getOrder_id());
        responseOrder.setResponseCode(lingYun.getBody().getOrder_status());
        logger.info("凌云抖币回调的信息 :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);
        return "T";
    }
}
