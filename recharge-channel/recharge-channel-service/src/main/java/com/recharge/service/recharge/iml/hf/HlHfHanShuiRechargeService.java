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
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;

@Service
public class HlHfHanShuiRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String appId = configJSONObject.getString("appId");
        String notifyUrl = configJSONObject.getString("notifyUrl");
        String key = configJSONObject.getString("key");

        try {
            String requestUrl = url +"?appId="+appId
                    +"&productValue="+huaFeiRechargeInfoBean.getAmt()
                    +"&phoneNumber="+huaFeiRechargeInfoBean.getPhone()
                    +"&notifyUrl="+URLEncoder.encode(notifyUrl,"utf-8")
                    +"&orderId="+channelOrder.getChannelOrderId()
                    +"&key="
                    + DigestUtils.md5Hex("appId"+appId+"notifyUrl"+notifyUrl+"orderId"
                    +channelOrder.getChannelOrderId()+"phoneNumber"+huaFeiRechargeInfoBean.getPhone()
                    +"productValue"+huaFeiRechargeInfoBean.getAmt()+key).toUpperCase();
            logger.info("send recharge request params:{}",requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl,"utf-8",5000);
            logger.info("send recharge response :{}",StringEscapeUtils.unescapeJava(responseBody));
            String retCode = JSONObject.parseObject(responseBody).getString("code");
            String msg = JSONObject.parseObject(responseBody).getString("message");
            if (StringUtils.equals("0",retCode)){
                String outChannelOrderId = JSONObject.parseObject(responseBody).getString("billNo");
                channelOrder.setOutChannelOrderId(outChannelOrderId);
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else{
                return new ProcessResult(ProcessResult.FAIL,msg);
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


        String url = configJSONObject.getString("queryUrl");
        String appId = configJSONObject.getString("appId");
        String key = configJSONObject.getString("key");

        try {
            String requestUrl = url +"?appId="+appId
                    +"&key="+DigestUtils.md5Hex("appId"+appId+"orderId"+channelOrder.getChannelOrderId()+key).toUpperCase()
                    +"&orderId="+channelOrder.getChannelOrderId();
            logger.info("send recharge request params:{}",requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl,"utf-8",5000);
            logger.info("send recharge response :{}", StringEscapeUtils.unescapeJava(responseBody));
            String retCode = JSONObject.parseObject(responseBody).getString("code");
            String msg = JSONObject.parseObject(responseBody).getString("message");
            if (StringUtils.equals("20000",retCode)){
                String outChannelOrderId = JSONObject.parseObject(responseBody).getString("billNo");
                channelOrder.setOutChannelOrderId(outChannelOrderId);
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else if (StringUtils.equals("20010",retCode)){
                return new ProcessResult(ProcessResult.PROCESSING,"处理中");
            }else{
                return new ProcessResult(ProcessResult.FAIL,msg);
            }
        }catch (Exception e) {
            logger.error("{}send error",channelOrder.getChannelOrderId(),e);
            return new ProcessResult(ProcessResult.PROCESSING,"处理中");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("20000",responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
        }else if (StringUtils.equals("20090",responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.FAIL,responseOrder.getResponseMsg());
        }else{
            return new ProcessResult(ProcessResult.UNKOWN, "未知错误码");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      余额查询地址：/api/getUserBalance
        String url = configJSONObject.getString("queryBalanceUrl");
        String appId = configJSONObject.getString("appId");
        String key = configJSONObject.getString("apiKey");

        try {
            String requestUrl = url + "?appId=" + appId
                    + "&sign=" + DigestUtils.md5Hex(appId + key).toLowerCase();
            logger.info("send recharge request params:{}", requestUrl);

            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "utf-8", 5000);
            logger.info("send recharge response :{}", StringEscapeUtils.unescapeJava(responseBody));

            String retCode = JSONObject.parseObject(responseBody).getString("Code");
            if (StringUtils.equals("200", retCode)) {
                String balance = JSONObject.parseObject(responseBody).getString("Money");
                return new BigDecimal(balance);
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            logger.error("send error", e);
            return BigDecimal.ZERO;
        }
    }
}
