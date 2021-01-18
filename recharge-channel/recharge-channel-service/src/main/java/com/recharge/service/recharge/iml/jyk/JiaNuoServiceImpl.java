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
import com.recharge.domain.ProductRelation;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class JiaNuoServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String apiKey = configJSONObject.getString("apiKey");
        String Phone="1";
        String CustomerIP="1";
        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("Service" ,"SubmitOrder");
        requestMap.put("UserId" , userId);
        requestMap.put("BizType" , "OIL");
        requestMap.put("OrderNo" , channelOrder.getChannelOrderId());
        ProductRelation productRelation = queryChannelProductId(rechargeOrderBean.getProductName() , "100032");
//        标准产品 ID(签名)
        requestMap.put("ProductId" , productRelation.getChannelProductId());
//        充值目标账号(签名)
        requestMap.put("AccountVal" , jykRechargeInfoBean.getAccount());
        requestMap.put("Phone" , Phone);
//        购买数量(签名)
        requestMap.put("BuyNum" , "1");
        requestMap.put("CustomerIP" ,CustomerIP);

//        时间戳(签名)
        requestMap.put("Time" , (System.currentTimeMillis()/1000)+"");

//          签名值
        String requestUrl=null;
        try {
            requestUrl =createLinkStringByGet(requestMap);
        } catch (UnsupportedEncodingException e) {
            logger.error("UnsupportedEncodingException " ,e);
        }
        requestMap.put("Sign",DigestUtils.md5Hex(requestUrl+apiKey).toLowerCase());
        String requestString = JSONObject.toJSONString(requestMap);

        try {
            logger.info("send recharge request requestUrl:{}",requestMap);
            String responseBody = HttpClientUtils.invokePostString(url,new StringEntity(requestString) , "utf-8", 5000);
            logger.info("send recharge response :{}",responseBody);

            String status = JSONObject.parseObject(responseBody).getString("status");
            String msg = JSONObject.parseObject(responseBody).getString("msg");
            if (StringUtils.equals("ok",status)){
                String orderStatus = JSONObject.parseObject(responseBody).getString("OrderStatus");
                if(StringUtils.equals("SUCCESS" , orderStatus)){
//                    充值成功
                    return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
                }else if(StringUtils.equals("FAILED" , orderStatus)
                        || StringUtils.equals("NOTEXIST" , orderStatus)){
                    return new ProcessResult(ProcessResult.SUCCESS,"提交失败");
                }else if (StringUtils.equals("UNDERWAY" , orderStatus)){
                    return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
                }
            }else{
                return new ProcessResult(ProcessResult.FAIL,msg);
            }
        }catch (ConnectTimeoutException connectException){
            logger.error("{}send error",rechargeOrderBean.getOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (Exception e) {
            logger.error("{}send error",rechargeOrderBean.getOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }


        return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String apiKey = configJSONObject.getString("apiKey");
        String BizType="OIL";
        String OrderNo=channelOrder.getChannelOrderId();
        String Time=(System.currentTimeMillis()/1000)+"";
        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("Service" ,"QueryOrder");
        requestMap.put("UserId" ,userId);
        requestMap.put("OrderNo" ,OrderNo);
        requestMap.put("BizType" ,BizType);
        requestMap.put("Time" , Time);
        String requestUrl=null;
        try {
            requestUrl =createLinkStringByGet(requestMap);
        } catch (UnsupportedEncodingException e) {
            logger.error("UnsupportedEncodingException " ,e);
        }
        requestMap.put("Sign",DigestUtils.md5Hex(requestUrl+apiKey).toLowerCase());

        try {
            logger.info("jia nuo send recharge request requestUrl:{}",requestMap);
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("jia nuo send recharge response :{}",responseBody);
            String code = JSONObject.parseObject(responseBody).getString("code");
            String msg = JSONObject.parseObject(responseBody).getString("msg");
            if (StringUtils.equals(code,"0")){
                String orderStatus = JSONObject.parseObject(responseBody).getString("OrderStatus");
                if (StringUtils.equals(orderStatus,"SUCCESS")){
                    return  new ProcessResult(ProcessResult.SUCCESS,"充值成功");
                }else if(StringUtils.equals(orderStatus,"UNDERWAY")) {
                    return  new ProcessResult(ProcessResult.PROCESSING,"充值中");
                }else if (StringUtils.equals(orderStatus,"FAILED")||StringUtils.equals(orderStatus,"NOTEXIST")){
                    return  new ProcessResult(ProcessResult.FAIL,"充值失败");
                }
            }else {
                return new ProcessResult(ProcessResult.FAIL,msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if(StringUtils.equals("SUCCESS" , responseOrder.getResponseCode())){

//                    充值成功
            return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
        }else if(StringUtils.equals("FAILED" , responseOrder.getResponseCode())
                || StringUtils.equals("NOTEXIST" , responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.FAIL,"充值失败");
        }

        return new ProcessResult(ProcessResult.UNKOWN,"结果可疑");
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String apiKey = configJSONObject.getString("apiKey");
        String BizType="OIL";
        String Time=(System.currentTimeMillis()/1000)+"";
        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("Service","QueryBalance");
        requestMap.put("UserId",userId);
        requestMap.put("BizType",BizType);
        requestMap.put("Time",Time);
        String requestUrl=null;
        try {
            requestUrl =createLinkStringByGet(requestMap);
        } catch (UnsupportedEncodingException e) {
            logger.error("UnsupportedEncodingException " ,e);
        }
        requestMap.put("Sign",DigestUtils.md5Hex(requestUrl+apiKey).toLowerCase());
        try {
            logger.info("send recharge request requestUrl:{}",requestMap);
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("send recharge response :{}",responseBody);

            String code = JSONObject.parseObject(responseBody).getString("code");
            String balance = JSONObject.parseObject(responseBody).getString("Balance");
            BigDecimal newBalance=new BigDecimal(balance);
            return  newBalance.divide(new BigDecimal(100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  null;
    }
}
