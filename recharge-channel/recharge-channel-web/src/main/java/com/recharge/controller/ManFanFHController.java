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
 * @author user
 * @create 2020/9/25 14:34
 */
@Controller
@RequestMapping("/mFFengHuang")
public class ManFanFHController {
    @Autowired
    private ChannelService channelService;

    private String channelId = "100090";

    private Logger logger = LoggerFactory.getLogger(getClass());


    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String userid, String orderid, String sporderid, String merchantsubmittime, String resultno, String sign, String payno) {

        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());

        String md5Source = "userid=" + userid + "&orderid=" + orderid + "&sporderid=" + sporderid + "&merchantsubmittime=" + merchantsubmittime + "&resultno=" + resultno + "&key=";

        logger.info("xcmd5Source : {}  ,xcsign：{}", md5Source, sign);
        String md5Key = configJSONObject.getString("md5Key");
        if (!StringUtils.equals(DigestUtils.md5Hex(md5Source + md5Key).toUpperCase(), sign)) {
            return "OK";
        }

        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(sporderid);
        responseOrder.setOutChannelOrderId(payno);
        responseOrder.setResponseCode(resultno);
        logger.info("MFfenghuang  payno :{}", responseOrder.getOutChannelOrderId());
        logger.info("MFfenghuang callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);

        return "OK";
    }
}
