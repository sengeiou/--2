package com.recharge.controller;

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
@RequestMapping("/manFanShouZhi")
public class ManFanShouZhiController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100045";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userId,String bizId,String ejId,String downstreamSerialno ,String voucher ,String voucherType,String status,String sign ){

        logger.info("callBack param: userId:{} , bizId:{} ,ejId:{},downstreamSerialno:{} ,voucher:{} ,voucherType:{},status:{},sign:{}",
                userId,bizId,ejId,downstreamSerialno ,voucher ,voucherType,status,sign);
        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String key = configJSONObject.getString("key");

        if (!StringUtils.equals(DigestUtils.md5Hex(bizId+downstreamSerialno+ejId+ status + userId + key),sign)){
            return "error";
        }
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(downstreamSerialno);
        responseOrder.setResponseCode(status);
        responseOrder.setOutChannelOrderId(voucher);
        channelService.callBack(channelId,responseOrder);
        return "success";
    }
}
