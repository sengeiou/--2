package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.ZhengBang;
import com.recharge.service.ChannelService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2021/3/12 9:19
 */
@Controller
@RequestMapping("/zhengBang")
public class ZhengBangController {
    @Autowired
    private ChannelService channelService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(@RequestBody ZhengBang zhengBang) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(zhengBang.getOrderId());
        responseOrder.setResponseCode(zhengBang.getStatus());
        responseOrder.setOutChannelOrderId(StringUtils.substring(zhengBang.getVoucher(), 0 ,90));
        logger.info("zhengBang callback :{}", JSON.toJSONString(responseOrder));
        logger.info("zhengBang remark1 :{}", responseOrder.getOutChannelOrderId());
        channelService.callBack("100151", responseOrder);
        Map<String,String> map = new HashMap<>();
        map.put("agentAcct",zhengBang.getAgentAcct());
        map.put("orderId",zhengBang.getOrderId());
        map.put("retCode","SUCCESS");
        String sign = DigestUtils.md5Hex("agentAcct="+zhengBang.getAgentAcct()+"&orderId="+zhengBang.getOrderId()+"&retCode=SUCCESS"+"&key=4e0ac5df2e82548c5f8bdbe2a829da5f");
        map.put("sign",sign);
        return JSONObject.toJSONString(map);
    }

}
