package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.Channel;
import com.recharge.service.ChannelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author qi.cao
 */
@Controller
@RequestMapping("/xiangShang")
public class XiangShangController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100026";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String id,String orderId,String errcode,String errinfo,String voucher){
        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String macid = configJSONObject.getString("macid");
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setOutChannelOrderId(StringUtils.substring(voucher , 0 ,90));
        responseOrder.setChannelOrderId(id.substring(id.indexOf(macid)+macid.length()));
        responseOrder.setResponseCode(errcode);
        responseOrder.setResponseMsg(errinfo);
        logger.info("xiangshang callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "true";
    }
}
