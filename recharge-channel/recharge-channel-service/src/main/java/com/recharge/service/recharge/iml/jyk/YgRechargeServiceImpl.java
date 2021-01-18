package com.recharge.service.recharge.iml.jyk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.JykRechargeInfoBean;
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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * zsh0100
 zsh0200
 zsh0500
 zsh1000
 youguang
 */
@Service
public class YgRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);

        String merchantID = configJSONObject.getString("MerchantID");
        String requestUrl = configJSONObject.getString("rechargeUrl");
        String callBackUrl = configJSONObject.getString("callBackUrl");
        String md5Key = configJSONObject.getString("md5Key");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("MerchantID",merchantID);
        requestMap.put("MerchantOrderID", channelOrder.getChannelOrderId());
        String productId = null;
        if (StringUtils.equals(JykRechargeInfoBean.TYPE_ZSH,jykRechargeInfoBean.getType())
        && StringUtils.equals(jykRechargeInfoBean.getAmt().toString(),"100")){
            productId = "zsh0100";
        }else if(StringUtils.equals(JykRechargeInfoBean.TYPE_ZSH,jykRechargeInfoBean.getType())&&
                StringUtils.equals(jykRechargeInfoBean.getAmt().toString(),"200")){
            productId = "zsh0200";
        }else if(StringUtils.equals(JykRechargeInfoBean.TYPE_ZSH,jykRechargeInfoBean.getType())&&
                StringUtils.equals(jykRechargeInfoBean.getAmt().toString(),"500")){
            productId = "zsh0500";
        }else if(StringUtils.equals(JykRechargeInfoBean.TYPE_ZSH,jykRechargeInfoBean.getType())&&
                StringUtils.equals(jykRechargeInfoBean.getAmt().toString(),"1000")){
            productId = "zsh1000";
        }
        requestMap.put("ProductID", productId);

        requestMap.put("Parvalue", jykRechargeInfoBean.getAmt().toString());

        requestMap.put("BuyAmount", "1");
        requestMap.put("TargetAccount", jykRechargeInfoBean.getAccount());
        requestMap.put("CustomerIP",jykRechargeInfoBean.getIp());
        requestMap.put("NotifyUrl",callBackUrl);
        String md5Source = requestMap.get("MerchantID")+
                requestMap.get("MerchantOrderID")+
                requestMap.get("ProductID")+
                requestMap.get("Parvalue")+
                requestMap.get("BuyAmount")+
                requestMap.get("TargetAccount");
        requestMap.put("Sign",DigestUtils.md5Hex(md5Source+md5Key).toUpperCase());


        try {
            logger.info("{},发送充值的参数:{}",rechargeOrderBean.getOrderId(),JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, requestMap, configJSONObject.getString("encoding"));
            logger.info("{},收到充值的响应:{}",rechargeOrderBean.getOrderId(),responseBody);


            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element retCode=root.element("resultno");
            if (StringUtils.equals("000",retCode.getStringValue())
                    ||StringUtils.equals("301",retCode.getStringValue())){
                channelOrder.setOutChannelOrderId(root.element("order").element("orderid").getStringValue());
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }if (StringUtils.equals("204",retCode.getStringValue())){
                return new ProcessResult(ProcessResult.NO_BALANCE,"提交失败");
            }else{
                return new ProcessResult(ProcessResult.FAIL,root.element("retmsg").getStringValue());
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
        return null;
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(),"2")){
            return  new ProcessResult(ProcessResult.FAIL,responseOrder.getResponseMsg());
        }else if(StringUtils.equals(responseOrder.getResponseCode(),"1")){
            return  new ProcessResult(ProcessResult.SUCCESS,"充值成功");
        }

        return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
    }
}
