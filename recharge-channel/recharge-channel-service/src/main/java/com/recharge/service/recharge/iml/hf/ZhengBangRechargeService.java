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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2021/3/11 17:49
 */
@Service
public class ZhengBangRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String agentAcct = configJSONObject.getString("userid");
        String key = configJSONObject.getString("key");
        String orderId = channelOrder.getChannelOrderId();
        String bizType = configJSONObject.getString("bizType");
        String phoneNum = huaFeiRechargeInfoBean.getPhone();
        String phoneType = "";
        if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "联通")) {
            phoneType = "UM";
        } else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "移动")) {
            phoneType = "CM";
        } else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "电信")) {
            phoneType = "TM";
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "运营商获取异常");

        }
        String amount = huaFeiRechargeInfoBean.getAmt().toString();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String a = "agentAcct=" + agentAcct + "&amount=" + amount + "&bizType=" + bizType + "&orderId=" + orderId + "&phoneNum=" + phoneNum + "&phoneType=" + phoneType + "&timestamp=" + timestamp + "&key=" + key;
        System.out.println(a);
        String sign = DigestUtils.md5Hex(a);
        Map<String, String> map = new HashMap();
        map.put("agentAcct", agentAcct);
        map.put("orderId", orderId);
        map.put("bizType", bizType);
        map.put("phoneNum", phoneNum);
        map.put("phoneType", phoneType);
        map.put("amount", amount);
        map.put("timestamp", timestamp);
        map.put("sign", sign);
        try {
            logger.info("zhengBang send recharge request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("zhengBang send recharge response :{}", responseBody);
            String code = JSONObject.parseObject(responseBody).getString("retCode");
            String msg = JSONObject.parseObject(responseBody).getString("retMsg");
            if (code.equals("SUCCESS")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, msg);
            }
        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.UNKOWN, "订单请求超时");
        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String agentAcct = configJSONObject.getString("userid");
        String orderId = channelOrder.getChannelOrderId();
        String key = configJSONObject.getString("key");
        String sign = DigestUtils.md5Hex("agentAcct=" + agentAcct + "&orderId=" + orderId + "&key=" + key);
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("agentAcct", agentAcct);
        requestMap.put("orderId", orderId);
        requestMap.put("sign", sign);
        String queryUrl = configJSONObject.getString("queryUrl");
        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            String status = JSONObject.parseObject(responseBody).getString("status");
            String msg = JSONObject.parseObject(responseBody).getString("retMsg");

            if (StringUtils.equals(status, "S")) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals(status, "P")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else if (StringUtils.equals(status, "F")) {
                return new ProcessResult(ProcessResult.FAIL, msg);
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "未知订单");
            }

        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("S", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("P", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        } else if (StringUtils.equals("F", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "订单异常");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String agentAcct = configJSONObject.getString("userid");
        String key = configJSONObject.getString("key");
        String sign = DigestUtils.md5Hex("agentAcct=" + agentAcct + "&key=" + key);
        Map map = new HashMap();
        map.put("agentAcct", agentAcct);
        map.put("sign", sign);
        String url = configJSONObject.getString("queryBalanceUrl");
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            String balance = JSONObject.parseObject(responseBody).getString("balance");
            return new BigDecimal(balance);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }

    }


}

