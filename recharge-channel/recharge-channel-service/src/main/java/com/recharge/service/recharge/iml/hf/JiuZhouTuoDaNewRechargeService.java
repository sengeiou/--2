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
 * @create 2021/3/16 13:19
 */
@Service
public class JiuZhouTuoDaNewRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        //订单号
        String OrderId = channelOrder.getChannelOrderId();
        //充值账号
        String Account = huaFeiRechargeInfoBean.getPhone();
        //产品ID
        String ProductId = "";
        String ShopId = configJSONObject.getString("ShopId");
        String UserId = configJSONObject.getString("UserId");
        //提交数量
        String Num = "1";
        //时间戳
        String Timestamp = String.valueOf(System.currentTimeMillis());
        //回调地址
        String NotifyUrl = configJSONObject.getString("notifyUrl");
        //省份
        String Province = "";
        //充值金额
        String Amount = huaFeiRechargeInfoBean.getAmt().toString();
        //顶单类型
        String ProductType = "1";
        //运营商
        String IspType = "";
        if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "联通")) {
            IspType = "1";
        } else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "移动")) {
            IspType = "0";
        } else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "电信")) {
            IspType = "2";
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "运营商获取异常");
        }

        String key = configJSONObject.getString("key");
        //签名
        String Sign = DigestUtils.md5Hex(ShopId + UserId + ProductType + OrderId + Account + Amount + Num + Timestamp + key);

        Map<String, String> map = new HashMap<>();
        map.put("OrderId", OrderId);
        map.put("Account", Account);
        map.put("ShopId", ShopId);
        map.put("UserId", UserId);
        map.put("Num", Num);
        map.put("Timestamp", Timestamp);
        map.put("NotifyUrl", NotifyUrl);
        map.put("Sign", Sign);
        map.put("Province", Province);
        map.put("Amount", Amount);
        map.put("ProductType", ProductType);
        map.put("IspType", IspType);
        String url = configJSONObject.getString("rechargeUrl");
        try {
            logger.info("jztd send recharge request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("jztd send recharge response :{}", responseBody);
            String code = JSONObject.parseObject(responseBody).getString("Code");
            if (code.equals("10012")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (code.equals("10009") || code.equals("19999")) {
                return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
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

        String ShopId = configJSONObject.getString("ShopId");
        String userId = configJSONObject.getString("UserId");
        String OrderId = channelOrder.getChannelOrderId();
        String Timestamp = String.valueOf(System.currentTimeMillis());
        String Key = configJSONObject.getString("key");
        String Sign = DigestUtils.md5Hex(ShopId + userId + OrderId + Timestamp + Key);
        Map<String, String> map = new HashMap<>();
        map.put("ShopId", ShopId);
        map.put("userId", userId);
        map.put("OrderId", OrderId);
        map.put("Timestamp", Timestamp);
        map.put("Sign", Sign);

        String url = configJSONObject.getString("queryUrl");
        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);
            String State = JSONObject.parseObject(responseBody).getString("State");
            if (State.equals("4")) {
                String VoucherContent = JSONObject.parseObject(responseBody).getString("VoucherContent");
                if (!StringUtils.isEmpty(VoucherContent)) {
                    channelOrder.setOutChannelOrderId(VoucherContent);
                }
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (State.equals("5")) {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
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
        if (StringUtils.equals("4", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("5", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "订单回调状态未知");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String ShopId = configJSONObject.getString("ShopId");
        String UserId = configJSONObject.getString("UserId");
        String Timestamp = String.valueOf(System.currentTimeMillis());
        String key = configJSONObject.getString("key");
        String Sign = DigestUtils.md5Hex(ShopId + UserId + Timestamp + key);
        Map<String, String> map = new HashMap<>();
        map.put("ShopId", ShopId);
        map.put("UserId", UserId);
        map.put("Timestamp", Timestamp);
        map.put("Sign", Sign);
        String url = configJSONObject.getString("queryBalanceUrl");
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            String Balance = JSONObject.parseObject(responseBody).getString("Balance");
            return new BigDecimal(Balance);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }


}
