package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianmi.open.api.tool.util.SignUtil;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/liFang")
public class LiFangCallBackController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100053";

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws Exception{

        /*
        *
        * |lifang callBack sourceString:outer_tidPS201807161713131466recharge_state=1tidS1807161691631timestamp2018-07-16 18:53:25user_idA2679307 , sign:6E5F4C339C5C36AB46889AE24FDD686985944DF4|""

        * */
        Map<String,String> requestMap = new HashMap<String,String>();
        requestMap.put("outer_tid","PS201807161713131466");
        requestMap.put("recharge_state","1");
        requestMap.put("tid","S1807161691631");
        requestMap.put("timestamp","2018-07-16 18:53:25");
        requestMap.put("user_id","A2679307");
        String s = SignUtil.sign(requestMap,"uUJxCLDdKnCLebaBfjt4La58CuZRhhhn");
        System.out.println(s);
    }

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String recharge_state,String outer_tid,String sign,String timestamp,String user_id,String tid){

        String sourceString = "outer_tid="+outer_tid+"recharge_state="+recharge_state+"tid="+tid+"timestamp="+timestamp+"user_id="+user_id;

        logger.info("lifang callBack sourceString:{} , sign:{}",sourceString,sign);
        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String appSecret = configJSONObject.getString("appSecret");

        Map<String,String> requestMap = new HashMap<String,String>();
        requestMap.put("outer_tid",outer_tid);
        requestMap.put("recharge_state",recharge_state);
        requestMap.put("tid",tid);
        requestMap.put("timestamp",timestamp);
        requestMap.put("user_id",user_id);
        String s = null;
        try {
            s = SignUtil.sign(requestMap,appSecret);
        } catch (IOException e) {
            logger.error("invoke signUtil error" , e);
            return "";
        }

        if (StringUtils.equals(sign, s)){
            ResponseOrder responseOrder = new ResponseOrder();
            responseOrder.setResponseCode(recharge_state);
            responseOrder.setChannelOrderId(outer_tid);
            responseOrder.setOutChannelOrderId(tid);
            logger.info("LiFangCallBackController callback :{}", JSON.toJSONString(responseOrder));
            channelService.callBack(channelId,responseOrder);
        }

        return "success";
    }
}