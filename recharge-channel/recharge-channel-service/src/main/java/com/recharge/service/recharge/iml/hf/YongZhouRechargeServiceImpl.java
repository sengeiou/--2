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
import java.util.*;

/**
 * @author Administrator
 * @create 2021/3/8 11:13
 */
@Service
public class YongZhouRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private List<String> rechargeErrorCode = Arrays.asList("10000", "10001", "10010", "10003", "10004", "10005",
            "10006", "10007", "10008", "10011", "10016", "10018", "10019");

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String ShopId = configJSONObject.getString("shopID");
        String UserId = configJSONObject.getString("UserID");
        String key = configJSONObject.getString("key");
        String OrderId = channelOrder.getChannelOrderId();
        String Account = huaFeiRechargeInfoBean.getPhone();
        String Num = "1";
        String Timestamp = String.valueOf(System.currentTimeMillis());
        String NotifyUrl = configJSONObject.getString("notify_url");
        String Amount = huaFeiRechargeInfoBean.getAmt().toString();
        String ProductType = "1";
        String Sign= DigestUtils.md5Hex(ShopId+UserId+ProductType+OrderId+Account+Amount+Num+Timestamp+key).toUpperCase();
        TreeMap<String, String> requestMap = new TreeMap<>();
        requestMap.put("OrderId",OrderId);
        requestMap.put("Account",Account);
        requestMap.put("ShopId",ShopId);
        requestMap.put("UserId",UserId);
        requestMap.put("Num",Num);
        requestMap.put("Timestamp",Timestamp);
        requestMap.put("NotifyUrl",NotifyUrl);
        requestMap.put("Sign",Sign);
        requestMap.put("Amount",Amount);
        requestMap.put("ProductType",ProductType);
        try {
            logger.info("{}泳州,发送充值的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url,requestMap,"utf-8", 5000);
            logger.info("{}泳州,接收充值的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(responseBody));
            String Code = JSONObject.parseObject(responseBody).getString("Code");
            if (StringUtils.equals("10012",Code)||StringUtils.equals("10009",Code)){
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            }else if(rechargeErrorCode.contains(Code)){
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }else {
                return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
            }
        } catch (Exception e) {
            logger.error("泳州订单号: {} 下单接口报错：{}", rechargeOrderBean.getOrderId(), e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("queryUrl");
        String ShopId = configJSONObject.getString("shopID");
        String UserId = configJSONObject.getString("UserID");
        String key = configJSONObject.getString("key");
        String OrderId = channelOrder.getChannelOrderId();
        String Timestamp = String.valueOf(System.currentTimeMillis());
        String Sign = DigestUtils.md5Hex(ShopId+UserId+OrderId+Timestamp+key);
        TreeMap<String, String> requestMap = new TreeMap<>();
        requestMap.put("ShopId",ShopId);
        requestMap.put("UserId",UserId);
        requestMap.put("OrderId",OrderId);
        requestMap.put("Timestamp",Timestamp);
        requestMap.put("Sign",Sign);
        try {
            logger.info("{}泳州,请求查单的参数:{}", OrderId, JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url,requestMap,"utf-8", 5000);
            logger.info("{}泳州,接收查单的参数:{}", OrderId, JSON.toJSONString(responseBody));
            String Code = JSONObject.parseObject(responseBody).getString("Code");
            String Message = JSONObject.parseObject(responseBody).getString("Message");
            if (StringUtils.equals("10023",Code)){
                String State = JSONObject.parseObject(responseBody).getString("State");
                if (StringUtils.equals("4",State)){
                    String VoucherContent = JSONObject.parseObject(responseBody).getString("VoucherContent");
                    if (!StringUtils.isEmpty(VoucherContent)){
                        channelOrder.setOutChannelOrderId(VoucherContent);
                    }
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                }else if(StringUtils.equals("0",State)){
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                }else if(StringUtils.equals("5",State)){
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                }else {
                    return new ProcessResult(ProcessResult.UNKOWN, "查询未知："+Message);
                }
            }else {
                return new ProcessResult(ProcessResult.UNKOWN, "查询未知："+Message);
            }
        } catch (Exception e) {
            logger.info("{}泳州,查单接口报错的参数:{}", OrderId, e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "查询报错："+e.getMessage());
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
        String shopID = configJSONObject.getString("shopID");
        String UserID = configJSONObject.getString("UserID");
        String key = configJSONObject.getString("key");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String sign= DigestUtils.md5Hex(shopID+ UserID+ timestamp+ key).toUpperCase();
        LinkedHashMap<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("shopID",shopID);
        requestMap.put("UserID",UserID);
        requestMap.put("Timestamp",timestamp);
        requestMap.put("sign",sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url,requestMap,"utf-8", 5000);
            String Code = JSONObject.parseObject(responseBody).getString("Code");
            if (StringUtils.equals("10013",Code)){
                String Balance = JSONObject.parseObject(responseBody).getString("Balance");
                return new BigDecimal(Balance);
            }else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    @Test
    void test(){
        ChannelOrder channelOrder = new ChannelOrder();
        Channel channel = new Channel();
        channelOrder.setChannelOrderId("cs202103081407");
        channel.setConfigInfo("{rechargeUrl:\"http://api.sale.rongyuanruitian.com/v1.0/Pay.ashx\",queryUrl:\"http://api.sale.rongyuanruitian.com/v1.0/QueryOrder.ashx\",balanceQueryUrl:\"http://api.sale.rongyuanruitian.com/v1.0/QueryBalance.ashx\",shopID:\"300000\",UserID:\"100878\",key:\"84CE0687DF23C5CE1734B7F2E494E299\",notify_url:\"http://115.28.88.114:8083/YongZhou/callback\"}");
//        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
        ProcessResult query = query(channel, channelOrder);
    }
}
