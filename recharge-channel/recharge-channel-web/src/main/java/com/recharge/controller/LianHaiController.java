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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Controller
@RequestMapping("/lianHai")
public class LianHaiController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100051";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String out_trade_id,
                           String orderStatus,
                           String order_number,
                           String price,
                           String customerA,
                           String customerB,
                           String supplierRemark,
                           String sign) {
        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String secretKey = configJSONObject.getString("secretKey");

        Map<String, String> signMap = new TreeMap<>();
        signMap.put("out_trade_id", out_trade_id);
        signMap.put("orderStatus", orderStatus);
        signMap.put("order_number", order_number);
        signMap.put("price", price);
        signMap.put("customerA", customerA);
        signMap.put("customerB", customerB);

        StringBuffer signSource = new StringBuffer();
        Iterator iter = signMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String value = (String) entry.getValue();
            signSource.append(value);
        }
        signSource.append(secretKey);

        String verifyString = DigestUtils.md5Hex(signSource.toString()).toLowerCase();
        if (!StringUtils.equals(sign, verifyString)) {
            logger.warn("sign check error");
            return "fail";
        }

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(out_trade_id);
        responseOrder.setResponseCode(orderStatus);
        responseOrder.setOutChannelOrderId(supplierRemark);
        logger.info("LianHaiController callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);
        return "success";
    }


}
