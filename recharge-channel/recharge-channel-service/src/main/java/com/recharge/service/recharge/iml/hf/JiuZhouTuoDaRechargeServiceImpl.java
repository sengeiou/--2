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

@Service
public class JiuZhouTuoDaRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");
        String callback = configJSONObject.getString("callback");

        Map<String,String> requestMap = new HashMap<String,String>();
        requestMap.put("OrderId",channelOrder.getChannelOrderId());
        requestMap.put("Account", huaFeiRechargeInfoBean.getPhone());
        requestMap.put("ShopId", "300000");
        requestMap.put("UserId",userId);
        requestMap.put("Num","1");
        requestMap.put("Timestamp",System.currentTimeMillis()+"");
        requestMap.put("NotifyUrl",callback);
        requestMap.put("Amount",huaFeiRechargeInfoBean.getAmt().toString());
        requestMap.put("ProductType","1");
        requestMap.put("Sign", DigestUtils.md5Hex(requestMap.get("ShopId")+requestMap.get("UserId")+requestMap.get("ProductType")
                +requestMap.get("OrderId")+requestMap.get("Account")+requestMap.get("Amount")+requestMap.get("Num")+requestMap.get("Timestamp")+key));
        try {
            logger.info("send recharge request params:{}",JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url,requestMap,"utf-8",5000);
            logger.info("send recharge response :{}",responseBody);

            String code = JSONObject.parseObject(responseBody).getString("Code");
            String message = JSONObject.parseObject(responseBody).getString("Message");
            String sysOrderId = JSONObject.parseObject(responseBody).getString("SysOrderId");
            if (StringUtils.equals("10012", code)
                    || StringUtils.equals("10009", code)
                    || StringUtils.equals("19999", code)){
                channelOrder.setOutChannelOrderId(sysOrderId);
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else {
                return new ProcessResult(ProcessResult.FAIL,message);
            }
        }catch (ConnectTimeoutException connectException){
            logger.error("{}send error",rechargeOrderBean.getOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (Exception e) {
            logger.error("{}send error",rechargeOrderBean.getOrderId(),e);
            return new ProcessResult(ProcessResult.SUCCESS,"提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String queryUrl = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

        Map<String,String> requestMap = new HashMap<String,String>();
        requestMap.put("OrderId",channelOrder.getChannelOrderId());
        requestMap.put("ShopId", "300000");
        requestMap.put("UserId",userId);
        requestMap.put("Timestamp",System.currentTimeMillis()+"");
        requestMap.put("Sign", DigestUtils.md5Hex(requestMap.get("ShopId")+requestMap.get("UserId")
                +requestMap.get("OrderId")+requestMap.get("Timestamp")+key));
        try {
            logger.info("send recharge request params:{}",JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl,requestMap,"utf-8",5000);
            logger.info("send recharge response :{}",responseBody);

            String code = JSONObject.parseObject(responseBody).getString("Code");
            String message = JSONObject.parseObject(responseBody).getString("Message");
            String state = JSONObject.parseObject(responseBody).getString("State");
            if (StringUtils.equals("10023", code)){
                if (StringUtils.equals("4", state)){
                    String voucherContent = JSONObject.parseObject(responseBody).getString("VoucherContent");
                    channelOrder.setOutChannelOrderId(voucherContent);
                    return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
                }else if(StringUtils.equals("5", state)){
                    return new ProcessResult(ProcessResult.FAIL,message);
                }
                return new ProcessResult(ProcessResult.PROCESSING,"处理中");
            }else if (StringUtils.equals("10020", code)){
                return new ProcessResult(ProcessResult.FAIL,message);
            }else {
                return new ProcessResult(ProcessResult.UNKOWN,message);
            }
        }catch (Exception e) {
            logger.error("{}send error",channelOrder.getChannelOrderId(),e);
            return new ProcessResult(ProcessResult.PROCESSING,"处理中");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("4", responseOrder.getResponseCode())){
           return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
        }else if(StringUtils.equals("5", responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }
        return new ProcessResult(ProcessResult.UNKOWN, "返回未知");
    }
}
