package com.recharge.service.recharge.iml.video;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.center.bean.VideoRechargeInfoBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
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


/**
 * @author Administrator
 * @create 2021/3/2 14:45
 */
@Service
public class DaShangRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String channelId = "";


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        VideoRechargeInfoBean videoRechargeInfoBean = (VideoRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(VideoRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String userId = configJSONObject.getString("userId");
        String privatekey = configJSONObject.getString("privatekey");
        ProductRelation productRelation = queryChannelProductId(channelOrder.getProductName(), channelId);
        String[] split = productRelation.getChannelProductId().split("\\|");
        String itemId = split[0];
//        String itemId = "80000999";
        String itemPrice = new BigDecimal(split[1]).multiply(new BigDecimal("1000")).toString();
        String checkItemFacePrice = new BigDecimal(split[2]).multiply(new BigDecimal("1000")).toString();
        String amt = "1";
        String uid = videoRechargeInfoBean.getAccount();
        String serialno = channelOrder.getChannelOrderId();
        String dtCreate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String md5Str = amt + checkItemFacePrice + dtCreate + itemId + itemPrice + serialno + uid + userId + privatekey;
        String sign = DigestUtils.md5Hex(md5Str);

        String requestUrl = url + "?amt=" + amt + "&checkItemFacePrice=" + checkItemFacePrice +
                "&dtCreate=" + dtCreate + "&itemId=" + itemId +
                "&itemPrice=" + itemPrice + "&serialno=" + serialno + "&uid=" + uid + "&userId=" + userId + "&sign=" + sign;
        try {
            logger.info("达尚下单接口请求的参数:订单号={}，参数={}", serialno, requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "utf-8", 5000);
            logger.info("达尚下单接口响应的参数:订单号={}，参数={}", serialno, responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String code = root.elementText("code");
            if (StringUtils.equals("00", code)) {
                return new ProcessResult(ProcessResult.SUCCESS, "发送充值");
            } else if (StringUtils.equals("23", code) || StringUtils.equals("31", code)) {
                return new ProcessResult(ProcessResult.UNKOWN, "提交未知");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (Exception e) {
            logger.info("达尚下单接口报错的参数:订单号={}，参数={}", serialno, e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "充值未知："+e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userId");
        String privatekey = configJSONObject.getString("privatekey");
        String serialno = channelOrder.getChannelOrderId();
        String sign = DigestUtils.md5Hex(userId+ serialno +privatekey);
        String requestUrl = url + "?userId=" + userId + "&serialno=" + serialno+ "&sign=" + sign;
        try {
            logger.info("达尚查单接口请求的参数:订单号={}，参数={}", serialno, requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "utf-8", 5000);
            logger.info("达尚查单接口响应的参数:订单号={}，参数={}", serialno, responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String status = root.elementText("status");
            if (StringUtils.equals("success", status)) {
                String rechargeStatus = root.element("data").element("status").getTextTrim();
                if (StringUtils.equals("2",rechargeStatus)){
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                }else if(StringUtils.equals("3",rechargeStatus)){
                    String statusDesc = root.element("data").element("statusDesc").getTextTrim();
                    return new ProcessResult(ProcessResult.FAIL, "充值失败:"+statusDesc);
                }else {
                    String statusDesc = root.element("data").element("statusDesc").getTextTrim();
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中："+statusDesc);
                }
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "查询失败");
            }
        } catch (Exception e) {
            logger.info("达尚查单接口响应的参数:订单号={}，参数={}", serialno, e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "查询出错："+e.getMessage());
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("3", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("balanceQueryUrl");
        String userId = configJSONObject.getString("userId");
        String privatekey = configJSONObject.getString("privatekey");
        String sign = DigestUtils.md5Hex(userId + privatekey);
        String requestUrl = url + "?userId=" + userId + "&sign=" + sign;
        try {
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "utf-8", 5000);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String status = root.elementText("status");
            if (StringUtils.equals("success", status)) {
                String balance = root.elementText("balance");
                return new BigDecimal(balance);
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Test
    void test() {
        Channel channel = new Channel();
        ChannelOrder channelOrder = new ChannelOrder();
        channel.setConfigInfo("{\"userId\":\"80800273\",\"rechargeUrl\":\"http://47.114.209.57/unicomAync/buy.do\",\"privatekey\":\"3793cf0b57270b4873949225133976f2\",\"queryUrl\":\"http://47.114.209.57/unicomAync/queryBizOrder.do\",\"balanceQueryUrl\":\"http://47.114.209.57/unicomAync/queryBalance.do\"}");
        channelOrder.setChannelOrderId("cs202103041138");
//        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
        ProcessResult query = query(channel, channelOrder);
    }
}
