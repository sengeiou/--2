package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
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
@RequestMapping("/yixiao")
public class YiXiaoCallBackController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100021";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String sporder_id,String order_id,String ordercash,String pro_name,String ststus,String sign){
        logger.info("sporder_id:{} ,order_id:{},ordercash:{},pro_name:{},ststus:{},sign:{}",sporder_id ,order_id , ordercash,pro_name,ststus,sign);
        String md5 = DigestUtils.md5Hex(sporder_id + order_id + ststus + ordercash);
        if (!StringUtils.equals(md5 , sign)){
            logger.warn("md5 check error");
            return "fail";
        }
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setResponseCode(ststus);
        responseOrder.setChannelOrderId(sporder_id);
        logger.info("YiXiaoCallBackController callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);

        return "success";
    }
}
