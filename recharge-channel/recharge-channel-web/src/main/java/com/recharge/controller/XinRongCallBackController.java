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
 * @create 2020/11/19 9:26
 */
@Controller
@RequestMapping("/xinRonghf")
public class XinRongCallBackController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String client_id, String out_logno, String logno, String money, String paytime,
                           String status, String ordersn, String fundbalance) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(out_logno);
        responseOrder.setResponseCode(status);
        logger.info("XingRongHf callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("100107", responseOrder);
        return "SUCCESS";
    }
}
