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
 * @create 2021/1/13 14:56
 */
@Controller
@RequestMapping("/suKa")
public class SuKaController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ChannelService channelService;

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userid, String orderid, String sporderid, String merchantsubmittime, String resultno, String sign, String parvalue, String remark1, String fundbalance) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(sporderid);
        responseOrder.setResponseCode(resultno);
        if (StringUtils.isNotBlank(remark1)) {
            responseOrder.setOutChannelOrderId(remark1);
        }
        logger.info("速卡异步回调接口接收参数 :{}", JSON.toJSONString(responseOrder));
        channelService.callBack("100132", responseOrder);
        return "success";
    }
}
