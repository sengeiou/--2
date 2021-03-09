package com.recharge.service.recharge.iml.hf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianmi.open.api.ApiException;
import com.qianmi.open.api.DefaultOpenClient;
import com.qianmi.open.api.OpenClient;
import com.qianmi.open.api.request.BmOrderCustomGetRequest;
import com.qianmi.open.api.request.BmRechargeMobilePayBillRequest;
import com.qianmi.open.api.request.FinanceGetAcctInfoRequest;
import com.qianmi.open.api.response.BmOrderCustomGetResponse;
import com.qianmi.open.api.response.BmRechargeMobilePayBillResponse;
import com.qianmi.open.api.response.FinanceGetAcctInfoResponse;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.HuaFeiRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * @author Administrator
 * @create 2021/3/9 10:22
 */
@Service
public class LiFangHFNewRechargeService extends AbsChannelRechargeService{
    //日志文件
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        //获取渠道类配置信息
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        //获取请求地址
        String url = configJSONObject.getString("url");
        //获取appkey
        String appKey = configJSONObject.getString("appKey");
        //获取appSecret
        String appSecret = configJSONObject.getString("appSecret");
        //获取access_token
        String accessToken = configJSONObject.getString("access_token");
        //获取充值账号
        String rechargeNumber = rechargeOrderBean.getRechargeNumber();
        //获取充值的面额
        String rechargeAmount = huaFeiRechargeInfoBean.getAmt().toString();
        //获取我们生成的订单号
        String channelOrderId = channelOrder.getChannelOrderId();
        //获取回调接口地址
        String callback = configJSONObject.getString("callBack");
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("appkey", appKey);
        requestMap.put("appsecret", appSecret);
        requestMap.put("accessToken", accessToken);
        requestMap.put("mobileNo", rechargeNumber);
        requestMap.put("rechargeAmount", rechargeAmount);
        requestMap.put("outerTid", channelOrderId);
        requestMap.put("callback", callback);
        //logger.info("LiFangNew send recharge request params:{}", JSONObject.toJSONString(requestMap));
        OpenClient client = new DefaultOpenClient(url, appSecret);
        BmRechargeMobilePayBillRequest req = new BmRechargeMobilePayBillRequest();
        req.setMobileNo(rechargeNumber);
        req.setRechargeAmount(rechargeAmount);
        req.setOuterTid(channelOrderId);
        req.setCallback(callback);
        try {
            logger.info("LiFangHFNew send recharge request params{} , LiFangNew send recharge BmRechargeMobilePayBillRequest:{}", JSON.toJSONString(requestMap),JSON.toJSONString(req));
            BmRechargeMobilePayBillResponse response = client.execute(req, accessToken);
            logger.info("LiFangHFNew send recharge response:{}", JSON.toJSONString(response));
            if(response.isSuccess()){ //订单发送成功
                if (StringUtils.equals(response.getOrderDetailInfo().getRechargeState(), "9")) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else if (StringUtils.equals(response.getOrderDetailInfo().getRechargeState(), "1")) {
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (StringUtils.equals(response.getOrderDetailInfo().getRechargeState(), "0")) {
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                } else {
                    return new ProcessResult(ProcessResult.UNKOWN, "充值请求可疑");
                }
            }else if(!response.isSuccess()){ //订单发送失败
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            }else{
                return new ProcessResult(ProcessResult.UNKOWN, "订单状态可疑");
            }
        } catch (ApiException e) {
            logger.error("LiFangHFNew send error", e);
            return new ProcessResult(ProcessResult.UNKOWN, "充值请求可疑原因为"+ e.getErrMsg());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        //获取渠道类配置信息
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        //获取请求地址
        String url = configJSONObject.getString("url");
        //获取appkey
        String appKey = configJSONObject.getString("appKey");
        //获取appSecret
        String appSecret = configJSONObject.getString("appSecret");
        //获取access_token
        String accessToken = configJSONObject.getString("access_token");
        //获取我们的订单号
        String channelOrderId = channelOrder.getChannelOrderId();
        //将请求类的配置信息封装进一个Map中
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("appkey", appKey);
        requestMap.put("appsecret", appSecret);
        requestMap.put("accessToken", accessToken);
        OpenClient client = new DefaultOpenClient(url, appSecret);
        BmOrderCustomGetRequest req = new BmOrderCustomGetRequest();

        req.setOuterTid(channelOrderId);
        try {
            logger.info("LiFangHFNew send query request:{}", JSON.toJSONString(req));
            BmOrderCustomGetResponse response = client.execute(req, accessToken);
            logger.info("LiFangHFNew send query response param:{}", JSONObject.toJSONString(response));
            if (response.getOrderDetailInfo() == null
                    || StringUtils.equals(response.getOrderDetailInfo().getRechargeState(), "9")) {
                return new ProcessResult(ProcessResult.FAIL, response.getSubMsg());
            } else if (response.getOrderDetailInfo() == null
                    || StringUtils.equals(response.getOrderDetailInfo().getRechargeState(), "1")) {
                return new ProcessResult(ProcessResult.SUCCESS, response.getSubMsg());
            } else if (response.getOrderDetailInfo() == null
                    || StringUtils.equals(response.getOrderDetailInfo().getRechargeState(), "0")) {
                return new ProcessResult(ProcessResult.PROCESSING, response.getSubMsg());
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "请求可疑");
            }
        } catch (ApiException e) {
            logger.error("LiFangHFNew send error", e);
            return new ProcessResult(ProcessResult.UNKOWN, "请求可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("9", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "状态可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        //获取渠道类配置信息
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        //获取请求地址
        String url = configJSONObject.getString("url");
        //获取appkey
        String appKey = configJSONObject.getString("appKey");
        //获取appSecret
        String appSecret = configJSONObject.getString("appSecret");
        //获取access_token
        String accessToken = configJSONObject.getString("access_token");
        //将请求类的配置信息封装进一个Map中
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("appkey", appKey);
        requestMap.put("appsecret", appSecret);
        requestMap.put("accessToken", accessToken);
        try {
            logger.info("LiFangHFNew send balanceQuery request params:{}", JSONObject.toJSONString(requestMap));
            OpenClient client = new DefaultOpenClient(url, appKey, appSecret);
            FinanceGetAcctInfoRequest req = new FinanceGetAcctInfoRequest();
            FinanceGetAcctInfoResponse response = client.execute(req, accessToken);
            logger.info("LiFangHFNew send balanceQuery response:{}", response.toString());
            String balance = response.getAcctInfo().getBalance();
            return new BigDecimal(balance);
        } catch (ApiException e) {
            logger.error("send error", e);
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }
    @Test
    void test(){
        ChannelOrder channelOrder = new ChannelOrder();
        Channel channel = new Channel();
        channel.setConfigInfo("{url:\"http://api.bm001.com/api\",appKey:\"10002814\",appSecret:\"zMWsBlsO6IE43kuh0zQ3fWcQp4EV28yF\",access_token:\"4445cd08be7a44dea9ed66ab2013511f\",callBack:\"http://139.129.85.83:8082/liFangJyk/callBack\"}");
        BigDecimal bigDecimal = balanceQuery(channel);
    }
}
