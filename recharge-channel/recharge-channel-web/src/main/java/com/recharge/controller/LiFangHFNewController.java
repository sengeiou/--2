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
 * @create 2021/3/9 10:27
 */
@Controller
@RequestMapping("/LiFangHFNew")
public class LiFangHFNewController {
    @Autowired
    private ChannelService channelService;
    //渠道编号
    private String channelId = "100147";
    //日志
    private Logger logger = LoggerFactory.getLogger(getClass());


    @RequestMapping("/callBack")
    @ResponseBody
    public String callback(String recharge_state, String outer_tid, String sign, String timestamp, String user_id, String tid,String voucher) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(outer_tid);
        responseOrder.setResponseCode(recharge_state);
        responseOrder.setOutChannelOrderId(voucher);
        logger.info("LiFangHFNewCallBackController callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);
        return "success";
    }
}
