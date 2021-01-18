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

/**
 * Created by qi.cao on 2017/6/21.
 * 提供江苏移动100  200  300  500 面值充值
 */
@Service
public class JieXinRechargeService extends AbsChannelRechargeService {

    private Logger logger= LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String rechargeUrl = configJSONObject.getString("rechargeUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("md5Key");
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        String signSource = userId+"&"+channelOrder.getChannelOrderId()
                +"&"+huaFeiRechargeInfoBean.getPhone()+"&"+huaFeiRechargeInfoBean.getAmt()+"&"+key;
        logger.info("orderid:{} . jiexin md5 sourceString:{}",rechargeOrderBean.getOrderId(),signSource);
        String requestUrl = rechargeUrl+"?Phone="+huaFeiRechargeInfoBean.getPhone()
                +"&UserID="+userId+"&OrderNo="+channelOrder.getChannelOrderId()
                +"&Money="+huaFeiRechargeInfoBean.getAmt()
                +"&Sign="+ DigestUtils.md5Hex(userId+"&"+channelOrder.getChannelOrderId()
                +"&"+huaFeiRechargeInfoBean.getPhone()+"&"+huaFeiRechargeInfoBean.getAmt()+"&"+key).toUpperCase();
        try {
            logger.info("orderId :{} , send jiexin requestUrl :{}",rechargeOrderBean.getOrderId(),requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl,"utf-8",5000);
            logger.info("orderId:{} response body :{}",rechargeOrderBean.getOrderId(),responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element retCode=root.element("result");
            if (StringUtils.equals("0", retCode.getStringValue())){
                    return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else{
                return new ProcessResult(ProcessResult.FAIL,"提交失败");
            }
        }  catch (ConnectTimeoutException connectException){
            logger.error("{}send error",rechargeOrderBean.getOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryUrl = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("md5Key");
        String requestUrl = queryUrl+"?UserID="+userId+"&OrderNo="+channelOrder.getChannelOrderId()
                +"&Sign="+ DigestUtils.md5Hex(userId+"&"+channelOrder.getChannelOrderId() +"&"+key).toUpperCase()   ;
        try {
            logger.info("orderId :{} , send jiexin requestUrl :{}",channelOrder.getOrderId(),requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl,"utf-8",5000);
            logger.info("orderId:{} response body :{}",channelOrder.getOrderId(),responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element retCode=root.element("result");
            Element msg=root.element("msg");
            if (StringUtils.equals("1", retCode.getStringValue())){
                return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
            }else if (StringUtils.equals("6", retCode.getStringValue())){
                return new ProcessResult(ProcessResult.PROCESSING,"充值中");
            }else if (StringUtils.equals("2", retCode.getStringValue())){
                return new ProcessResult(ProcessResult.FAIL,"充值失败");
            }else{
                return new ProcessResult(ProcessResult.UNKOWN,"提交可疑"+msg);
            }
        }  catch (ConnectTimeoutException connectException){
            logger.error("{}send error",channelOrder.getOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (Exception e) {
            logger.error("{}send error", channelOrder.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        return null;
    }
}
