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
 * @create 2021/3/4 13:56
 */
@Controller
@RequestMapping("/DaShang")
public class DaShangController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userId, String bizId, String ejId, String voucher, String voucherType, String downstreamSerialno, String status, String sign) {

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(downstreamSerialno);
        responseOrder.setResponseCode(status);
        if (StringUtils.isNotBlank(voucher)) {
            responseOrder.setOutChannelOrderId(StringUtils.substring(voucher, 0, 90));
        }
        logger.info("达尚 callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("", responseOrder);
        return "success";
    }
}
