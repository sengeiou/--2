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
import com.recharge.utils.YiMingDianXunUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Service
public class YiMingDianXunService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String requestUrl = configJSONObject.getString("requestUrl");
        String user = configJSONObject.getString("user");
        String password = configJSONObject.getString("password");
        String key = configJSONObject.getString("key");

        try {
            Map<String,String> request = new HashMap<>();
            request.put("price",huaFeiRechargeInfoBean.getAmt().toString());
            request.put("mobile",huaFeiRechargeInfoBean.getPhone());
            request.put("req_plus",channelOrder.getChannelOrderId());
            Map<String,Object> data = new HashMap<>();
            data.put("account",user);
            data.put("password",password);
            data.put("request", request);
            logger.info("send recharge request params:{}",JSONObject.toJSONString(data));
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl+"?user="+user+"&data="+
                    URLEncoder.encode(YiMingDianXunUtils.Encrypt(JSONObject.toJSONString(data),key),"utf-8"),"utf-8",5000);
            responseBody = YiMingDianXunUtils.Decrypt(responseBody , key);
            logger.info("send recharge response :{}",responseBody);
            String retCode = JSONObject.parseObject(responseBody).getString("code");
            String msg = JSONObject.parseObject(responseBody).getString("msg");
            if (StringUtils.equals("success",retCode)){
                String outChannelOrderId = JSONObject.parseObject(responseBody).getJSONObject("response").getString("order_id");
                channelOrder.setOutChannelOrderId(outChannelOrderId);
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else{
                return new ProcessResult(ProcessResult.FAIL,"提交失败");
            }
        }catch (ConnectTimeoutException connectException){
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


        String queryUrl = configJSONObject.getString("queryUrl");
        String user = configJSONObject.getString("user");
        String password = configJSONObject.getString("password");
        String key = configJSONObject.getString("key");

        try {
            Map<String,String> request = new HashMap<>();
            request.put("req_plus",channelOrder.getChannelOrderId());
            Map<String,Object> data = new HashMap<>();
            data.put("account",user);
            data.put("password",password);
            data.put("request", request);
            logger.info("send recharge request params:{}",JSONObject.toJSONString(data));
            String responseBody = HttpClientUtils.invokeGetHttp(queryUrl+"?user="+user+"&data="+
                    URLEncoder.encode(YiMingDianXunUtils.Encrypt(JSONObject.toJSONString(data),key),"utf-8"),"utf-8",5000);
            responseBody = YiMingDianXunUtils.Decrypt(responseBody , key);
            logger.info("send recharge response :{}",responseBody);
            String retCode = JSONObject.parseObject(responseBody).getString("code");
            String msg = JSONObject.parseObject(responseBody).getString("msg");
            if (StringUtils.equals("success",retCode)){
                return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
            }else if (StringUtils.equals("fail",retCode)){
                return new ProcessResult(ProcessResult.FAIL,"充值失败");
            }else{
                return new ProcessResult(ProcessResult.PROCESSING,"处理中");
            }
        }catch (ConnectTimeoutException connectException){
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (Exception e) {
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("success",responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
        }else if(StringUtils.equals("fail",responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.FAIL,responseOrder.getResponseMsg());
        }else {
            return new ProcessResult(ProcessResult.UNKOWN,responseOrder.getResponseMsg());
        }
    }
}
