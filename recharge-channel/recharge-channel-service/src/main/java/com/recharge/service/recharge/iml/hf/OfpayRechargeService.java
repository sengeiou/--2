package com.recharge.service.recharge.iml.hf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.HuaFeiRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by CAOQI on 2017/4/28.
 */
@Service
public class OfpayRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        String userId = configJSONObject.getString("userId");
        String userPws = configJSONObject.getString("userPws");
        String requestUrl = configJSONObject.getString("rechargeUrl");
        String callBackUrl = configJSONObject.getString("callBackUrl");
        String md5Key = configJSONObject.getString("md5Key");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid", userId);
        requestMap.put("userpws", DigestUtils.md5Hex(userPws));

//        现在只用快充
        requestMap.put("cardid", "140101");

        requestMap.put("cardnum", huaFeiRechargeInfoBean.getAmt().toString());

        requestMap.put("sporder_id", channelOrder.getChannelOrderId());
        requestMap.put("sporder_time", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        requestMap.put("game_userid", huaFeiRechargeInfoBean.getPhone());
        String md5Source = requestMap.get("userid") +
                requestMap.get("userpws") +
                requestMap.get("cardid") +
                requestMap.get("cardnum") +
                requestMap.get("sporder_id") +
                requestMap.get("sporder_time") +
                requestMap.get("game_userid");
        requestMap.put("md5_str", DigestUtils.md5Hex(md5Source + md5Key).toUpperCase());
        requestMap.put("ret_url", callBackUrl);
        requestMap.put("version", "6.0");


        try {
            logger.info("{},发送充值的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, requestMap, configJSONObject.getString("encoding"));
            logger.info("{},收到充值的响应:{}", rechargeOrderBean.getOrderId(), responseBody);


            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element retCode = root.element("retcode");
            if (StringUtils.equals("1", retCode.getStringValue())) {
                Element gameState = root.element("game_state");
                Element ofpayOrderId = root.element("orderid");
                channelOrder.setOutChannelOrderId(ofpayOrderId.getStringValue());
                if (StringUtils.equals("9", gameState.getStringValue())) {
                    return new ProcessResult(ProcessResult.FAIL, "提交失败");
                } else {
                    return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
                }
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }


        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String userId = configJSONObject.getString("userId");
        String queryUrl = configJSONObject.getString("queryUrl");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid", userId);
        requestMap.put("spbillid", channelOrder.getChannelOrderId());

        try {
            logger.info("{},send query param:{}", channelOrder.getOrderId(), JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, configJSONObject.getString("encoding"));
            logger.info("{},query responseBody:{}", channelOrder.getOrderId(), responseBody);

            if (StringUtils.equals("1", responseBody)) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals("9", responseBody)) {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            } else {
                return new ProcessResult(ProcessResult.PROCESSING, "处理中");
            }


        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        /*0-生成订单、1-处理中、
        2-交易成功、3-交易失败
                */
        String tranState = responseOrder.getResponseCode();
        if (StringUtils.equals("1", tranState)) {
            return new ProcessResult(ProcessResult.SUCCESS, "交易成功");
        } else if (StringUtils.equals("9", tranState)) {
            return new ProcessResult(ProcessResult.FAIL, responseOrder.getResponseMsg());
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "未知返回码");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      余额查询地址：http://AXXXX.api2.ofpay.com/queryuserinfo.do?userid=Axxxxx&userpws=xxxxxxx&version=6.0
        String userId = configJSONObject.getString("userId");
        String queryUrl = configJSONObject.getString("queryBalanceUrl");
        String userPws = configJSONObject.getString("userPws");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid", userId);
        requestMap.put("userpws", DigestUtils.md5Hex(userPws));
        requestMap.put("version", "6.0");

        try {
            logger.info("send query param:{}", JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, configJSONObject.getString("encoding"));
            logger.info("query responseBody:{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element retCode = root.element("retcode");
            if (StringUtils.equals("1", retCode.getStringValue())) {
                String balance = root.elementText("ret_leftcredit");
                return new BigDecimal(balance);
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            logger.error("send error", e);
            return BigDecimal.ZERO;
        }

    }
}
