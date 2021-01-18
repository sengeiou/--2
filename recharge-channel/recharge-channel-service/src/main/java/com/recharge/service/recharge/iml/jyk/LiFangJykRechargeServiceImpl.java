package com.recharge.service.recharge.iml.jyk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianmi.open.api.ApiException;
import com.qianmi.open.api.DefaultOpenClient;
import com.qianmi.open.api.OpenClient;
import com.qianmi.open.api.request.BmOrderCustomGetRequest;
import com.qianmi.open.api.request.FinanceGetAcctInfoRequest;
import com.qianmi.open.api.response.BmOrderCustomGetResponse;
import com.qianmi.open.api.response.FinanceGetAcctInfoResponse;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.JykRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.utils.LiFangSignUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020/12/28 10:16
 */
@Service
public class LiFangJykRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("url");
        String appSecret = configJSONObject.getString("appSecret");
        String accessToken = configJSONObject.getString("access_token");
        String callBack = configJSONObject.getString("callBack");
        ProductRelation productRelation = queryChannelProductId(rechargeOrderBean.getProductName(), "100120");
        String item_id = productRelation.getChannelProductId();
        Map<String, String> map = new HashMap<>();
        map.put("method", "bm.elife.gasCard.payBill");
        map.put("access_token", accessToken);
        map.put("outerTid", channelOrder.getChannelOrderId());
        map.put("v", "1.1");
        map.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        map.put("itemId", item_id);
        map.put("gasCardNo", jykRechargeInfoBean.getAccount());
        map.put("callback", callBack);
        try {
            String sign = LiFangSignUtils.sign(map, appSecret);
            map.put("sign", sign);
            logger.info("LiFangJyk send recharge request params:" + JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("LiFangJyk send recharge response:" + JSON.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("code");
            String message = JSONObject.parseObject(responseBody).getString("message");
            if (StringUtils.isBlank(message)) {
                return new ProcessResult(ProcessResult.SUCCESS, "发送充值成功");
            } else {
                if (StringUtils.equals("9", code)) {
                    return new ProcessResult(ProcessResult.FAIL, "发送充值失败" + message);
                } else {
                    return new ProcessResult(ProcessResult.UNKOWN, "充值请求可疑");
                }
            }
        } catch (Exception e) {
            logger.error("LiFangJyk send error:" + e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "充值请求可疑");
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
            logger.info("LiFangJyk send query request:{}", JSON.toJSONString(req));
            BmOrderCustomGetResponse response = client.execute(req, accessToken);
            logger.info("LiFangJyk send query response param:{}", JSONObject.toJSONString(response));
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
            logger.error("send error", e);
            e.printStackTrace();
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
            OpenClient client = new DefaultOpenClient(url, appKey, appSecret);
            FinanceGetAcctInfoRequest req = new FinanceGetAcctInfoRequest();
            FinanceGetAcctInfoResponse response = client.execute(req, accessToken);
            String balance = response.getAcctInfo().getBalance();
            return new BigDecimal(balance);
        } catch (ApiException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    @Test
    public void test() {
        LiFangJykRechargeServiceImpl liFangJykRechargeService = new LiFangJykRechargeServiceImpl();
        Channel channel = new Channel();
//        channel.setConfigInfo("{url:\"http://api.bm001.com/api\",appKey:\"10002723\",appSecret:\"rUmpzVCb0alZyQteazNJrHXLLbCM7J6H\",access_token:\"dc4e2939e3244863855e38f58f205344\",callBack:\"http://139.129.85.83:8082/liFangJyk/callBack\"}");
        channel.setConfigInfo("{url:\"http://api.bm001.com/api\",appKey:\"10002723\",appSecret:\"rUmpzVCb0alZyQteazNJrHXLLbCM7J6H\",access_token:\"dc4e2939e3244863855e38f58f205344\",callBack:\"http://139.129.85.83:8082/liFangJyk/callBack\"}");
//        ChannelOrder channelOrder = new ChannelOrder();
        BigDecimal bigDecimal = liFangJykRechargeService.balanceQuery(channel);
        ChannelOrder channelOrder = new ChannelOrder();
//        channelOrder.setChannelOrderId("cs202012301723");
//        channelOrder.setChannelOrderId("cs202012311101");
        channelOrder.setChannelOrderId("cs202012311318");
//        ProcessResult query = liFangJykRechargeService.query(channel, channelOrder);
//        ProcessResult recharge = liFangJykRechargeService.recharge(channel, channelOrder, new RechargeOrderBean());
        System.out.println(1);
    }

}
