package com.recharge.controller;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.WoHu;
import com.recharge.service.ChannelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author user
 * @create 2020/7/13 14:35
 */
@Controller
@RequestMapping("/wohu")
public class WoHuController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callback")
    @ResponseBody
    public String callBack(@RequestBody WoHu woHu) {
        ResponseOrder responseOrder = new ResponseOrder();
        String sign = woHu.getSign();
        JSONObject configJSONObject = JSON.parseObject(sign);
        String orderId = configJSONObject.getString("order_id");
        String orderStatus = configJSONObject.getString("order_status");
        String operatorNo = configJSONObject.getString("operator_no");
        responseOrder.setChannelOrderId(orderId);
        responseOrder.setResponseCode(orderStatus);
        responseOrder.setOutChannelOrderId(StringUtils.substring(operatorNo, 0 ,90));
        logger.info("wohu callback :{}", JSON.toJSONString(responseOrder));
        logger.info("wohu remark1 :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100073", responseOrder);
        return "success";
    }
}
