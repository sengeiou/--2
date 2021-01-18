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
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * xiangshang 充值接口
 */
@Service
public class XiangShangRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {

        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String macid = configJSONObject.getString("macid");
        String md5Key = configJSONObject.getString("md5Key");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("deno", huaFeiRechargeInfoBean.getAmt().toString());
        requestMap.put("encryptType", "MD5");
        requestMap.put("macid", macid);
        requestMap.put("orderid", macid + channelOrder.getChannelOrderId());
        requestMap.put("phone", huaFeiRechargeInfoBean.getPhone());
        requestMap.put("time", System.currentTimeMillis() / 1000 + "");

        String souceString = "deno" + requestMap.get("deno")
                + "macid" + requestMap.get("macid") + "orderid" + requestMap.get("orderid") + "phone" + requestMap.get("phone") + "time" + requestMap.get("time");

        requestMap.put("sign", DigestUtils.md5Hex(souceString + md5Key));

        try {
            logger.info("send recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();

            String code = root.elementText("errcode");
            String errinfo = root.elementText("errinfo");
            String voucher = root.elementText("voucher");
            channelOrder.setOutChannelOrderId(voucher);
            if (StringUtils.equals("OrderSended", code) || StringUtils.equals("OrderExists", code)) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, errinfo);
            }


        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (SocketTimeoutException socketTimeoutException) {
            return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String queryUrl = configJSONObject.getString("queryUrl");
        String macid = configJSONObject.getString("macid");
        String md5Key = configJSONObject.getString("md5Key");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("encryptType", "MD5");
        requestMap.put("macid", macid);
        requestMap.put("orderid", macid + channelOrder.getChannelOrderId());
        requestMap.put("time", System.currentTimeMillis() / 1000 + "");

        String souceString = "macid" + requestMap.get("macid") + "orderid" + requestMap.get("orderid") + "time" + requestMap.get("time");

        System.out.println(souceString + md5Key);
        requestMap.put("sign", DigestUtils.md5Hex(souceString + md5Key));

        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();

            String code = root.elementText("errcode");
            String voucher = root.elementText("voucher");
            channelOrder.setOutChannelOrderId(voucher);
            if (StringUtils.equals("OrderSended", code)) {
                return new ProcessResult(ProcessResult.PROCESSING, "处理中");
            } else if (StringUtils.equals("OrderSuccess", code)) {
                return new ProcessResult(ProcessResult.SUCCESS, "交易成功");
            } else if (StringUtils.equals("OrderFail", code)
                    || StringUtils.equals("ConfigError", code)
                    || StringUtils.equals("OrderNotExists", code)) {
                return new ProcessResult(ProcessResult.FAIL, "交易失败");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
            }


        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "处理中");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("OrderSuccess", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "交易成功");
        } else if (StringUtils.equals("OrderFail", responseOrder.getResponseCode())
                || StringUtils.equals("ConfigError", responseOrder.getResponseCode())
                || StringUtils.equals("OrderNotExists", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "交易失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      余额查询地址：http://shop.test.bolext.cn:81/shop/buyunit/balance.do
        String queryUrl = configJSONObject.getString("queryBalanceUrl");
        String macid = configJSONObject.getString("macid");
        String md5Key = configJSONObject.getString("md5Key");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("encryptType", "MD5");
        requestMap.put("macid", macid);
        requestMap.put("time", System.currentTimeMillis() / 1000 + "");

        String souceString = "macid" + requestMap.get("macid") + "time" + requestMap.get("time");

        System.out.println(souceString + md5Key);
        requestMap.put("sign", DigestUtils.md5Hex(souceString + md5Key));

        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();

            String balance = root.elementText("balance");
            return new BigDecimal(balance);

        } catch (Exception e) {
            logger.error("send error", e);
            return BigDecimal.ZERO;
        }
    }
}
