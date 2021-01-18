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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qi.cao
 */
@Service
public class HaoChongRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");
        String callback = configJSONObject.getString("callback");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userid", userId);
        requestMap.put("price", huaFeiRechargeInfoBean.getAmt().toString());
        requestMap.put("num", "1");
        requestMap.put("mobile", huaFeiRechargeInfoBean.getPhone());
        requestMap.put("spordertime", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        requestMap.put("sporderid", channelOrder.getChannelOrderId());
        requestMap.put("back_url", callback);
        requestMap.put("sign", DigestUtils.md5Hex("userid=" + requestMap.get("userid") + "&productid=&price=" + requestMap.get("price") + "&num=1&mobile="
                + requestMap.get("mobile") + "&spordertime=" + requestMap.get("spordertime") + "&sporderid=" + requestMap.get("sporderid") + "&key=" + key));
        try {
            logger.info("send recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            if (StringUtils.equals("0", resultno)
                    || StringUtils.equals("9999", resultno)
                    || StringUtils.equals("2", resultno)) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String queryUrl = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userId");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userid", userId);
        requestMap.put("sporderid", channelOrder.getChannelOrderId());
        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            if (StringUtils.equals("1", resultno)) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals("9", resultno)
                    || StringUtils.equals("5007", resultno)) {
                return new ProcessResult(ProcessResult.FAIL, "查询失败");
            } else if (StringUtils.equals("2", resultno)
                    || StringUtils.equals("0", resultno)) {
                return new ProcessResult(ProcessResult.PROCESSING, "处理中");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "可疑");
            }

        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "处理中");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("9", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } else if (StringUtils.equals("2", responseOrder.getResponseCode())
                || StringUtils.equals("0", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.PROCESSING, "处理中");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      余额查询地址：http://180.96.21.204:8082/searchbalance.do
        String queryUrl = configJSONObject.getString("queryBalanceUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userid", userId);
        requestMap.put("sign", DigestUtils.md5Hex("userid=" + requestMap.get("userid") + "&key=" + key));

        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            if (StringUtils.equals("1", resultno)) {
                String balance = root.elementText("balance");
                return new BigDecimal(balance);
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }

    }
}
