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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author Administrator
 * @create 2021/3/1 9:13
 */
@Service
public class YouWangRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("url");
        String merid = configJSONObject.getString("merid");
        String userCode = configJSONObject.getString("userCode");
        String key = configJSONObject.getString("key");
        String callBackUri = configJSONObject.getString("callBackUri");
        String interfacecode="C1001";
        String mobile=huaFeiRechargeInfoBean.getPhone();
        String merOrderid=channelOrder.getChannelOrderId();
        String requestDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String denomination = huaFeiRechargeInfoBean.getAmt().multiply(new BigDecimal(100)).toString();
        String channelType="0";
        String sign= DigestUtils.md5Hex("callBackUri="+callBackUri
                +"&channelType="+channelType
                +"&denomination="+denomination
                +"&interfacecode="+interfacecode
                +"&merOrderid="+merOrderid
                +"&merid="+merid
                +"&mobile="+mobile
                +"&requestDate="+requestDate
                +"&userCode="+userCode
                +"&key="+key).toUpperCase();
        LinkedHashMap<String, Object> requestMap = new LinkedHashMap<>();
        LinkedHashMap<String, String> bizParam = new LinkedHashMap<>();
        requestMap.put("interfacecode",interfacecode);
        requestMap.put("merid",merid);
        requestMap.put("userCode",userCode);
        requestMap.put("sign",sign);
        requestMap.put("bizParam",bizParam);
        bizParam.put("mobile",mobile);
        bizParam.put("merOrderid",merOrderid);
        bizParam.put("requestDate",requestDate);
        bizParam.put("callBackUri",callBackUri);
        bizParam.put("denomination",denomination);
        bizParam.put("channelType",channelType);
        String requestString = JSONObject.toJSONString(requestMap);
        try {
            logger.info("{}游网,下单接口发送的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostString(url, new StringEntity(requestString), "utf-8", 5000);
            logger.info("{}游网,下单接口接收的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(requestMap));
            String res_code = JSONObject.parseObject(responseBody).getString("res_code");
            if (StringUtils.equals("200",res_code)){
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            }else {
                String res_msg = JSONObject.parseObject(responseBody).getString("res_msg");
                return new ProcessResult(ProcessResult.FAIL, "提交失败:"+res_msg);
            }
        } catch (Exception e) {
            logger.error("游网订单号: {} send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交未知原因=" + e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("url");
        String merid = configJSONObject.getString("merid");
        String userCode = configJSONObject.getString("userCode");
        String key = configJSONObject.getString("key");
        String interfacecode="C1101";
        String rechargeType="801";
        String merOrderid=channelOrder.getChannelOrderId();
        String sign= DigestUtils.md5Hex(
                "interfacecode="+interfacecode
                +"&merOrderid="+merOrderid
                +"&merid="+merid
                +"&rechargeType="+rechargeType
                +"&userCode="+userCode
                +"&key="+key).toUpperCase();
        LinkedHashMap<String, Object> requestMap = new LinkedHashMap<>();
        LinkedHashMap<String, String> bizParam = new LinkedHashMap<>();
        requestMap.put("interfacecode",interfacecode);
        requestMap.put("merid",merid);
        requestMap.put("userCode",userCode);
        requestMap.put("sign",sign);
        requestMap.put("bizParam",bizParam);
        bizParam.put("rechargeType",rechargeType);
        bizParam.put("merOrderid",merOrderid);
        String requestString = JSONObject.toJSONString(requestMap);
        try {
            logger.info("{}游网,查询接口的请求参数:{}", merOrderid, JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostString(url, new StringEntity(requestString), "utf-8", 5000);
            logger.info("{}游网,查询接口的返回参数:{}", merOrderid, JSON.toJSONString(responseBody));
            String res_code = JSONObject.parseObject(responseBody).getString("res_code");
            if (StringUtils.equals("200",res_code)){
                String retObject = JSONObject.parseObject(responseBody).getString("retObject");
                String result = JSONObject.parseObject(retObject).getString("result");
                if (StringUtils.equals("70",result)){
                    String operatorOid = JSONObject.parseObject(retObject).getString("operatorOid");
                    if (!operatorOid.isEmpty()) {
                        channelOrder.setOutChannelOrderId(operatorOid);
                    }
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                }else if(StringUtils.equals("15",result)){
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                }else {
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                }
            }else if(StringUtils.equals("106",res_code)){
                return new ProcessResult(ProcessResult.FAIL, "订单不存在");
            }else {
                String res_msg = JSONObject.parseObject(responseBody).getString("res_msg");
                return new ProcessResult(ProcessResult.UNKOWN, "查询未知："+res_msg);
            }
        } catch (Exception e) {
            logger.error("游网查询错误订单号: {} send error{}", merOrderid, e);
            return new ProcessResult(ProcessResult.UNKOWN, "查询报错："+e.getMessage()); }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("70", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("15", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "可疑");
        }
    }
    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("url");
        String merid = configJSONObject.getString("merid");
        String userCode = configJSONObject.getString("userCode");
        String key = configJSONObject.getString("key");
        String interfacecode="C1103";
        String sign= DigestUtils.md5Hex("interfacecode="+interfacecode+"&merid="+merid+"&userCode="+userCode+"&key="+key).toUpperCase();
        LinkedHashMap<String, Object> requestMap = new LinkedHashMap<>();
        LinkedHashMap<String, String> bizParam = new LinkedHashMap<>();
        requestMap.put("interfacecode",interfacecode);
        requestMap.put("merid",merid);
        requestMap.put("userCode",userCode);
        requestMap.put("sign",sign);
        requestMap.put("bizParam",bizParam);
        String requestString = JSONObject.toJSONString(requestMap);
        try {
            String responseBody = HttpClientUtils.invokePostString(url, new StringEntity(requestString), "utf-8", 5000);
            String res_code = JSONObject.parseObject(responseBody).getString("res_code");
            if (StringUtils.equals("200",res_code)){
                String retObject = JSONObject.parseObject(responseBody).getString("retObject");
                String usableAmount = JSONObject.parseObject(retObject).getString("usableAmount");
                return new BigDecimal(usableAmount);
            }else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    @Test
    void test(){
        Channel channel = new Channel();
        channel.setConfigInfo("{url:\"http://39.104.71.124:1219/api/biz\",merid:\"CZ_210301091323592\",userCode:\"UD_u2a55GtmO4\",key:\"3cb9208f568ddded613a3d2acd4c30e0\",callBackUri:\"http://115.28.88.114:8082/youWang/callBack\"}");
        ChannelOrder channelOrder = new ChannelOrder();
        channelOrder.setChannelOrderId("cs202103011111");
//        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
        ProcessResult query = query(channel, channelOrder);
        System.out.println("z");
    }
}
