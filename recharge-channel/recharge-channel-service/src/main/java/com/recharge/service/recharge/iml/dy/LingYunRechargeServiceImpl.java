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
import com.recharge.domain.ChannelOrderSupRelation;
import com.recharge.mapper.IChannelOrderSupRelationMapper;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Administrator
 * @create 2021/3/4 14:32
 */
@Service
public class LingYunRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IChannelOrderSupRelationMapper IChannelOrderSupRelationMapper;

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        DouYinRechargeInfoBean douYinRechargeInfoBean = (DouYinRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(DouYinRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String merchant_id = configJSONObject.getString("merchant_id");
        String md5key = configJSONObject.getString("md5key");
        String notify_url=configJSONObject.getString("notify_url");
        String sign_type="MD5";
        String time=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String product_id="TIKTOK";
//        String trade_amount="6.00";
        String trade_amount=douYinRechargeInfoBean.getAmount().setScale(2).toString();
        String order_id=channelOrder.getChannelOrderId();
        String remark="db";
        String account=douYinRechargeInfoBean.getAccount();
//        String account="35964461";
        String phone=douYinRechargeInfoBean.getPhone();
//        String phone="18360969388";
        ChannelOrderSupRelation newchannelOrder = new ChannelOrderSupRelation();
        newchannelOrder.setChannelOrderId(channelOrder.getChannelOrderId());
        newchannelOrder.setCreateTime(time);
        newchannelOrder.setSupId(channel.getChannelId());
        Map<String, String> body = new TreeMap<>();
        Map<String, Object> map = new LinkedHashMap<>();
        body.put("product_id",product_id);
        body.put("trade_amount",trade_amount);
        body.put("order_id",order_id);
        body.put("notify_url",notify_url);
        body.put("remark",remark);
        body.put("account",account);
        body.put("phone",phone);
        StringBuffer sb=new StringBuffer();
        sb.append("account=").append(account)
                .append("&notify_url=").append(notify_url)
                .append("&order_id=").append(order_id)
                .append("&phone=").append(phone)
                .append("&product_id=").append(product_id)
                .append("&remark=").append(remark)
                .append("&trade_amount=").append(trade_amount);
        String params_src=sb.toString();
        String md5Str=merchant_id+params_src+time+md5key;
        String sign= DigestUtils.md5Hex(md5Str).toLowerCase();
        map.put("sign",sign);
        map.put("sign_type",sign_type);
        map.put("merchant_id",merchant_id);
        map.put("time",time);
        map.put("body",body);
        String s = JSON.toJSONString(map);
        try {
            logger.info("凌云抖币，下单接口请求的参数:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map),"utf-8"), "", "utf-8", 5000);
            logger.info("凌云抖币，下单接口响应的参数:{}", JSONObject.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("code");
            if (StringUtils.equals("0000",code)){
                String data = JSONObject.parseObject(responseBody).getString("body");
                String platform_order_id = JSONObject.parseObject(data).getString("platform_order_id");
                newchannelOrder.setSupOrderId(platform_order_id);
                IChannelOrderSupRelationMapper.insertOne(newchannelOrder);
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            }else if(StringUtils.equals("1999",code)){
                return new ProcessResult(ProcessResult.UNKOWN, "供货商系统维护");
            }else if(StringUtils.equals("1002",code)){
                return new ProcessResult(ProcessResult.UNKOWN, "订单号重复");
            }else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (Exception e) {
            logger.info("凌云抖币 下单报错:{}", e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "提交报错："+e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("queryUrl");
        String merchant_id = configJSONObject.getString("merchant_id");
        String md5key = configJSONObject.getString("md5key");
        String time=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String order_id = channelOrder.getChannelOrderId();
        String product_id = "TIKTOK";
        ChannelOrderSupRelation channelOrderSupRelation = IChannelOrderSupRelationMapper.selectByOrderId(order_id);
        String platform_order_id = channelOrderSupRelation.getSupOrderId();
        Map<String, String> body = new TreeMap<>();
        Map<String, Object> map = new LinkedHashMap<>();
        body.put("product_id",product_id);
        body.put("platform_order_id",platform_order_id);
        body.put("order_id",order_id);
        StringBuffer sb=new StringBuffer();
        sb.append("order_id=").append(order_id)
                .append("&platform_order_id=").append(platform_order_id)
                .append("&product_id=").append(product_id);
        String params_src=sb.toString();
        String md5Str=merchant_id+params_src+time+md5key;
        String sign= DigestUtils.md5Hex(md5Str).toLowerCase();
        map.put("sign",sign);
        map.put("merchant_id",merchant_id);
        map.put("time",time);
        map.put("body",body);
        String s = JSON.toJSONString(map);
        try {
            logger.info("凌云抖币，查单接口请求的参数:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map)), "", "utf-8", 5000);
            logger.info("凌云抖币，查单接口响应的参数:{}", JSONObject.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("code");
            if(StringUtils.equals("0000",code)){
                String data = JSONObject.parseObject(responseBody).getString("body");
                String order_status = JSONObject.parseObject(data).getString("order_status");
                if(StringUtils.equals("SUCCESS",order_status)){
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                }else if(StringUtils.equals("FAILURE",order_status)||StringUtils.equals("INVALID",order_status)){
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                }else if(StringUtils.equals("PROCESSING",order_status)||StringUtils.equals("PENDING",order_status)){
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                }else {
                    return new ProcessResult(ProcessResult.UNKOWN, "查询可疑,状态码:"+order_status);
                }
            }else {
                return new ProcessResult(ProcessResult.UNKOWN, "查询可疑,状态码:"+code);
            }
        } catch (Exception e) {
            logger.info("凌云抖币 查询报错:{}", e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "查询报错:"+e.getMessage());
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("SUCCESS", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("FAILURE", responseOrder.getResponseCode())||StringUtils.equals("INVALID",responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else if (StringUtils.equals("PROCESSING", responseOrder.getResponseCode())||StringUtils.equals("PENDING",responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        }else {
            return new ProcessResult(ProcessResult.UNKOWN, "未知");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("balanceQueryUrl");
        String merchant_id = configJSONObject.getString("merchant_id");
        String md5key = configJSONObject.getString("md5key");
        String time=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String md5Str=merchant_id+time+md5key;
        String sign= DigestUtils.md5Hex(md5Str).toLowerCase();
        Map<String, String> map = new TreeMap<String, String>();
        map.put("merchant_id", merchant_id);
        map.put("time", time);
        map.put("sign", sign);
        System.out.println(map);
        try {
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map)), "", "utf-8", 5000);
            String code = JSONObject.parseObject(responseBody).getString("code");
            if (StringUtils.equals(code, "0000")) {
                String body = JSONObject.parseObject(responseBody).getString("body");
                String balance = JSONObject.parseObject(body).getString("balance");
                return new BigDecimal(balance);
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    void test(){
        Channel channel = new Channel();
        ChannelOrder channelOrder = new ChannelOrder();
        channel.setConfigInfo("{rechargeUrl:\"http://47.94.143.82:52100/v1/api/open/acquire/order\",queryUrl:\"http://47.94.143.82:52100/v1/api/open/inquire/order\",balanceQueryUrl:\"http://47.94.143.82:52100/v1/api/open/inquire/balance\",md5key:\"F4754550A8B099BEDA465F24C8337955\",notify_url:\"http://115.28.88.114:8083/LingYun/callback\",merchant_id:\"3021202021030200002\"}");
        channelOrder.setChannelOrderId("cs202103081508");
        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
//        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult query = query(new Channel(), new ChannelOrder());
    }
}
