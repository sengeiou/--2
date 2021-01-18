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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Administrator
 * @create 2020/3/24 9:09
 */
@Service
public class ZhiXinRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        //请求参数
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("url");
        String mrch_no = configJSONObject.getString("userId");
        String secret_key = configJSONObject.getString("secretKey");
        String notify_url = configJSONObject.getString("callback");
        String request_time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String product_type = "1";
        String phone_no = huaFeiRechargeInfoBean.getPhone();
        String cp = "";
        String city_code = "";
        BigDecimal recharge_amount = huaFeiRechargeInfoBean.getAmt().multiply(new BigDecimal(100));
        String recharge_type = "0";
        String recharge_desc = "";

        //签名
        Map<String, String> signMap = new TreeMap<>();
        signMap.put("mrch_no", mrch_no);
        signMap.put("request_time", request_time);
        signMap.put("client_order_no", channelOrder.getChannelOrderId());
        signMap.put("product_type", product_type);
        signMap.put("phone_no", phone_no);
        signMap.put("cp", cp);
        signMap.put("city_code", city_code);
        signMap.put("recharge_amount", recharge_amount.toString());
        signMap.put("recharge_type", recharge_type);
        signMap.put("recharge_desc", recharge_desc);
        signMap.put("notify_url", notify_url);


        StringBuffer signSource = new StringBuffer();
        Iterator iter = signMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String value = (String) entry.getValue();
            String key = (String) entry.getKey();
            signSource.append(key + value);
        }
        signSource.append(secret_key);
        String sign = DigestUtils.md5Hex(signSource.toString());
        signMap.put("sign", sign.toLowerCase());
        String requestString = JSONObject.toJSONString(signMap);

        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(signMap));
            String responseBody = HttpClientUtils.invokePostString(url, new StringEntity(requestString), "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            String code = JSONObject.parseObject(responseBody).getString("code");
            String msg = JSONObject.parseObject(responseBody).getString("message");
            if (StringUtils.equals(code, "2")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, msg);
            }
        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryUrl = configJSONObject.getString("queryUrl");
        String mrch_no = configJSONObject.getString("userId");
        String request_time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String client_order_no = channelOrder.getChannelOrderId();
        String secret_key = configJSONObject.getString("secretKey");
        //签名
        Map<String, String> signMap = new TreeMap<>();
        signMap.put("mrch_no", mrch_no);
        signMap.put("request_time", request_time);
        signMap.put("client_order_no", client_order_no);
        signMap.put("order_time", new SimpleDateFormat("yyyyMMddHHmmss").format(channelOrder.getOrderTime()));

        StringBuffer signSource = new StringBuffer();
        Iterator iter = signMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String value = (String) entry.getValue();
            String key = (String) entry.getKey();
            signSource.append(key + value);
        }
        signSource.append(secret_key);
        String sign = DigestUtils.md5Hex(signSource.toString());
        signMap.put("sign", sign.toLowerCase());
        String requestString = JSONObject.toJSONString(signMap);

        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(signMap));
            String responseBody = HttpClientUtils.invokePostString(queryUrl, new StringEntity(requestString), "utf-8", 5000);
            logger.info("send query response :{}", responseBody);
            //返回报文
            String code = JSONObject.parseObject(responseBody).getString("code");
            JSONObject bodyObj = JSONObject.parseObject(responseBody).getJSONObject("data");
            if (StringUtils.equals(code, "2")) {
                String orderRes = bodyObj.getString("recharge_status");
                if (StringUtils.equals(orderRes, "2")) {
                    String elecardID = bodyObj.getString("elecardID");
                    channelOrder.setOutChannelOrderId(elecardID);
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (StringUtils.equals(orderRes, "6")) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else {
                    return new ProcessResult(ProcessResult.PROCESSING, "处理中");
                }
            } else {
                String msg = JSONObject.parseObject(responseBody).getString("msg");
                return new ProcessResult(ProcessResult.PROCESSING, msg);
            }
        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "处理中");
        }

    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("6", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
        }

    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        //请求参数
        String queryBalanceUrl = configJSONObject.getString("queryBalanceUrl");
        String mrch_no = configJSONObject.getString("userId");
        String request_time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String secret_key = configJSONObject.getString("secretKey");
        Map<String, String> signMap = new TreeMap<>();
        signMap.put("mrch_no", mrch_no);
        signMap.put("request_time", request_time);
        //签名
        StringBuffer signSource = new StringBuffer();
        Iterator iter = signMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String value = (String) entry.getValue();
            String key = (String) entry.getKey();
            signSource.append(key + value);

        }
        signSource.append(secret_key);
        String sign = DigestUtils.md5Hex(signSource.toString());

        signMap.put("sign", sign.toLowerCase());
        String requestString = JSONObject.toJSONString(signMap);
        try {
            logger.info("send queryBalance request params:{}", JSONObject.toJSONString(signMap));
            String responseBody = HttpClientUtils.invokePostString(queryBalanceUrl, new StringEntity(requestString), "utf-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);
            String code = JSONObject.parseObject(responseBody).getString("code");
            JSONObject bodyObj = JSONObject.parseObject(responseBody).getJSONObject("data");
            if (StringUtils.equals(code, "2")) {
                String balance = bodyObj.getString("balance");
                return new BigDecimal(balance);
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }

    }

}
