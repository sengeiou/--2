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

/**
 * @author qi.cao
 */
@Controller
@RequestMapping("/haoChong")
public class HaoChongController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100024";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userid,String orderid,String sporderid,String merchantsubmittime,
                           String resultno,String sign,String remark1){

        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String key = configJSONObject.getString("key");

        String sourceString ="userid="+userid+"&orderid="+orderid+"&sporderid="
                +sporderid+"&merchantsubmittime="+merchantsubmittime+"&resultno="+resultno;
        logger.info("source {}" , sourceString);
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(sporderid);
        responseOrder.setResponseCode(resultno);

        responseOrder.setOutChannelOrderId(StringUtils.substring(remark1 , 0 ,90));
        logger.info("haoChong callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "ok";
    }

}
