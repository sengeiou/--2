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
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class TongYunRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");
        String callback = configJSONObject.getString("callback");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("OrderId", channelOrder.getChannelOrderId());
        requestMap.put("Account", huaFeiRechargeInfoBean.getPhone());
//        requestMap.put("ProductId",null);
        requestMap.put("ShopId", "300000");
        requestMap.put("UserId", userId);
        requestMap.put("Num", "1");
        requestMap.put("Timestamp", System.currentTimeMillis() + "");
        requestMap.put("NotifyUrl", callback);
        requestMap.put("Amount", huaFeiRechargeInfoBean.getAmt().toString());
        requestMap.put("ProductType", "1");
        requestMap.put("Sign", DigestUtils.md5Hex(requestMap.get("ShopId") + userId + requestMap.get("ProductType")
                + requestMap.get("OrderId") + requestMap.get("Account") + requestMap.get("Amount") + 1 + requestMap.get("Timestamp") + key));
        try {
            logger.info("send recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            String code = jsonObject.getString("Code");
            String message = jsonObject.getString("Message");
            if (StringUtils.equals(code, "10012")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, message);
            }
        }  catch (Exception e) {
            logger.error("{}send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String queryUrl = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("OrderId", channelOrder.getChannelOrderId());
//        requestMap.put("ProductId",null);
        requestMap.put("ShopId", "300000");
        requestMap.put("userId", userId);
        requestMap.put("Num", "1");
        requestMap.put("Timestamp", System.currentTimeMillis() + "");
        requestMap.put("Sign", DigestUtils.md5Hex(requestMap.get("ShopId") + userId
                + requestMap.get("OrderId") + requestMap.get("Timestamp") + key));
        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            String code = jsonObject.getString("Code");
            String message = jsonObject.getString("Message");
            String state = jsonObject.getString("State");
            if (StringUtils.equals(code, "10023")) {
                if (StringUtils.equals(state, "0")) {
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                } else if (StringUtils.equals(state, "4")) {
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (StringUtils.equals(state, "5")) {
                    return new ProcessResult(ProcessResult.FAIL, "充值成功");
                }
                return new ProcessResult(ProcessResult.UNKOWN, "查询结果可疑");
            } else if (StringUtils.equals(code, "1000")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, message);
            }
        }  catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(), "4")) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "5")) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }
        return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      余额查询地址：http://api.sale.tongyun188.com/v1.0/QueryBalance.ashx
        String queryUrl = configJSONObject.getString("queryBalanceUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("ShopId", "300000");
        requestMap.put("UserId", userId);
        requestMap.put("Timestamp", System.currentTimeMillis() + "");
        requestMap.put("Sign", DigestUtils.md5Hex(requestMap.get("ShopId") + userId
                + requestMap.get("Timestamp") + key));
        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            String code = jsonObject.getString("Code");
            if (StringUtils.equals(code, "10013")) {
                String balance = jsonObject.getString("Balance");
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
