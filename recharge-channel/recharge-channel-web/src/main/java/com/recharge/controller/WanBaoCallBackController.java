package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.Channel;
import com.recharge.service.ChannelService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/wanBaoNeo")
public class WanBaoCallBackController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100049";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String agentid, String orderno, String orderstatus, String supplierOrderNo, String verifystring) {

        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String key = configJSONObject.getString("key");

        String sign = DigestUtils.md5Hex("agentid=" + agentid + "&orderno=" + orderno + "&orderstatus=" + orderstatus + "&merchantKey=" + key);
        if (!StringUtils.equals(sign, verifystring)) {
            logger.warn("sign check error");
            return "F";
        }
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(orderno);
        responseOrder.setResponseCode(orderstatus);
        logger.info("WanBaoCallBackController callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);
        return "T";
    }

}
