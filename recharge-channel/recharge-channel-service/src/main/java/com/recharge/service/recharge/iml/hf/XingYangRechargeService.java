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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Administrator
 * @create 2021/1/5 13:05
 */
@Service
public class XingYangRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String ShopId = configJSONObject.getString("ShopId");
        String UserId = configJSONObject.getString("UserId");
        String Timestamp = Long.toString(System.currentTimeMillis());
        String Key = configJSONObject.getString("Key");
        String OrderId = channelOrder.getChannelOrderId();
        String Account = huaFeiRechargeInfoBean.getPhone();
        String NotifyUrl = configJSONObject.getString("notifyUrl");
        BigDecimal amt = huaFeiRechargeInfoBean.getAmt();
        String Num;
        String Amount;
        if(amt.intValue()<10){
            Num = amt.toString();
            Amount = "1";
        }else {
            Num = "1";
            Amount = huaFeiRechargeInfoBean.getAmt().toString();
        }
        String ProductType = "1";
        String Sign = DigestUtils.md5Hex(ShopId + UserId + ProductType + OrderId + Account + Amount + Num + Timestamp + Key);
        Map<String, String> map = new TreeMap<String, String>();
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
        try {
            logger.info("山东信扬发送充值订单请求参数{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("山东信扬发送充值订单接收参数{}", JSONObject.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("Code");
            if (StringUtils.equals("10012", code)) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals("10009", code)) {
                return new ProcessResult(ProcessResult.UNKOWN, "订单号已存在");
            } else if (StringUtils.equals("19999", code)) {
                return new ProcessResult(ProcessResult.UNKOWN, "供货商系统错误，找供货商核实");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (Exception e) {
            logger.error("山东信扬发送充值订单{}报错{}", rechargeOrderBean.getOrderId(), e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "发送充值出错：" + e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("queryUrl");
        String ShopId = configJSONObject.getString("ShopId");
        String UserId = configJSONObject.getString("UserId");
        String Timestamp = Long.toString(System.currentTimeMillis());
        String Key = configJSONObject.getString("Key");
        String OrderId = channelOrder.getChannelOrderId();
        String Sign = DigestUtils.md5Hex(ShopId + UserId + OrderId + Timestamp + Key);
        Map<String, String> map = new TreeMap<String, String>();
        map.put("ShopId", ShopId);
        map.put("UserId", UserId);
        map.put("Timestamp", Timestamp);
        map.put("OrderId", OrderId);
        map.put("Sign", Sign);
        try {
            logger.info("山东信扬查询订单请求参数{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("山东信扬查询订单请求参数{}", JSONObject.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("Code");
            if (StringUtils.equals("10023", code)) {
                String state = JSONObject.parseObject(responseBody).getString("State");
                if (StringUtils.equals("0", state)) {
                    return new ProcessResult(ProcessResult.PROCESSING, "处理中");
                } else if (StringUtils.equals("4", state)) {
                    String voucherType = JSONObject.parseObject(responseBody).getString("VoucherType");
                    if (StringUtils.isNotBlank(voucherType)) {
                        String voucherContent = JSONObject.parseObject(responseBody).getString("VoucherContent");
                        channelOrder.setOutChannelOrderId(voucherContent);
                    }
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (StringUtils.equals("5", state)) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else {
                    return new ProcessResult(ProcessResult.UNKOWN, "查询异常");
                }
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "查询异常");
            }
        } catch (Exception e) {
            logger.error("山东信扬查询订单{}报错{}", OrderId, e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "查询异常" + e.getMessage());
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("4", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("5", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("balanceQueryUrl");
        String ShopId = configJSONObject.getString("ShopId");
        String UserId = configJSONObject.getString("UserId");
        String Timestamp = Long.toString(System.currentTimeMillis());
        String Key = configJSONObject.getString("Key");
        String Sign = DigestUtils.md5Hex(ShopId + UserId + Timestamp + Key);
        Map<String, String> map = new TreeMap<String, String>();
        map.put("ShopId", ShopId);
        map.put("UserId", UserId);
        map.put("Timestamp", Timestamp);
        map.put("Sign", Sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            String code = JSONObject.parseObject(responseBody).getString("Code");
            if (StringUtils.equals("10013", code)) {
                String balance = JSONObject.parseObject(responseBody).getString("Balance");
                return new BigDecimal(balance);
            }
            System.out.println(responseBody);
            return BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Test
    void test() {
        Channel channel = new Channel();
        ChannelOrder channelOrder = new ChannelOrder();
        channel.setConfigInfo("{\"ShopId\": \"300000\",\"UserId\": \"100017\",\"Key\": \"497C943FEE4D5DFB2CB371D3202CF1D8\",\"rechargeUrl\": \"http://47.105.185.167:8003/v1.0/Pay.ashx\",\"balanceQueryUrl\": \"http://47.105.185.167:8003/v1.0/QueryBalance.ashx\",\"notifyUrl\": \"http://139.129.85.83:8082/xingYang/callBack\",\"queryUrl\": \"http://47.105.185.167:8003/v1.0/QueryOrder.ashx\"}");
        channelOrder.setChannelOrderId("cs202101061347");
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult query = query(channel, channelOrder);
        System.out.println("z");
    }
}
