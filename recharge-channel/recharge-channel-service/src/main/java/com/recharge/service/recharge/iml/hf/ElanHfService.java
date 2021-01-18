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
public class ElanHfService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String requestUrl = configJSONObject.getString("requestUrl");
        String member = configJSONObject.getString("member");
        String password = configJSONObject.getString("password");

        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("member",member);
        requestMap.put("password",password);
        requestMap.put("phone",huaFeiRechargeInfoBean.getPhone());
        requestMap.put("rd_number",channelOrder.getChannelOrderId());
        requestMap.put("money",huaFeiRechargeInfoBean.getAmt().toString());
        try {
            logger.info("send recharge request params:{}",JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, requestMap,"utf-8",5000);
            logger.info("send recharge response :{}",responseBody);

            String retCode = JSONObject.parseObject(responseBody).getString("code");
            String message = JSONObject.parseObject(responseBody).getString("message");
            if (StringUtils.equals("200",retCode)){
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else if(StringUtils.equals("1001",retCode)
                    || StringUtils.equals("1003",retCode)){
                return new ProcessResult(ProcessResult.FAIL,"余额不足或产品被关闭");
            }else{
                return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
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


        String queryUrl = configJSONObject.getString("queryUrl");
        String member = configJSONObject.getString("member");
        String password = configJSONObject.getString("password");

        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("member",member);
        requestMap.put("password",password);
        requestMap.put("rd_number",channelOrder.getChannelOrderId());
        try {
            logger.info("send query request requestUrl:{}",queryUrl);
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap,"utf-8",5000);
            logger.info("send query response :{}",responseBody);

            String code = JSONObject.parseObject(responseBody).getString("code");

            String status = null;
            try {
                status = JSONObject.parseObject(responseBody).getJSONObject("result").getString("status");
            } catch (Exception e) {
                if (JSONObject.parseObject(responseBody).getJSONArray("result").isEmpty()){
                    return new ProcessResult(ProcessResult.FAIL,"交易失败");
                }
            }

            if (StringUtils.equals("200" , code)){
                if (StringUtils.equals("1" , status)){
                    return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
                }else if (StringUtils.equals("0" , status)){
                    return new ProcessResult(ProcessResult.FAIL,"交易失败");
                }else if (StringUtils.equals("2" , status)){
                    return new ProcessResult(ProcessResult.PROCESSING,"处理中");
                }else{
                    return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
                }
            }else{
                return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
            }


        }catch (Exception e) {
            logger.error("{}send error",channelOrder.getChannelOrderId(),e);
            return new ProcessResult(ProcessResult.PROCESSING,"处理中");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        return null;
    }
}
