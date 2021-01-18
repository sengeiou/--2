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
@RequestMapping("/juKa")
public class JuKaCallBackController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100017";

    private Logger logger= LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userid,String orderid,String sporderid,String merchantsubmittime,String resultno,String sign){

        logger.info("userid:{},orderid:{},sporderid:{},merchantsubmittime:{},resultno,{},sign:{}",
                userid,orderid,sporderid,merchantsubmittime,resultno,sign);

        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String key = configJSONObject.getString("key");
        String sourceString ="userid="+userid+"&orderid="+orderid+"&sporderid="+sporderid
                +"&merchantsubmittime="+merchantsubmittime+"&resultno="+resultno+"&key="+key;

        if (StringUtils.equals(DigestUtils.md5Hex(sourceString) , sign)){
            ResponseOrder responseOrder = new ResponseOrder();
            responseOrder.setResponseCode(resultno);
            responseOrder.setChannelOrderId(sporderid);
            responseOrder.setOutChannelOrderId(orderid);
            logger.info("JuKaCallBackController callback :{}", JSON.toJSONString(responseOrder));
            channelService.callBack(channelId,responseOrder);
        }

        return "OK";
    }
}
