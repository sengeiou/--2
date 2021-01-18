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
@RequestMapping("/xuanJie")
public class XuanJieController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100050";

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * @param szAgentId      商户账号(需联系商务生成)
     * @param szOrderId      商户平台自行生成的订单编号，保证唯一性(由字母或数字组成) 同一订单号同一天内提交只处理一次
     * @param szPhoneNum     手机号码
     * @param nDemo          面值： 流量产品单位为：M 话费产品单位为：元 例如：10M nDemo=10
     * @param fSalePrice     售价
     * @param nFlag          2成功 3失败
     * @param szRtnMsg       备注字段，不参数签名 如有凭证，此参数返回充值凭证
     * @param szVerifyString 验证摘要串
     * @return
     */
    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(String szAgentId, String szOrderId, String szPhoneNum, String nDemo,
                           String fSalePrice, String nFlag, String szRtnMsg, String szVerifyString) {

        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());
        String key = configJSONObject.getString("key");

        String sign = DigestUtils.md5Hex(
                "szAgentId=" + szAgentId
                        + "&szOrderId=" + szOrderId
                        + "&szPhoneNum=" + szPhoneNum
                        + "&nDemo=" + nDemo
                        + "&fSalePrice=" + fSalePrice
                        + "&nFlag=" + nFlag
                        + "&szKey=" + key
        ).toLowerCase();
        if (!StringUtils.equals(sign, szVerifyString)) {
            logger.warn("sign check error");
            return "fail";
        }


        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(szOrderId);
        responseOrder.setResponseCode(nFlag);

        logger.info("XuanJieController callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId, responseOrder);
        return "ok";
    }


}
