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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
 * @create 2020/12/21 10:35
 */
@Service
public class JiangShanHfRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        String userId = configJSONObject.getString("userId");
        String requestUrl = configJSONObject.getString("rechargeUrl");
        String callBackUrl = configJSONObject.getString("callBackUrl");
        String md5Key = configJSONObject.getString("md5Key");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid", userId);
        requestMap.put("price", huaFeiRechargeInfoBean.getAmt().toString());
        requestMap.put("num", "1");
        requestMap.put("mobile", huaFeiRechargeInfoBean.getPhone());
        requestMap.put("spordertime", new SimpleDateFormat("YYYY-MM-DD HH:MM:SS").format(new Date()));
        requestMap.put("sporderid", channelOrder.getChannelOrderId());
        String md5Source = "userid=" + requestMap.get("userid") + "&productid=" + "" + "&price=" + requestMap.get("price") + "&num=1&mobile=" + requestMap.get("mobile") + "&spordertime=" + requestMap.get("spordertime")
                + "&sporderid=" + requestMap.get("sporderid");
        requestMap.put("sign", DigestUtils.md5Hex(md5Source + "&key=" + md5Key));
        requestMap.put("back_url", callBackUrl);
        try {
            logger.info("JiangShanHF,发送充值的参数:{}",JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, requestMap, configJSONObject.getString("encoding"));
            logger.info("JiangShanHF,收到充值的响应:{}",responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element retCode = root.element("resultno");
            if (StringUtils.equals("0", retCode.getStringValue())) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals("1", retCode.getStringValue())) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals("2", retCode.getStringValue())) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (Exception e) {
            logger.error("{}JiangShanHF,发送充值的错误信息{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑原因=" + e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String userId = configJSONObject.getString("userId");
        String requestUrl = configJSONObject.getString("queryUrl");
        String payno = configJSONObject.getString("payno");
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid", userId);
        requestMap.put("sporderid", channelOrder.getChannelOrderId());
        requestMap.put("payno", payno);
        try {
            logger.info("江山查询的请求参数:{}", JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, requestMap, configJSONObject.getString("encoding"));
            logger.info("江山查询的返回参数:{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String retCode = root.elementText("resultno");
            String element = root.elementText("payno");
            if (StringUtils.equals("0", retCode)
                    || StringUtils.equals("2", retCode)) {
                return new ProcessResult(ProcessResult.PROCESSING, "处理中");
            } else if (StringUtils.equals("1", retCode)) {
                channelOrder.setOutChannelOrderId(element);
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals("9", retCode)) {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "查询可疑");
            }
        } catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
        } else if (StringUtils.equals("9", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "提交失败");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryUrl = configJSONObject.getString("queryBalanceUrl");
        String userId = configJSONObject.getString("userId");
        String md5Key = configJSONObject.getString("md5Key");
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userid", userId);
        requestMap.put("sign", DigestUtils.md5Hex("userid=" + userId + "&key=" + md5Key));
        try {
            logger.info("send queryBalance request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String balance = root.elementText("balance");
            return new BigDecimal(balance);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Test
    public void test(){
        JiangShanHfRechargeServiceImpl jiangShanHfRechargeService = new JiangShanHfRechargeServiceImpl();
        Channel channel = new Channel();
        channel.setConfigInfo("{userId:\"10020917\",rechargeUrl:\"http://esales.fhcard.com/onlinepay.do\",queryUrl:\"http://esales.fhcard.com/searchpay.do\",md5Key:\"AD7RwBEYjGbYF78H5mr6HmsEzThPHJ5K\",callBackUrl:\"http://139.129.85.83:8082/jiangshanHF/callBack\",queryBalanceUrl:\"http://esales.fhcard.com/searchbalance.do\"} ");
        jiangShanHfRechargeService.balanceQuery(channel);
    }
}
