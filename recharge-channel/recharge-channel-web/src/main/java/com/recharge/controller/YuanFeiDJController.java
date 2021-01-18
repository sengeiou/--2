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
 * @author user
 * @create 2020/11/3 22:49
 */
@Controller
@RequestMapping("/yuanfeiDJ")
public class YuanFeiDJController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userid,String orderid,String sporderid,String merchantsubmittime,String resultno,String sign,String parvalue,String remark1,String payno,String fundbalance) {

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(sporderid);
        responseOrder.setResponseCode(resultno);
        responseOrder.setOutChannelOrderId(StringUtils.substring(remark1, 0 ,90));
        logger.info("yuanfeiDJ callback :{}", JSON.toJSONString(responseOrder));
        logger.info("yuanfeiDJ remark1 :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100101", responseOrder);
        return "success";
    }
}
