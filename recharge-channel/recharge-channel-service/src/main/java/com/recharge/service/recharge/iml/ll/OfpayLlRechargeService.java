package com.recharge.service.recharge.iml.ll;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.FlowRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by CAOQI on 2017/4/28.
 */
@Service
public class OfpayLlRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        ProductRelation productRelation  = queryChannelProductId(rechargeOrderBean.getProductName(),"100005");
//        ProductRelation productRelation = new ProductRelation();
//        productRelation.setChannelProductId("5_30M_1_1_1_4G");
        if (productRelation == null){
            logger.info("channel product is null,{}",rechargeOrderBean.getProductName());
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }
        FlowRechargeInfoBean flowRechargeInfoBean = (FlowRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(FlowRechargeInfoBean.class);

        String userId = configJSONObject.getString("userId");
        String userPws = configJSONObject.getString("userPws");
        String requestUrl = configJSONObject.getString("rechargeUrl");
        String callBackUrl = configJSONObject.getString("callBackUrl");
        String md5Key = configJSONObject.getString("md5Key");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid",userId);
        requestMap.put("userpws", DigestUtils.md5Hex(userPws));

//        手机号
        requestMap.put("phoneno", flowRechargeInfoBean.getPhone());
//面值(请按照流量产品文档中对应商品输入)
        requestMap.put("perValue", productRelation.getChannelProductId().split("_")[0]);
//流量值 (请按照流量产品文档中对应商品输入)  30M
        requestMap.put("flowValue", productRelation.getChannelProductId().split("_")[1]);
//        使用范围 1（省内）、 2（全国）
        requestMap.put("range", productRelation.getChannelProductId().split("_")[2]);

//        生效时间 1（当日）、2（次日）、3（次月）
        requestMap.put("effectStartTime", productRelation.getChannelProductId().split("_")[3]);

/*
* 1-当月有效,2-30天有效,3-半年有效,4-3个月有效,5-2个月
有效,6-6个月有效,7-20天有效,8-3日有效,9-90天有效,10-
7天有效
*/
        requestMap.put("effectTime", productRelation.getChannelProductId().split("_")[4]);

//        网络制式 2G、3G、4G(可不传，不传默认4GGG依次匹配)
        requestMap.put("netType", productRelation.getChannelProductId().split("_")[5]);

//       Sp商家的订单号（商户传给欧飞的唯一订单编号）
        requestMap.put("sporderId", channelOrder.getChannelOrderId());
        String md5Source = requestMap.get("userid") + requestMap.get("userpws") + requestMap.get("phoneno")
                + requestMap.get("perValue") +requestMap.get("flowValue") + requestMap.get("range") + requestMap.get("effectStartTime") +
                requestMap.get("effectTime")+ requestMap.get("netType")+ requestMap.get("sporderId");
        requestMap.put("md5Str",DigestUtils.md5Hex(md5Source+md5Key).toUpperCase());
        requestMap.put("retUrl",callBackUrl);
        requestMap.put("version","6.0");


        try {
            logger.info("{},发送充值的参数:{}",rechargeOrderBean.getOrderId(),JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, requestMap, configJSONObject.getString("encoding"));
            logger.info("{},收到充值的响应:{}",rechargeOrderBean.getOrderId(),responseBody);


            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element retCode=root.element("retcode");
            Element retMsg = root.element("err_msg");
            if (StringUtils.equals("1",retCode.getStringValue())){
                Element gameState=root.element("game_state");
                Element ofpayOrderId = root.element("orderid");
                channelOrder.setOutChannelOrderId(ofpayOrderId.getStringValue());
                if (StringUtils.equals("9",gameState.getStringValue())){
                    return new ProcessResult(ProcessResult.FAIL,"提交失败");
                }else{
                    return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
                }
            }else{
                return new ProcessResult(ProcessResult.FAIL,retMsg.getStringValue());
            }


        } catch (ConnectTimeoutException connectException){
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

        String userId = configJSONObject.getString("userId");
        String requestUrl = configJSONObject.getString("rechargeUrl");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid",userId);
        requestMap.put("spbillid", channelOrder.getChannelOrderId());

        try {
            logger.info("{},发送充值的参数:{}",channelOrder.getOrderId(),JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, requestMap, configJSONObject.getString("encoding"));
            logger.info("{},收到充值的响应:{}",channelOrder.getOrderId(),responseBody);

            if (StringUtils.equals("1",responseBody)){
                    return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
            }else if (StringUtils.equals("9",responseBody)){
                return new ProcessResult(ProcessResult.FAIL,"充值失败");
            }else{
                return new ProcessResult(ProcessResult.PROCESSING,"处理中");
            }


        }catch (Exception e) {
            logger.error("{}send error",channelOrder.getOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        /*0-生成订单、1-处理中、
        2-交易成功、3-交易失败
                */
        String tranState = responseOrder.getResponseCode();
        if (StringUtils.equals("1",tranState)){
            return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
        }else if (StringUtils.equals("9",tranState)){
            return new ProcessResult(ProcessResult.FAIL, responseOrder.getResponseMsg());
        }else{
            return new ProcessResult(ProcessResult.UNKOWN,"未知返回码");
        }
    }
}
