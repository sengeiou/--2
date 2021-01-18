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
import java.net.URLEncoder;

@Service
public class XingChenSupServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        Channel channel = new Channel();
        channel.setConfigInfo("{coopId:\"10002\"," +
                "md5Key:\"356d943c6b2e3150e88070edc00d9b77\"," +
                "rechargeUrl:\"http://api.myxcwl.cn/Service/PostOrder.aspx\"," +
                "queryUrl:\"http://api.myxcwl.cn/Service/GetOrder.aspx\"}");
        ChannelOrder channelOrder = new ChannelOrder();
        channelOrder.setChannelOrderId("PS201703221321003");
        channelOrder.setOrderId("R00000000003");
        RechargeOrderBean rechargeOrderBean= new RechargeOrderBean();
        rechargeOrderBean.setOrderId("R00000000003");
        GameRechargeInfoBean gameRechargeInfoBean = new GameRechargeInfoBean();
        gameRechargeInfoBean.setGameId("389063097");
        gameRechargeInfoBean.setIp("218.75.123.99");
        gameRechargeInfoBean.setNumber(new BigDecimal(1));
        rechargeOrderBean.setRechargeInfoBeanObj(gameRechargeInfoBean);

        XingChenSupServiceImpl jianGuoRechargeService = new XingChenSupServiceImpl();
        jianGuoRechargeService.query(channel,channelOrder);
    }

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {

        GameRechargeInfoBean gameRechargeInfoBean = (GameRechargeInfoBean)rechargeOrderBean.getRechargeInfoObj(GameRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String rechargeUrl =  configJSONObject.getString("rechargeUrl");
        String coopId = configJSONObject.getString("coopId");
        String md5Key =  configJSONObject.getString("md5Key");

        try {
        String requestUrl = rechargeUrl +"?tranid="+channelOrder.getChannelOrderId()
                +"&coopid="+coopId+"&proid=10031&quantity="+gameRechargeInfoBean.getNumber()
                +"&account="+gameRechargeInfoBean.getGameId()+"&chargetype="+ URLEncoder.encode("Q币","utf-8")+"&sign="+
                DigestUtils.md5Hex("tranid"+channelOrder.getChannelOrderId()+"coopid"+coopId
                        +"proid10031quantity"+gameRechargeInfoBean.getNumber()
                        +"account"+gameRechargeInfoBean.getGameId()+md5Key);
            logger.info("request param url:{}" , requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl , "utf-8" ,4000);
            logger.info("response body :{}" , responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element resultCode=root.element("resultCode");
            Element resultDesc=root.element("resultDesc");
            if (StringUtils.equals(resultCode.getStringValue() , "0000")){
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else if (StringUtils.equals(resultCode.getStringValue() , "9999")){
                return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
            }else{
                return new ProcessResult(ProcessResult.FAIL,resultDesc.getStringValue());
            }
        }  catch (ConnectTimeoutException connectException){
            logger.error("{}send error",rechargeOrderBean.getOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        } catch (Exception e) {
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryUrl =  configJSONObject.getString("queryUrl");
        String coopId = configJSONObject.getString("coopId");
        String md5Key =  configJSONObject.getString("md5Key");

        try {
            String requestUrl = queryUrl + "?coopid="+coopId+"&tranid="+channelOrder.getChannelOrderId()
                    +"&sign="+DigestUtils.md5Hex("coopid"+coopId+"tranid"+channelOrder.getChannelOrderId()+md5Key);
            logger.info("request param url:{}" , requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl , "utf-8" ,4000);
            logger.info("response body :{}" , responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element resultCode=root.element("resultCode");
            Element resultDesc=root.element("resultDesc");
            if (StringUtils.equals(resultCode.getStringValue() , "0000")){
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else if (StringUtils.equals(resultCode.getStringValue() , "9999")){
                return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
            }else{
                return new ProcessResult(ProcessResult.FAIL,resultDesc.getStringValue());
            }
        }  catch (ConnectTimeoutException connectException){
            return new ProcessResult(ProcessResult.PROCESSING,"查询失败");
        } catch (Exception e) {
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("SUCCESS" , responseOrder.getResponseCode())){

            return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
        }else if(StringUtils.equals("CANCEL" , responseOrder.getResponseCode())
                || StringUtils.equals("FAILED" , responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }else {
            return new ProcessResult(ProcessResult.UNKOWN,"充值可疑");
        }
    }
}
