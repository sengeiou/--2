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
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiBaoRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      请求地址：/flyover/orderApi/buy
        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String appId = configJSONObject.getString("appId");
        String notifyUrl = configJSONObject.getString("callback");
        String acctKey = configJSONObject.getString("acctKey");
        String appKey = configJSONObject.getString("appKey");
        StringBuffer productName = new StringBuffer();
        String[] arr = new String[]{"电信", "移动", "联通"};
        for (String s : arr) {
            if (rechargeOrderBean.getProductName().indexOf(s) != -1) {
                productName.append("全国")
                        .append(s)
                        .append("话费")
                        .append(rechargeOrderBean.getProductName().substring(rechargeOrderBean.getProductName().indexOf(s) + 2));
            }
        }
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userId", userId);
        requestMap.put("appid", appId);
//        业务ID，话费快充1，话费慢充2，广东石化电子账户3，石化卡充值4，石油卡充值5，腾讯视频会员6
        requestMap.put("busId", "1");
        requestMap.put("goodsId", queryChannelProductId(productName.toString(), "100043").getChannelProductId());
        requestMap.put("notifyUrl", notifyUrl);
        requestMap.put("accountNo", huaFeiRechargeInfoBean.getPhone());
        requestMap.put("outOrderId", channelOrder.getChannelOrderId());
        requestMap.put("timeStamp", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        requestMap.put("num", "1");
        requestMap.put("sign", DigestUtils.md5Hex(
                "accountNo=" + requestMap.get("accountNo")
                        + "&appid=" + requestMap.get("appid")
                        + "&busId=" + requestMap.get("busId")
                        + "&goodsId=" + requestMap.get("goodsId")
                        + "&notifyUrl=" + requestMap.get("notifyUrl")
                        + "&num=" + requestMap.get("num")
                        + "&outOrderId=" + requestMap.get("outOrderId")
                        + "&timeStamp=" + requestMap.get("timeStamp")
                        + "&userId=" + requestMap.get("userId")
                        + "&acctKey=" + acctKey
                        + "&appKey=" + appKey).toUpperCase());
        String requestString = JSONObject.toJSONString(requestMap);
        try {
            logger.info("send recharge request params:{}", requestString);
            String responseBody = HttpClientUtils.invokePostString(url, new StringEntity(requestString), "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);
            String code = JSONObject.parseObject(responseBody).getString("code");
            String msg = JSONObject.parseObject(responseBody).getString("msg");
            if (StringUtils.equals(code, "200")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, msg);
            }
        } catch (Exception e) {
            logger.error("{}send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
//      请求地址：/flyover/orderApi/orderInfo
        String url = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userId");
        String appId = configJSONObject.getString("appId");
        String acctKey = configJSONObject.getString("acctKey");
        String appKey = configJSONObject.getString("appKey");
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userId", userId);
        requestMap.put("appid", appId);
//        业务ID，话费快充1，话费慢充2，广东石化电子账户3，石化卡充值4，石油卡充值5，腾讯视频会员6
        requestMap.put("busId", "1");
        requestMap.put("outOrderId", channelOrder.getChannelOrderId());
        requestMap.put("sign", DigestUtils.md5Hex(
                "appid=" + requestMap.get("appid")
                        + "&busId=" + requestMap.get("busId")
                        + "&outOrderId=" + requestMap.get("outOrderId")
                        + "&userId=" + requestMap.get("userId")
                        + "&acctKey=" + acctKey
                        + "&appKey=" + appKey).toUpperCase());
        String requestString = JSONObject.toJSONString(requestMap);
        try {
            logger.info("send query request params:{}", requestString);
            String responseBody = HttpClientUtils.invokePostString(url, new StringEntity(requestString), "utf-8", 5000);
            logger.info("send query response :{}", responseBody);
            String code = JSONObject.parseObject(responseBody).getString("code");
            String msg = JSONObject.parseObject(responseBody).getString("msg");
            String data = JSONObject.parseObject(responseBody).getString("data");
            if (StringUtils.equals(code, "200")) {
//              0未处理 1成功 2失败 3处理中 9未确认
                String orderRes = JSONObject.parseObject(data).getString("orderRes");
                if (StringUtils.equals("1", orderRes)) {
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (StringUtils.equals("2", orderRes)) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else {
                    return new ProcessResult(ProcessResult.PROCESSING, "处理中");
                }
            } else {
                return new ProcessResult(ProcessResult.PROCESSING, msg);
            }
        } catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "查询失败，原因为:"+e.getMessage());
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
//        0未处理 1成功 2失败 3处理中 9未确认
        if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
//      请求地址：/flyover/orderApi/balance
        String url = configJSONObject.getString("queryBalanceUrl");
        String userId = configJSONObject.getString("userId");
        String acctKey = configJSONObject.getString("acctKey");
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userId", userId);
        requestMap.put("sign", DigestUtils.md5Hex(
                "userId=" + requestMap.get("userId")
                        + "&acctKey=" + acctKey).toUpperCase());
        String requestString = JSONObject.toJSONString(requestMap);
        try {
            logger.info("send queryBalance request params:{}", requestString);
            String responseBody = HttpClientUtils.invokePostString(url, new StringEntity(requestString), "utf-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);
            String code = JSONObject.parseObject(responseBody).getString("code");
            String data = JSONObject.parseObject(responseBody).getString("data");
            if (StringUtils.equals(code, "200")) {
                String balance = JSONObject.parseObject(data).getString("acct_money");
                return new BigDecimal(balance).divide(new BigDecimal("1000"));
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            logger.error("AiBao balanceQuery send error{}", e);
            return BigDecimal.ZERO;
        }
    }
}
