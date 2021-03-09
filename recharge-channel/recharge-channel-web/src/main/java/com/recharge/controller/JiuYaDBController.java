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
 * @create 2021/3/9 11:04
 */
@Controller
@RequestMapping("/JiuYaDB")
public class JiuYaDBController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ChannelService channelService;

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userid, String orderid, String sporderid, String merchantsubmittime, String resultno, String sign, String parvalue, String fundbalance, String othorderid) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(sporderid);
        responseOrder.setResponseCode(resultno);
        if (!StringUtils.isEmpty(othorderid)) {
            responseOrder.setOutChannelOrderId(othorderid);
        }
        logger.info("久雅抖币回调的信息 :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("100148", responseOrder);
        return "success";
    }
}
