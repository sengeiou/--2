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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class JiaoFeiJyk100Service extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String apiId = configJSONObject.getString("apiId");
        String apiKey = configJSONObject.getString("apiKey");
        String requestTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        String tradeType = StringUtils.equals(JykRechargeInfoBean.TYPE_ZSH,jykRechargeInfoBean.getType())?"48":"49";
        String souceString ="APIID="+apiId+"&Account="+jykRechargeInfoBean.getAccount()+"&BuyNum="+1+"&CreateTime="
                +requestTime+"&isCallBack=1&OrderID="+channelOrder.getChannelOrderId()+"&TotalPrice="+new BigDecimal(jykRechargeInfoBean.getAmt()).multiply(new BigDecimal("1000"))
                +"&TradeType="+tradeType+"&UnitPrice="+new BigDecimal(jykRechargeInfoBean.getAmt()).multiply(new BigDecimal("1000"))+"&APIKEY="+apiKey;
//        tradetype 10 话费直充
        StringBuffer requestUrl = new StringBuffer(url).append("?APIID=").append(apiId).append("&TradeType="+tradeType).append("&Account=")
                .append(jykRechargeInfoBean.getAccount()).append("&UnitPrice=").append(new BigDecimal(jykRechargeInfoBean.getAmt()).multiply(new BigDecimal("1000")))
                .append("&BuyNum=").append(1).append("&TotalPrice=").append(new BigDecimal(jykRechargeInfoBean.getAmt()).multiply(new BigDecimal("1000")))
                .append("&OrderID=").append(channelOrder.getChannelOrderId()).append("&CreateTime=").append(requestTime)
                .append("&IsCallBack=1&Sign=").append(DigestUtils.md5Hex(souceString).toUpperCase());
        try {
            logger.info("send recharge request requestUrl:{}",requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl.toString(),"utf-8",5000);
            logger.info("send recharge response :{}",responseBody);

            String retCode = JSONObject.parseObject(responseBody).getString("Code");
            String returnOrderID = JSONObject.parseObject(responseBody).getString("ReturnOrderID");
            if (StringUtils.equals("10018",retCode)){
                channelOrder.setOutChannelOrderId(returnOrderID);
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else if (StringUtils.equals("10008",retCode) ||
                    StringUtils.equals("10022",retCode)){
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else{
                return new ProcessResult(ProcessResult.FAIL,"提交失败");
            }
        }catch (ConnectTimeoutException connectException){
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


        String queryUrl = configJSONObject.getString("queryUrl");
        String apiId = configJSONObject.getString("apiId");
        String apiKey = configJSONObject.getString("apiKey");
        String souceString ="APIID="+apiId+"&OrderID="+channelOrder.getChannelOrderId()+"&APIKEY="+apiKey;
//        tradetype 10 话费直充
        StringBuffer requestUrl = new StringBuffer(queryUrl).append("?APIID="+apiId+"&OrderID="+channelOrder.getChannelOrderId()+
                "&Sign=").append(DigestUtils.md5Hex(souceString).toUpperCase());
        try {
            logger.info("send query request requestUrl:{}",requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl.toString(),"utf-8",5000);
            logger.info("send query response :{}",responseBody);

            String retCode = JSONObject.parseObject(responseBody).getString("Code");
            if (StringUtils.equals("10027",retCode)){
                return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
            }else if (StringUtils.equals("10026",retCode)){
                return new ProcessResult(ProcessResult.FAIL,"交易失败");
            }else if (StringUtils.equals("10029",retCode)){
                return new ProcessResult(ProcessResult.UNKOWN,"交易可疑");
            }else{
                return new ProcessResult(ProcessResult.PROCESSING,"处理中");
            }
        }catch (Exception e) {
            logger.error("{}send error",channelOrder.getChannelOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        /*10025 订单处理中 查询订单状态时返回
10026 交易失败 查询订单状态时返回
10027 交易成功 查询订单状态时返回
                */
        String responseCode = responseOrder.getResponseCode();
        if (StringUtils.equals("10027",responseCode)){
            return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
        }else if (StringUtils.equals("10026",responseCode)){
            return new ProcessResult(ProcessResult.FAIL, responseOrder.getResponseMsg());
        }else{
            return new ProcessResult(ProcessResult.UNKOWN,"未知返回码");
        }
    }
}
