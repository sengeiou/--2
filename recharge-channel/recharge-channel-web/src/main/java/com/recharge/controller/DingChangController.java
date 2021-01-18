package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.service.ChannelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 * @create 2021/1/4 18:04
 */
@Controller
@RequestMapping("/dingChang")
public class DingChangController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String agent, String out_order_no, String order_no, String amount, String agent_price, String status, String refid, String sign) {

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(out_order_no);
        responseOrder.setResponseCode(status);
        if (StringUtils.isNotBlank(refid)) {
            responseOrder.setOutChannelOrderId(StringUtils.substring(refid, 0, 90));
        }
        logger.info("dingChang callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("100126", responseOrder);
        return "SUCCESS";
    }
}
