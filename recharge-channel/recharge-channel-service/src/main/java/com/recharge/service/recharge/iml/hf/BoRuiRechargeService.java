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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author user
 * @create 2020/11/6 10:56
 */
@Service
public class BoRuiRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String Account = configJSONObject.getString("account");
        String url = configJSONObject.getString("url");
        String key = configJSONObject.getString("key");
        String Action = configJSONObject.getString("recharge");
        String V = configJSONObject.getString("V");
        String OutTradeNo = channelOrder.getChannelOrderId();
        String BackUrl = configJSONObject.getString("callback");
        String Mobile = huaFeiRechargeInfoBean.getPhone();
        String Package = huaFeiRechargeInfoBean.getAmt().toString();
        String Sign = DigestUtils.md5Hex("account=" + Account + "&mobile=" + Mobile + "&package=" + Package + "&key=" + key).toLowerCase();
        Map<String, String> map = new HashMap<>();
        map.put("Action", Action);
        map.put("V", V);
        map.put("OutTradeNo", OutTradeNo);
        map.put("BackUrl", BackUrl);
        map.put("Account", Account);
        map.put("Mobile", Mobile);
        map.put("Package", Package);
        map.put("Sign", Sign);
        try {
            logger.info("boRui send recharge request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("boRui send recharge response :{}", responseBody);
            String code = JSONObject.parseObject(responseBody).getString("Code");
            if (code.equals("0")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
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
        String Action = configJSONObject.getString("query");
        String V = configJSONObject.getString("V");
        String Account = configJSONObject.getString("account");
        String TaskID = "";
        String OutTradeNo = channelOrder.getChannelOrderId();
        String key = configJSONObject.getString("key");
        String url = configJSONObject.getString("url");
        String SendTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String Sign = DigestUtils.md5Hex("account=" + Account + "&key=" + key);

        Map<String, String> map = new HashMap<>();
        map.put("Action", Action);
        map.put("V", V);
        map.put("Account", Account);
        map.put("TaskID", TaskID);
        map.put("OutTradeNo", OutTradeNo);
        map.put("SendTime", SendTime);
        map.put("Sign", Sign);
        try {
            logger.info("boRui send query request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("boRui send query response :{}", responseBody);
            String Reports = JSONObject.parseObject(responseBody).getString("Reports");
            JSONArray createArray = JSONArray.parseArray(Reports);
            String info = String.valueOf(createArray.get(0));
            String Status = JSONObject.parseObject(info).getString("Status");
            if (Status.equals("2")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else if (Status.equals("3") || Status.equals("5")) {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            } else if (Status.equals("4")) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
            }
        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }


    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("4", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("5", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "未知订单");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("url");
        String Action = configJSONObject.getString("balance");
        String V = configJSONObject.getString("V");
        String Account = configJSONObject.getString("account");
        String key = configJSONObject.getString("key");
        String sign = DigestUtils.md5Hex("account=" + Account + "&key=" + key).toLowerCase();
        Map<String, String> map = new HashMap<>();
        map.put("Action", Action);
        map.put("V", V);
        map.put("Account", Account);
        map.put("Sign", sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            String balance = JSONObject.parseObject(responseBody).getString("Balance");
            return new BigDecimal(balance);
        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }
    }

}
