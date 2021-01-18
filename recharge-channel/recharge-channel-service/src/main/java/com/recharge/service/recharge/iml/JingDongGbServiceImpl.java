package com.recharge.service.recharge.iml;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.c.security.cipher.CipherUtil;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.HuaFeiRechargeInfoBean;
import com.recharge.center.bean.JingDongGbBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.service.recharge.iml.hf.DingXinRechargeServiceImpl;
import com.recharge.utils.RSAUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JingDongGbServiceImpl extends AbsChannelRechargeService{

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        JingDongGbBean jingDongGbBean = (JingDongGbBean) rechargeOrderBean.getRechargeInfoObj(JingDongGbBean.class);

        String callerId = configJSONObject.getString("callerId");
        String requestUrl = configJSONObject.getString("rechargeUrl");
        String authUrl = configJSONObject.getString("authUrl");
        String priKey = configJSONObject.getString("priKey");
        String pubKey = configJSONObject.getString("pubKey");
        String md5Key = configJSONObject.getString("md5Key");

        Map<String, String> requestMap = new HashMap<>();
//        业务系统编号
        requestMap.put("callerId",callerId);
//  京东账号
        requestMap.put("pin", jingDongGbBean.getPin());
//  MD5(callerId+”_”+pin+”_”+ 颁 发 key)

        System.out.println(callerId + jingDongGbBean.getPin() + md5Key);
        requestMap.put("signature", CipherUtil.MD5(callerId +"_" + jingDongGbBean.getPin() +"_" + md5Key));

//        数量
        requestMap.put("name", jingDongGbBean.getName().toString());

        requestMap.put("mobile", jingDongGbBean.getPhone());


        String pin = null ;
        try {
            logger.info("{},发送充值的参数:{}",rechargeOrderBean.getOrderId(),JSON.toJSONString(requestMap));

            Map<String,String> reMap = new HashMap<>();
            reMap.put(CipherUtil.encryptCipher(JSONObject.toJSONString(requestMap), pubKey , priKey) , "");
            String responseBody = HttpClientUtils.invokePostHttp(authUrl, reMap, "utf-8" , 5000);
            responseBody = CipherUtil.decryptCipher(responseBody, pubKey , priKey) ;
            logger.info("{},收到充值的响应:{}",rechargeOrderBean.getOrderId(),responseBody);
            JSONObject responseJsonObj = JSONObject.parseObject(responseBody);
            String responseCode = responseJsonObj.getString("responseCode");
            String responseMessage = responseJsonObj.getString("responseMessage");
            if (!StringUtils.equals("0000", responseCode)){
                return new ProcessResult(ProcessResult.FAIL,responseMessage);
            }

            pin = responseJsonObj.getString("pin");
        }catch (Exception e) {
            logger.error("{}send error",rechargeOrderBean.getOrderId(),e);
            return new ProcessResult(ProcessResult.FAIL,"身份认证失败");
        }

        requestMap = new HashMap<>();
//        业务系统编号
        requestMap.put("callerId",callerId);
//  京东账号
        requestMap.put("pin", pin);
//  MD5(callerId+”_”+pin+”_”+ 颁 发 key)

        System.out.println(callerId + jingDongGbBean.getPin() + md5Key);
        requestMap.put("signature", CipherUtil.MD5(callerId + "_" + pin + "_" + md5Key));

//        交易唯一标识，
        requestMap.put("transactionCode", channelOrder.getChannelOrderId());

//        钢镚面值，如 50 钢镚， 20 钢镚， 10 钢镚
        requestMap.put("denomination",jingDongGbBean.getAmt().toString());

//        数量
        requestMap.put("number", jingDongGbBean.getNumber().toString());

        requestMap.put("mobile", jingDongGbBean.getPhone());


        try {
            logger.info("{},发送充值的参数:{}",rechargeOrderBean.getOrderId(),JSON.toJSONString(requestMap));

            Map<String,String> reMap = new HashMap<>();
            reMap.put(CipherUtil.encryptCipher(JSONObject.toJSONString(requestMap), pubKey , priKey) , "");
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, reMap, "utf-8" , 5000);
            responseBody = CipherUtil.decryptCipher(responseBody, pubKey , priKey) ;
            logger.info("{},收到充值的响应:{}",rechargeOrderBean.getOrderId(),responseBody);
            JSONObject responseJsonObj = JSONObject.parseObject(responseBody);


            String responseCode = responseJsonObj.getString("responseCode");
            String responseMessage = responseJsonObj.getString("responseMessage");
            if (StringUtils.equals("0000",responseCode)){
                String exchangeMemoNo = responseJsonObj.getString("exchangeMemoNo");
                channelOrder.setOutChannelOrderId(exchangeMemoNo);
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else{
                return new ProcessResult(ProcessResult.FAIL,responseMessage);
            }
        } catch (ConnectTimeoutException connectException){
            logger.error("{}send error",rechargeOrderBean.getOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (Exception e) {
            logger.error("{}send error",rechargeOrderBean.getOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String callerId = configJSONObject.getString("callerId");
        String requestUrl = configJSONObject.getString("queryUrl");
        String md5Key = configJSONObject.getString("md5Key");
        String priKey = configJSONObject.getString("priKey");
        String pubKey = configJSONObject.getString("pubKey");
        Map<String, String> requestMap = new HashMap<>();
//        业务系统编号
        requestMap.put("callerId",callerId);
//        交易唯一标识，
        requestMap.put("transactionCode", channelOrder.getChannelOrderId());

//  MD5(callerId+”_”+pin+”_”+ 颁 发 key)
        requestMap.put("signature", CipherUtil.MD5(callerId + "_"+ channelOrder.getChannelOrderId() + "_" + md5Key));


        try {
            logger.info("{},发送充值的参数:{}", channelOrder.getChannelOrderId(),JSON.toJSONString(requestMap));
            Map<String,String> reMap = new HashMap<>();
            reMap.put(CipherUtil.encryptCipher(JSONObject.toJSONString(requestMap), pubKey , priKey) , "");
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, reMap, "utf-8" , 5000);
            logger.info("{},收到充值的响应:{}", channelOrder.getChannelOrderId(),responseBody);
            responseBody = CipherUtil.decryptCipher(responseBody, pubKey , priKey) ;
            JSONObject responseJsonObj = JSONObject.parseObject(responseBody);

            String responseCode = responseJsonObj.getString("responseCode");
            String responseMessage = responseJsonObj.getString("responseMessage");
            if (StringUtils.equals("0000",responseCode)){
                return new ProcessResult(ProcessResult.SUCCESS,"兑换成功");
            }else{
                return new ProcessResult(ProcessResult.FAIL,responseMessage);
            }
        }catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(),e);
            return new ProcessResult(ProcessResult.PROCESSING,"处理中");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        return null;
    }
}
