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
import com.recharge.domain.ProductRelation;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Administrator
 * @create 2020/12/15 15:39
 */
@Service
public class AiErBeiRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String channelId = "100116";

    public static String getSign(Map<String, String> signMap, String password) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : signMap.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.append(password);
        return encodeMD5Hex(sb.toString()).toLowerCase();
    }

    public static String encodeMD5Hex(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes("US-ASCII"));
            byte[] digest = md.digest();
            StringBuffer md5 = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                md5.append(Character.forDigit((digest[i] & 0xF0) >> 4, 16));
                md5.append(Character.forDigit(digest[i] & 0xF, 16));
            }
            return md5.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "-1";
    }

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String userName = configJSONObject.getString("userName");
        String url = configJSONObject.getString("rechargeurl");
        String key = configJSONObject.getString("key");
        String clientOrderId = channelOrder.getChannelOrderId();
        String requestTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String account = huaFeiRechargeInfoBean.getPhone();
        ProductRelation productRelation = new ProductRelation();
        if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "联通")) {
            productRelation = queryChannelProductId("全国联通话费" + huaFeiRechargeInfoBean.getAmt() + "元", channelId);
            if (productRelation == null) {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "移动")) {
            productRelation = queryChannelProductId("全国移动话费" + huaFeiRechargeInfoBean.getAmt() + "元", channelId);
            if (productRelation == null) {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } else {
            productRelation = queryChannelProductId("全国电信话费" + huaFeiRechargeInfoBean.getAmt() + "元", channelId);
            if (productRelation == null) {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        }
        String productCode = productRelation.getChannelProductId();
        String num = "1";
        Map<String, String> map = new TreeMap<String, String>();
        map.put("userName", userName);
        map.put("clientOrderId", clientOrderId);
        map.put("requestTime", requestTime);
        map.put("account", account);
        map.put("productCode", productCode);
        map.put("num", num);
        String sign = getSign(map, key);
        map.put("sign", sign);
        try {
            logger.info("{}aierbei,发送充值的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map)), "", "utf-8", 5000);
            logger.info("{}aierbei,response的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("code");
            String message = JSONObject.parseObject(responseBody).getString("msg");
            if (StringUtils.equals(code, "0")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals(code, "-1")) {
                return new ProcessResult(ProcessResult.UNKOWN, "提交未知原因=" + message);
            } else {
                return new ProcessResult(ProcessResult.FAIL, message);
            }
        } catch (Exception e) {
            logger.error("aierbei订单号: {} send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交未知原因=" + e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String userName = configJSONObject.getString("userName");
        String url = configJSONObject.getString("queryurl");
        String key = configJSONObject.getString("key");
        String clientOrderId = channelOrder.getChannelOrderId();
        String requestTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Map<String, String> map = new TreeMap<String, String>();
        map.put("requestTime", requestTime);
        map.put("userName", userName);
        map.put("clientOrderId", clientOrderId);
        String sign = getSign(map, key);
        map.put("sign", sign);
        String responseBody = null;
        try {
            logger.info("{}aierbei,查询的请求参数:{}", clientOrderId, JSON.toJSONString(map));
            responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map)), "", "utf-8", 5000);
            logger.info("{}aierbei,查询的返回参数:{}", clientOrderId, JSON.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("code");
            if (StringUtils.equals(code, "0")) {
                String status = JSONObject.parseObject(responseBody).getString("status");
                if (StringUtils.equals(status, "success")) {
                    String serial = JSONObject.parseObject(responseBody).getString("serial");
                    if (!serial.isEmpty()) {
                        channelOrder.setOutChannelOrderId(serial);
                    }
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (StringUtils.equals(status, "failure")) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else if (StringUtils.equals(status, "progress")) {
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                } else {
                    return new ProcessResult(ProcessResult.UNKOWN, "返回的status为：" + status);
                }
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "未知错误");
            }
        } catch (Exception e) {
            logger.error("aierbei查询错误订单号: {} send error{}", clientOrderId, e);
            return new ProcessResult(ProcessResult.UNKOWN, "未知错误");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("success", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("failure", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String userName = configJSONObject.getString("userName");
        String url = configJSONObject.getString("balanceQueryurl");
        String requestTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Map<String, String> map = new TreeMap<String, String>();
        map.put("userName", userName);
        map.put("requestTime", requestTime);
        String sign = getSign(map, "A217E05016B9599A15ABD0EEA9D43373");
        map.put("sign", sign);
        System.out.println(map);
        try {
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map)), "", "utf-8", 5000);
            String code = JSONObject.parseObject(responseBody).getString("code");
            if (StringUtils.equals(code, "0")) {
                String balance = JSONObject.parseObject(responseBody).getString("balance");
                return new BigDecimal(balance);
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    public void test() {
        AiErBeiRechargeServiceImpl aiErBeiRechargeService = new AiErBeiRechargeServiceImpl();
//        System.out.println(bigDecimal);
        Channel channel = new Channel();
        channel.setConfigInfo("{\"userName\": \"HFyajie\",\"rechargeurl\": \"http://139.224.75.49:8081/acceptor/recharge.do\",\"balanceQueryurl\": \"http://139.224.75.49:8081/acceptor/queryBalance.do\",\"notifyUrl\": \"http://139.129.85.83:8082/aiErBei/callBack\",\"key\": \"A217E05016B9599A15ABD0EEA9D43373\",\"queryurl\": \"http://139.224.75.49:8081/acceptor/query.do\"}");
//        ProcessResult recharge = aiErBeiRechargeService.recharge(new Channel(), new ChannelOrder(), new RechargeOrderBean());
        ChannelOrder channelOrder = new ChannelOrder();
        channelOrder.setChannelOrderId("cs202012151717");
//        BigDecimal bigDecimal = aiErBeiRechargeService.balanceQuery(channel);
        ProcessResult query = aiErBeiRechargeService.query(channel, channelOrder);
        System.out.println("z");
    }
}
