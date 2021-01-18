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
import java.util.*;

/**
 * @author user
 * @create 2020/9/18 15:51
 */
@Service
public class YiJunRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        //会员订单号
        String OrderId = channelOrder.getChannelOrderId();
        //充值帐号
        String Account = huaFeiRechargeInfoBean.getPhone();
        //产品编号(流量,加油卡，Q币游戏订单需要)
        String ProductId = "";
        //系统编号,由我司商务提供
        String ShopId = configJSONObject.getString("shopID");
        //会员编号,由我司商务提供
        String UserId = configJSONObject.getString("UserID");
        //提交数量
        String Num = "1";
        //时间戳(Unix时间戳,毫秒)
        String Timestamp = String.valueOf(System.currentTimeMillis());
        //通知地址(可传空,不参与签名),UTF8(URL)编码后提交
        String NotifyUrl = configJSONObject.getString("callBack");

        //充值金额,单位：元(话费订单需要)
        String Amount = String.valueOf(huaFeiRechargeInfoBean.getAmt());
        //订单类型(1话费,2流量,3加油卡,4 Q币游戏)
        String ProductType = "1";
        //运营商类型(话费流量订单需要,0移动,1联通,2电信),不传则以我司业务平台为准,不参与签名
        //huaFeiRechargeInfoBean.getOperator()
        String Type;
        if (huaFeiRechargeInfoBean.getOperator().equals("移动")) {
            Type = "0";
        } else if (huaFeiRechargeInfoBean.getOperator().equals("联通")) {
            Type = "1";
        } else if (huaFeiRechargeInfoBean.getOperator().equals("电信")) {
            Type = "2";
        } else {
            return new ProcessResult(ProcessResult.PROCESSING, "运营商匹配失败");

        }

        //扩展字段,具体内容参考请求报文示例,UTF8(URL)编码后提交
        String Extension = "";

        String key = configJSONObject.getString("key");
        //签名
        //md5(shopId+userId+ProductType+orderId+account+Amount+num+timestamp+key)
        String Sign = DigestUtils.md5Hex(ShopId + UserId + ProductType + OrderId + Account + Amount + Num + Timestamp + key);
        String url = configJSONObject.getString("rechargeUrl");

        Map<String, String> map = new HashMap<>();


        map.put("OrderId", OrderId);
        map.put("Account", Account);
        map.put("ShopId", ShopId);
        map.put("UserId", UserId);
        map.put("Num", Num);
        map.put("Timestamp", Timestamp);
        map.put("NotifyUrl", NotifyUrl);
        map.put("Sign", Sign);
        map.put("Amount", Amount);
        map.put("ProductType", ProductType);
        map.put("Type", Type);
        logger.info("yiJun send recharge request params:{}", JSONObject.toJSONString(map));
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            String code = JSONObject.parseObject(responseBody).getString("Code");
            String message = JSONObject.parseObject(responseBody).getString("Message");
            if (code.equals("10012")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, message);
            }

        } catch (ConnectTimeoutException connectException) {
            logger.error("{}yiJun send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("{}yiJun send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String ShopId = configJSONObject.getString("shopID");
        String UserId = configJSONObject.getString("UserID");
        String OrderId = channelOrder.getChannelOrderId();
        String Timestamp = String.valueOf(System.currentTimeMillis());
        String key = configJSONObject.getString("key");
        String Sign = DigestUtils.md5Hex(ShopId + UserId + OrderId + Timestamp + key);
        Map<String, String> map = new HashMap<>();
        map.put("ShopId", ShopId);
        map.put("UserId", UserId);
        map.put("OrderId", OrderId);
        map.put("Timestamp", Timestamp);
        map.put("Sign", Sign);
        String url = configJSONObject.getString("queryUrl");
        logger.info("yiJun send query request params:{}", JSONObject.toJSONString(map));

        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            String state = JSONObject.parseObject(responseBody).getString("State");
            String voucherContent = JSONObject.parseObject(responseBody).getString("VoucherContent");
            if (StringUtils.equals("4", state)) {
                channelOrder.setOutChannelOrderId(voucherContent);
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals("5", state)) {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            } else if (StringUtils.equals("0", state)) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "查询未知");
            }
        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }

    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("4", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("5", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String ShopId = configJSONObject.getString("shopID");
        //会员编号,由我司商务提供
        String UserId = configJSONObject.getString("UserID");
        String Timestamp = String.valueOf(System.currentTimeMillis());
        String key = configJSONObject.getString("key");
        //md5(ShopId+ UserId+ Timestamp+ Key)
        String Sign = DigestUtils.md5Hex(ShopId + UserId + Timestamp + key);

        Map<String, String> map = new HashMap<>();
        map.put("ShopId", ShopId);
        map.put("UserId", UserId);
        map.put("Timestamp", Timestamp);
        map.put("Sign", Sign);
        String url = configJSONObject.getString("balancequeryUrl");

        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            String balance = JSONObject.parseObject(responseBody).getString("Balance");
            return new BigDecimal(balance);
        } catch (Exception e) {
            logger.error("{}yiJun send error", e);
            return BigDecimal.ZERO;
        }


    }


}
