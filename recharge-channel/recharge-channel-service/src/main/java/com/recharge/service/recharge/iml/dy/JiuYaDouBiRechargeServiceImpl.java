package com.recharge.service.recharge.iml.dy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.DouYinRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Administrator
 * @create 2021/3/5 17:05
 */
@Service
public class JiuYaDouBiRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String channelId = "100147";


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        DouYinRechargeInfoBean douYinRechargeInfoBean = (DouYinRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(DouYinRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        ProductRelation productRelation = queryChannelProductId("抖币" + douYinRechargeInfoBean.getAmount() + "元", channelId);
        if (productRelation == null) {
            return new ProcessResult(ProcessResult.FAIL, "供货商商品编号查询失败");
        }
        String userid = configJSONObject.getString("userid");
        //设置商品编号
        String productid = productRelation.getChannelProductId();
        //设置商品数量
        String num = "1";
        //设置充值账号
        String account = douYinRechargeInfoBean.getAccount()+douYinRechargeInfoBean.getPhone();
        //设置订单时间
        String spordertime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //设置订单ID
        String sporderid = channelOrder.getChannelOrderId();
        String key = configJSONObject.getString("key");
        String areaid = "";
        String serverid = "";
        String sign = DigestUtils.md5Hex(
                "userid=" + userid
                        + "&productid=" + productid
                        + "&num=" + num
                        + "&areaid=" + areaid
                        + "&serverid=" + serverid
                        + "&account=" + account
                        + "&spordertime=" + spordertime
                        + "&sporderid=" + sporderid
                        + "&key=" + key);
        String back_url = configJSONObject.getString("callback");
        Map<String, String> requestMap = new TreeMap<>();
        requestMap.put("userid", userid);
        requestMap.put("productid", productid);
        requestMap.put("num", num);
        requestMap.put("areaid", areaid);
        requestMap.put("serverid", serverid);
        requestMap.put("account", account);
        requestMap.put("spordertime", spordertime);
        requestMap.put("sporderid", sporderid);
        requestMap.put("sign", sign);
        requestMap.put("userip", "");
        requestMap.put("back_url", back_url);
        String s = JSONObject.toJSONString(requestMap);
        try {
            logger.info("JiuYaDB send recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(configJSONObject.getString("rechargeUrl"), requestMap, "utf-8", 5000);
            logger.info("JiuYaDB send recharge response :{}", JSONObject.toJSONString(responseBody));
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            if (StringUtils.equals(resultno, "0")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals(resultno, "5002")) {
                return new ProcessResult(ProcessResult.FAIL, "余额不足");
            } else if (StringUtils.equals(resultno, "9999")) {
                return new ProcessResult(ProcessResult.UNKOWN, "需要跟供货商核实");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (Exception e) {
            logger.error("{}send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String userid = configJSONObject.getString("userid");
        String sporderid = channelOrder.getChannelOrderId();
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid", userid);
        requestMap.put("sporderid", sporderid);
        String url = configJSONObject.getString("queryUrl");
        try {
            logger.info("JiuYa send queryBalance request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("JiuYa send queryBalance response :{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            if (StringUtils.equals(resultno, "1")) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals(resultno, "9")) {
                return new ProcessResult(ProcessResult.FAIL, "充值失败已退款");
            } else if (StringUtils.equals(resultno, "0")) {
                return new ProcessResult(ProcessResult.PROCESSING, "等待充值");
            } else if (StringUtils.equals(resultno, "2")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else if (StringUtils.equals(resultno, "9999")) {
                return new ProcessResult(ProcessResult.UNKOWN, "需要跟供货商核实");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "未知");
            }
        } catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑" + e.getMessage());
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(), "1")) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "0")) {
            return new ProcessResult(ProcessResult.PROCESSING, "等待充值");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "2")) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        } else if(StringUtils.equals(responseOrder.getResponseCode(), "9")){
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }else {
            return new ProcessResult(ProcessResult.UNKOWN, "未知");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String userid = configJSONObject.getString("userid");
        String sign = DigestUtils.md5Hex("userid=" + userid + "&key=" + configJSONObject.getString("key"));
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid", userid);
        requestMap.put("sign", sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(configJSONObject.getString("queryBalanceUrl"), requestMap, "utf-8", 5000);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String balance = root.elementText("balance");
            return new BigDecimal(balance);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    @Test
    void test(){
        Channel channel = new Channel();
        ChannelOrder channelOrder = new ChannelOrder();
        channelOrder.setChannelOrderId("cs202103081053");
        channel.setConfigInfo("{rechargeUrl:\"http://121.41.86.9:8082/gameonlinepay.do\",queryUrl:\"http://121.41.86.9:8001/searchpay.do\",queryBalanceUrl:\"http://121.41.86.9:8082/searchbalance.do\",key:\"Cy2Wt2sQ7e4NGCQQYZiNMY5wki6czAbn\",callback:\"http://47.104.224.57:8083/JiuYaDB/callback\",userid:\"10004927\"}");
//        channel.setConfigInfo("{rechargeUrl:\"http://121.41.86.9:8082/gameonlinepay.do\",queryUrl:\"http://121.41.86.9:8001/searchpay.do\",queryBalanceUrl:\"http://121.41.86.9:8082/searchbalance.do\",key:\"Cy2Wt2sQ7e4NGCQQYZiNMY5wki6czAbn\",callback:\"http://115.28.88.114:8083/JiuYaDB/callback\",userid:\"10004927\"}");
//        ProcessResult query = query(channel, channelOrder);
        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
    }
}
