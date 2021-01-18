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
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

@Service
public class YiXiaoService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String requestUrl = configJSONObject.getString("requestUrl");
        String userCd = configJSONObject.getString("userCd");
        String key = configJSONObject.getString("key");
        String ret_url = configJSONObject.getString("ret_url");

        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("userCd",userCd);
        requestMap.put("parvalue",huaFeiRechargeInfoBean.getAmt().toString());
        requestMap.put("sporder_id",channelOrder.getChannelOrderId());
        requestMap.put("mobilenum",huaFeiRechargeInfoBean.getPhone());
        String md5Str = userCd + requestMap.get("parvalue") +requestMap.get("sporder_id")+ requestMap.get("mobilenum")+key;
        requestMap.put("md5_str", DigestUtils.md5Hex(md5Str).toLowerCase());
        requestMap.put("ret_url",ret_url);
        try {
            logger.info("send recharge request params:{}",JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, requestMap,"utf-8",5000);
            logger.info("send recharge response :{}",responseBody);

            String status = JSONObject.parseObject(responseBody).getString("status");
            String error = JSONObject.parseObject(responseBody).getString("error");
            if (StringUtils.equals("1",status)){
                JSONObject info = JSONObject.parseObject(responseBody).getJSONObject("info");
                String outChannelOrderId = info.getString("order_id");
                channelOrder.setOutChannelOrderId(outChannelOrderId);
                String char_status = info.getString("char_status");
                if (StringUtils.equals("1",char_status)){
                    return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
                }else{
                    return new ProcessResult(ProcessResult.FAIL,"提交失败："+char_status);
                }
            }else{
                return new ProcessResult(ProcessResult.FAIL,error);
            }
        }catch (ConnectTimeoutException connectException){
            logger.error("{}send error",rechargeOrderBean.getOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (SocketTimeoutException socketTimeoutException){
            return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
        }catch (Exception e) {
            logger.error("{}send error",rechargeOrderBean.getOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String userCd = configJSONObject.getString("userCd");
        String key = configJSONObject.getString("key");
        String queryUrl = configJSONObject.getString("queryUrl");
        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("userCd",userCd);
        requestMap.put("sporder_id",channelOrder.getChannelOrderId());
        requestMap.put("order_tp","31");
        String md5Str = userCd+requestMap.get("sporder_id")+ "31" +key;
        requestMap.put("md5_str",DigestUtils.md5Hex(md5Str).toLowerCase());
        try {
            logger.info("send query request requestUrl:{}",queryUrl);
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap,"utf-8",5000);
            logger.info("send query response :{}",responseBody);

            String status = JSONObject.parseObject(responseBody).getString("status");
            String error = JSONObject.parseObject(responseBody).getString("error");
            if (StringUtils.equals("1",status)){
                JSONObject info = JSONObject.parseObject(responseBody).getJSONObject("info");
                String outChannelOrderId = info.getString("order_id");
                channelOrder.setOutChannelOrderId(outChannelOrderId);
                String ststus = info.getString("ststus");
                if (StringUtils.equals("2",ststus)){
                    return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
                }else if (StringUtils.equals("1",ststus)){
                    return new ProcessResult(ProcessResult.PROCESSING,"处理中");
                }else if (StringUtils.equals("3",ststus) || StringUtils.equals("4",ststus)){
                    return new ProcessResult(ProcessResult.FAIL,"充值失败");
                }else{
                    return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
                }
            }else if(StringUtils.equals("1029",status)){
                return new ProcessResult(ProcessResult.FAIL,"充值失败");
            }else{
                return new ProcessResult(ProcessResult.PROCESSING,"处理中");
            }

        }catch (Exception e) {
            logger.error("{}send error",channelOrder.getChannelOrderId(),e);
            return new ProcessResult(ProcessResult.PROCESSING,"处理中");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("2",responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
        }else if (StringUtils.equals("3",responseOrder.getResponseCode()) || StringUtils.equals("4",responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.FAIL,"充值失败");
        }else{
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }
}
