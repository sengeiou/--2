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
 * @author user
 * @create 2020/6/30 18:36
 */
@Controller
@RequestMapping("/jiuya")
public class JiuyaController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ChannelService channelService;

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userid,String orderid,String sporderid,String merchantsubmittime,String resultno,String sign,String parvalue,String fundbalance,String othorderid) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(sporderid);
        responseOrder.setResponseCode(resultno);
        responseOrder.setOutChannelOrderId(othorderid);
        logger.info("JiuyaController callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("100066", responseOrder);
        return "success";
    }

}
