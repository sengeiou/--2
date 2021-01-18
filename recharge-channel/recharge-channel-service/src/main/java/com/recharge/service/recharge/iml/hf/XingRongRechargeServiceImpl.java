package com.recharge.service.recharge.iml.hf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
 * @create 2020/11/18 18:34
 */
@Service
public class XingRongRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String rechargeUrl = configJSONObject.getString("rechargeUrl");
        String client_id = configJSONObject.getString("client_id");
        String client_secret = configJSONObject.getString("client_secret");
        //设置订单ID
        String out_logno = channelOrder.getChannelOrderId();
        //设置产品类型
        StringBuffer category = new StringBuffer();
        String operator = huaFeiRechargeInfoBean.getOperator();
        if (StringUtils.equals("联通", operator)) {
            category.append("2");
        }
        if (StringUtils.equals("电信", operator)) {
            category.append("3");
        }
        //设置充值电话
        String account = huaFeiRechargeInfoBean.getPhone();
        //设置充值面额
        String money = huaFeiRechargeInfoBean.getAmt().toString();
        Map<String, String> requestMap = new HashMap<String, String>();
        Map<String, String> bodyHashMap = new HashMap<>();
        bodyHashMap.put("out_logno", out_logno);
        bodyHashMap.put("category", category.toString());
        bodyHashMap.put("account", account);
        bodyHashMap.put("money", money);
        requestMap.put("client_id", client_id);
        requestMap.put("type", "create");
        requestMap.put("data_type", "JSON");
        requestMap.put("body", JSONObject.toJSONString(bodyHashMap));
        requestMap.put("sign", DigestUtils.md5Hex(client_secret +
                "account" + account +
                "category" + category +
                "money" + money +
                "out_logno" + out_logno +
                client_secret).toUpperCase());
        try {
            logger.info("xinRong send recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(rechargeUrl, requestMap, "utf-8", 5000);
            logger.info("xinRong send recharge response :{}", responseBody);
            String code = JSONObject.parseObject(responseBody).getString("code");
            String message = JSONObject.parseObject(responseBody).getString("message");
            if (StringUtils.equals(code, "200")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, message);
            }
        } catch (ConnectTimeoutException connectException) {
            logger.error("xinRong {}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("xinRong {}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryUrl = configJSONObject.getString("queryUrl");
        String client_id = configJSONObject.getString("client_id");
        String client_secret = configJSONObject.getString("client_secret");
        //设置订单ID
        String out_logno = channelOrder.getChannelOrderId();
        Map<String, String> requestMap = new HashMap<String, String>();
        Map<String, String> bodyHashMap = new HashMap<>();
        bodyHashMap.put("out_logno", out_logno);
        requestMap.put("client_id", client_id);
        requestMap.put("type", "query");
        requestMap.put("data_type", "JSON");
        requestMap.put("body", JSONObject.toJSONString(bodyHashMap));
        requestMap.put("sign", DigestUtils.md5Hex(client_secret + "out_logno" + out_logno + client_secret).toUpperCase());
        try {
            logger.info("xinRong send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("xinRong send query response :{}", responseBody);
            String data = JSONObject.parseObject(responseBody).getString("data");
            String code = JSONObject.parseObject(responseBody).getString("code");
            JSONObject dataArray = JSONArray.parseObject(data);
            String status = String.valueOf(dataArray.get("status"));
            if (StringUtils.equals(code, "200")) {
                if (StringUtils.equals(status, "1")) {
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (StringUtils.equals(status, "2")) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else if (StringUtils.equals(status, "0")) {
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                } else {
                    return new ProcessResult(ProcessResult.PROCESSING, "等待充值");
                }
            } else {
                return new ProcessResult(ProcessResult.PROCESSING, "等待充值");
            }
        } catch (Exception e) {
            logger.error("xinRong {}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("0", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        } else {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryBalanceUrl = configJSONObject.getString("queryBalanceUrl");
        String client_id = configJSONObject.getString("client_id");
        String client_secret = configJSONObject.getString("client_secret");
        Map<String, String> requestMap = new HashMap<String, String>();
        Map<String, String> bodyHashMap = new HashMap<>();
        bodyHashMap.put("query_type", "1");
        requestMap.put("client_id", client_id);
        requestMap.put("type", "checkaccount");
        requestMap.put("data_type", "JSON");
        requestMap.put("body", JSONObject.toJSONString(bodyHashMap));
        requestMap.put("sign", DigestUtils.md5Hex(client_secret + "query_type" + "1" + client_secret).toUpperCase());
        try {
            logger.info("xinRong send queryBalance request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryBalanceUrl, requestMap, "utf-8", 5000);
            logger.info("xinRong send queryBalance response :{}", responseBody);
            String data = JSONObject.parseObject(responseBody).getString("data");
            JSONObject dataArray = JSONArray.parseObject(data);
            String balance = String.valueOf(dataArray.get("balance"));
            return new BigDecimal(balance);
        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }
    }
}
