package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import com.recharge.utils.desDemo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author user
 * @create 2020/7/14 18:59
 */
@Controller
@RequestMapping("/lakala")
public class LakalaController {
    @Autowired
    private ChannelService channelService;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String param) {
        ResponseOrder responseOrder = new ResponseOrder();
        String Json = desDemo.decrypt(param);
        logger.info("lakala callback :{}", JSON.toJSONString(Json));
        String data = JSONObject.parseObject(Json).getString("data");
        String orderId = JSONObject.parseObject(Json).getString("order_id");
        String voucherList = JSONObject.parseObject(data).getString("voucherList");
        JSONArray jsonArray = new JSONArray(JSON.parseArray(voucherList));
        String delivery_state=null;
        String voucher_no = null;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
             delivery_state = jsonObject.getString("delivery_state");
            voucher_no= jsonObject.getString("voucher_no");
            logger.info("channelOrderId:{}",voucher_no);
        }
        responseOrder.setChannelOrderId(orderId);
        responseOrder.setResponseCode(delivery_state);
        responseOrder.setOutChannelOrderId(voucher_no);
        logger.info("lakala callback :{}", JSON.toJSONString(responseOrder));
        logger.info("lakala remark1 :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100075", responseOrder);
        return "success";
    }
}
