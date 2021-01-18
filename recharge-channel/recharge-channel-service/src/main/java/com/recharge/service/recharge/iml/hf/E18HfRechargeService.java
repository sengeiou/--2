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
public class E18HfRechargeService extends AbsChannelRechargeService {


    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     *
     *
     * 话费充值（0101）查询充值结果（0102）代理商余额查询（0103）
     省内流量充值（0104） 漫游流量充值（0105） QQ币充值（0106）

     * @param channel
     * @param channelOrder
     * @param rechargeOrderBean
     * @return
     */
    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String id = configJSONObject.getString("id");
        String password = configJSONObject.getString("password");
        String callback = configJSONObject.getString("callback");

        Map<String,String> requestMap = new HashMap<String,String>();
        requestMap.put("id",id);
        requestMap.put("password","****");
        requestMap.put("phone",huaFeiRechargeInfoBean.getPhone());
        requestMap.put("type","0101");
        requestMap.put("money",huaFeiRechargeInfoBean.getAmt().toString());
        requestMap.put("number",channelOrder.getChannelOrderId());
        requestMap.put("callback" , callback);
        requestMap.put("sign",DigestUtils.md5Hex(id+ password+ huaFeiRechargeInfoBean.getPhone()+ huaFeiRechargeInfoBean.getAmt().toString() + channelOrder.getChannelOrderId()));
        try {
            logger.info("send recharge request params:{}",JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url,requestMap,"utf-8",5000);
            logger.info("send recharge response :{}",responseBody);

            String[] responseParams = responseBody.split("&");
            Map<String,String> responseMap = new HashMap<String, String>();
            for (String responseParam:responseParams){
                responseMap.put(responseParam.split("=")[0],responseParam.split("=")[1]);
            }

            String code = responseMap.get("code");
            if (StringUtils.equals("1130",code) || StringUtils.equals("1132" , code)){
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else {
                return new ProcessResult(ProcessResult.FAIL,"提交失败");
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


        String url = configJSONObject.getString("url");
        String id = configJSONObject.getString("id");
        String password = configJSONObject.getString("password");

        Map<String,String> requestMap = new HashMap<String,String>();
        requestMap.put("id",id);
        requestMap.put("password","****");
        requestMap.put("type","0102");
        requestMap.put("number",channelOrder.getChannelOrderId());
        requestMap.put("sign",DigestUtils.md5Hex(id+ password+ channelOrder.getChannelOrderId()));

        try {
            logger.info("send query request params:{}",JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url,requestMap,"utf-8",5000);
            logger.info("send query response :{}",responseBody);
            String[] responseParams = responseBody.split("&");
            Map<String,String> responseMap = new HashMap<String, String>();
            for (String responseParam:responseParams){
                if (StringUtils.isEmpty(responseParam)){
                    continue;
                }
                responseMap.put(responseParam.split("=")[0],responseParam.split("=")[1]);
            }

            String code = responseMap.get("code");
            if (StringUtils.equals("1133",code)){
                return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
            }else if (StringUtils.equals("1134",code)){
                return new ProcessResult(ProcessResult.FAIL,"充值失败");
            }else {
                return new ProcessResult(ProcessResult.PROCESSING,"处理中");
            }

        }catch (Exception e) {
            logger.error("{}send error",channelOrder.getChannelOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        /*
        1130	正在 充值中
1133	  充值成功
1134	充值失败
1135	退款

                */
        String responseCode = responseOrder.getResponseCode();
        if (StringUtils.equals("1133",responseCode)){
            return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
        }else if (StringUtils.equals("1134",responseCode)
                || StringUtils.equals("1135" ,responseCode)){
            return new ProcessResult(ProcessResult.FAIL, responseOrder.getResponseMsg());
        }else{
            return new ProcessResult(ProcessResult.UNKOWN,"未知返回码");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        return new BigDecimal("9999999999");
    }
}
