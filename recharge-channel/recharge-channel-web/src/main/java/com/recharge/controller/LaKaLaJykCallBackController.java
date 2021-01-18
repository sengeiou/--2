package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.iml.jyk.LaKaLajykRechargeServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 * @create 2020/12/1 14:36
 */
@Controller
@RequestMapping("/lakalajyk")
public class LaKaLaJykCallBackController {
    @Autowired
    private ChannelService channelService;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String param) {
        LaKaLajykRechargeServiceImpl laKaLajykRechargeService = new LaKaLajykRechargeServiceImpl();
        ResponseOrder responseOrder = new ResponseOrder();
        String Json = laKaLajykRechargeService.decrypt(param);
        logger.info("lakala callback :{}", JSON.toJSONString(Json));
        String res = JSONObject.parseObject(Json).getString("res");
        if (StringUtils.equals(res,"true")){
            String data = JSONObject.parseObject(Json).getString("data");
            String state = JSONObject.parseObject(data).getString("delivery_state");
            String orderId = JSONObject.parseObject(data).getString("order_id");
            responseOrder.setChannelOrderId(orderId);
            responseOrder.setResponseCode(state);
        }else {
            String message = JSONObject.parseObject(Json).getString("message");
            String mes = JSONObject.parseObject(message).getString("mes");
            responseOrder.setResponseCode("2");
            responseOrder.setResponseMsg(mes);
        }
        logger.info("lakala callback :{}", JSON.toJSONString(responseOrder));
        logger.info("lakala remark1 :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100110", responseOrder);
        return "success";
    }
}
