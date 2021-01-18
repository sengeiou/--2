package com.recharge.service.recharge.iml.yx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.GameRechargeInfoBean;
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

/**
 * Created by CAOQI on 2017/8/19.
 * jianguo网络 游戏充值
 *
 *
 * 响应用的同一套东西，请求的地方不一样
 */
@Service
public class JianGuoYyRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());


    public static void main(String[] args) {
        Channel channel = new Channel();
        channel.setConfigInfo("{merchantId:\"10040\"," +
                "md5Key:\"ga4gd2fs334gd345d\"," +
                "rechargeUrl:\"http://www.6078.com/api/pt_ordertrade.asp\"," +
                "callBackUrl:\"http://admin.0527jiexin.com/QueryOrder.aspx\"}");
        ChannelOrder channelOrder = new ChannelOrder();
        channelOrder.setChannelOrderId("PS201703221321003");
        channelOrder.setOrderId("R00000000004");
        RechargeOrderBean rechargeOrderBean= new RechargeOrderBean();
        rechargeOrderBean.setOrderId("R00000000004");
        GameRechargeInfoBean gameRechargeInfoBean = new GameRechargeInfoBean();
        gameRechargeInfoBean.setGameId("2196973346");
        gameRechargeInfoBean.setIp("218.75.123.99");
        gameRechargeInfoBean.setNumber(new BigDecimal(1));
        rechargeOrderBean.setRechargeInfoBeanObj(gameRechargeInfoBean);

        JianGuoYyRechargeServiceImpl jianGuoRechargeService = new JianGuoYyRechargeServiceImpl();
        jianGuoRechargeService.recharge(channel,channelOrder,rechargeOrderBean);
    }

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String merchantId =  configJSONObject.getString("merchantId");
        String md5Key = configJSONObject.getString("md5Key");
        String rechargeUrl =  configJSONObject.getString("rechargeUrl");
        String callBackUrl = configJSONObject.getString("callBackUrl");
//        productid_saleprice
        ProductRelation productRelation  = queryChannelProductId(rechargeOrderBean.getProductName(),"100007");

//        ProductRelation productRelation = new ProductRelation();
//        productRelation.setChannelProductId("590_10");
        String productId = productRelation.getChannelProductId().split("_")[0];
        String salePrice = productRelation.getChannelProductId().split("_")[1];

        GameRechargeInfoBean gameRechargeInfoBean = (GameRechargeInfoBean)rechargeOrderBean.getRechargeInfoObj(GameRechargeInfoBean.class);

        String signSource = gameRechargeInfoBean.getNumber()+gameRechargeInfoBean.getGameId()+merchantId+channelOrder.getChannelOrderId()
                +callBackUrl+productId+salePrice+
                gameRechargeInfoBean.getIp()+md5Key;
        logger.info("orderid:{} . jianguo md5 sourceString:{}",rechargeOrderBean.getOrderId(),signSource);
        String requestUrl = rechargeUrl+"?merchantid="+merchantId+"&productid="+productId+"&buynumber="+gameRechargeInfoBean.getNumber()
                +"&saleprice="+salePrice+"&orderno="+channelOrder.getChannelOrderId()+"&gameid="+gameRechargeInfoBean.getGameId()
                +"&userip="+gameRechargeInfoBean.getIp()+"&notifyurl="+callBackUrl+"&sign="+DigestUtils.md5Hex(signSource);
        try {
            logger.info("orderId :{} , send jianguo requestUrl :{}",rechargeOrderBean.getOrderId(),requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "gb2312", 5000);
            logger.info("orderId:{} response body :{}",rechargeOrderBean.getOrderId(),responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element retCode=root.element("apiresult").element("resultcode");
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
        String merchantId =  configJSONObject.getString("merchantId");
        String md5Key = configJSONObject.getString("md5Key");
        String queryUrl =  configJSONObject.getString("queryUrl");

        String signSource = merchantId+channelOrder.getChannelOrderId()+md5Key;
        logger.info("orderid:{} . jianguo md5 sourceString:{}",channelOrder.getChannelOrderId(),signSource);
        String requestUrl = queryUrl+"?merchantid="+merchantId+"&orderno="+channelOrder.getChannelOrderId()+
                "&sign="+DigestUtils.md5Hex(signSource);
        try {
            logger.info("query orderId :{} , send jianguo requestUrl :{}",channelOrder.getChannelOrderId(),requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "gb2312", 5000);
            logger.info("query orderId:{} response body :{}",channelOrder.getChannelOrderId(),responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element retCode=root.element("apiresult").element("resultcode");
//            查询成功
            if (StringUtils.equals("0", retCode.getStringValue())){
                Element orderStatus=root.element("buycard").element("orderstatus");
                if (StringUtils.equals(orderStatus.getStringValue(), "order_success")){
                    return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
                }else if (StringUtils.equals(orderStatus.getStringValue(), "order_failed")){
                    return new ProcessResult(ProcessResult.FAIL,"交易失败");
                }else if(StringUtils.equals("order_underway",orderStatus.getStringValue())){
                    return new ProcessResult(ProcessResult.UNKOWN,"交易可疑");
                }else {
                    return new ProcessResult(ProcessResult.PROCESSING,"处理中");
                }
            }else{
                return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
            }
        }  catch (ConnectTimeoutException connectException){
            logger.error("{}send error",channelOrder.getChannelOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        String tranState = responseOrder.getResponseCode();

        logger.info("jianguo callback info:{}",JSON.toJSONString(responseOrder));
        if (StringUtils.equals("order_success",tranState)){
            return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
        }else if (StringUtils.equals("order_failed",tranState)){
            return new ProcessResult(ProcessResult.FAIL, responseOrder.getResponseMsg());
        }else{
            return new ProcessResult(ProcessResult.UNKOWN,"未知返回码");
        }
    }
}
