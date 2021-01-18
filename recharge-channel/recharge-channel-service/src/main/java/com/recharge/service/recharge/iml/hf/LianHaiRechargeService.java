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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class LianHaiRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      请求地址：http://feiyu.lianhaikeji.com/fish/api/createOrder
        String url = configJSONObject.getString("url");
        String appKey = configJSONObject.getString("appKey");
        String secretKey = configJSONObject.getString("secretKey");
        String notifyUrl = configJSONObject.getString("notifyUrl");


        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("out_trade_id", channelOrder.getChannelOrderId());
        requestMap.put("notify_url", notifyUrl);
        requestMap.put("timestamp", String.valueOf(new Date().getTime() / 1000));


        requestMap.put("product", "302");
//        规格编码，具体咨询商务获取（直充类测试商品规格：1，卡密类测试商品规格：test）
        requestMap.put("sub_code", "lhsz-" + huaFeiRechargeInfoBean.getAmt());


        requestMap.put("account", huaFeiRechargeInfoBean.getPhone());
        requestMap.put("appKey", appKey);
        requestMap.put("customerA", "");
        requestMap.put("customerB", "");


        requestMap.put("sign", DigestUtils.md5Hex(
                requestMap.get("account") +
                        appKey +
                        requestMap.get("customerA") +
                        requestMap.get("customerB") +
                        requestMap.get("notify_url") +
                        requestMap.get("out_trade_id") +
                        requestMap.get("product") +
                        secretKey +
                        requestMap.get("sub_code") +
                        requestMap.get("timestamp")
        ).toLowerCase());


        try {
            logger.info("send recharge request params:{}", requestMap);
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8");
            logger.info("send recharge response :{}", responseBody);

            String code = JSONObject.parseObject(responseBody).getString("code");
            if (StringUtils.equals(code, "1")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }

        } catch (Exception e) {
            logger.error("{}send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      请求地址：http://feiyu.lianhaikeji.com/fish/api/queryOrder
        String url = configJSONObject.getString("queryUrl");
        String appKey = configJSONObject.getString("appKey");
        String secretKey = configJSONObject.getString("secretKey");


        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("out_trade_id", channelOrder.getChannelOrderId());
        requestMap.put("timestamp", String.valueOf(new Date().getTime() / 1000));
        requestMap.put("appKey", appKey);
        requestMap.put("sign", DigestUtils.md5Hex(
                appKey +
                        requestMap.get("out_trade_id") +
                        secretKey +
                        requestMap.get("timestamp")
        ).toLowerCase());

        try {
            logger.info("send query request params:{}", requestMap);
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8");
            logger.info("send query response :{}", responseBody);

            String code = JSONObject.parseObject(responseBody).getString("code");
            JSONObject bodyObj = JSONObject.parseObject(responseBody).getJSONObject("body");
            if (StringUtils.equals("1", code)) {
//             订单状态：0-正在充值 1-成功 2-失败
                String orderRes = bodyObj.getString("status");
                if (StringUtils.equals("1", orderRes)) {
                    String supplierRemark = bodyObj.getString("supplierRemark");
                    channelOrder.setOutChannelOrderId(supplierRemark);
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (StringUtils.equals("2", orderRes)) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else {
                    return new ProcessResult(ProcessResult.PROCESSING, "处理中");
                }
            } else {
                String msg = JSONObject.parseObject(responseBody).getString("msg");
                return new ProcessResult(ProcessResult.PROCESSING, msg);
            }

        } catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "处理中");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
//             交易结果,交易成功为 “success”,其他为失败
        if (StringUtils.equals("success", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      请求地址：http://feiyu.lianhaikeji.com/fish/api/balance
        String url = configJSONObject.getString("queryBalanceUrl");
        String appKey = configJSONObject.getString("appKey");
        String secretKey = configJSONObject.getString("secretKey");


        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("appKey", appKey);
        requestMap.put("timestamp", String.valueOf(new Date().getTime() / 1000));

        requestMap.put("sign",
                DigestUtils.md5Hex(appKey + secretKey + requestMap.get("timestamp")).toLowerCase());

        try {
            logger.info("send queryBalance request params:{}", requestMap);
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8");

            logger.info("send queryBalance response :{}", responseBody);

            String code = JSONObject.parseObject(responseBody).getString("code");
            if (StringUtils.equals(code, "1")) {
                String balance = JSONObject.parseObject(responseBody).getString("body");
                return new BigDecimal(balance).divide(new BigDecimal("100"));
            } else {
                return BigDecimal.ZERO;
            }

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

}
