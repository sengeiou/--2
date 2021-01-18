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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 聚卡
 */
@Service
public class JuKaService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String rechargeUrl = configJSONObject.getString("rechargeUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");
        String backUrl = configJSONObject.getString("backUrl");
        Map<String,String> requestMap = new HashMap<String, String>();
        try {
            requestMap.put("userid" ,userId);
            requestMap.put("productid" ,null);
            requestMap.put("price" , huaFeiRechargeInfoBean.getAmt().toString());
            requestMap.put("num" , "1");
            requestMap.put("mobile" , huaFeiRechargeInfoBean.getPhone());
            requestMap.put("spordertime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            requestMap.put("sporderid" , channelOrder.getChannelOrderId());

            String sourceString = "userid="+requestMap.get("userid")+"&productid=&price="+requestMap.get("price")
                    +"&num=1&mobile="+requestMap.get("mobile")+"&spordertime="+requestMap.get("spordertime")
                    +"&sporderid="+requestMap.get("sporderid")+"&key="+key;
            requestMap.put("sign",DigestUtils.md5Hex(sourceString));

            requestMap.put("back_url" , backUrl);

            logger.info("send recharge request requestUrl:{}",JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(rechargeUrl,requestMap,"utf-8",5000);
            logger.info("send recharge response :{}",responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element order = document.getRootElement();
            String code = order.element("resultno").getStringValue();
            if(StringUtils.equals("0" , code)
                    ||StringUtils.equals("1" , code)
                    ||StringUtils.equals("2" , code)){
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else if(Arrays.binarySearch(new String[]{"5001","5002","5003","5004","5005",
                    "5008","5009","5010","5011","5012"},code)!=-1){
                return new ProcessResult(ProcessResult.FAIL,"提交失败");
            }else {
                return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
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
        String userId = configJSONObject.getString("userId");

        Map<String,String> requestMap = new HashMap<String, String>();
        try {

            requestMap.put("userid" , userId);
            requestMap.put("sporderid" , channelOrder.getChannelOrderId());
            logger.info("send query request requestUrl:{}",JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl,requestMap ,"utf-8",5000);
            logger.info("send query response :{}",responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element order = document.getRootElement();
            String code = order.element("resultno").getStringValue();
            if(StringUtils.equals("1" , code)){
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else if(StringUtils.equals("9" , code)
                    || StringUtils.equals("5007",code)){
                return new ProcessResult(ProcessResult.FAIL,"提交失败");
            }else {
                return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
            }
        }catch (Exception e) {
            logger.error("{}send error",channelOrder.getChannelOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        String responseCode = responseOrder.getResponseCode();
        if (StringUtils.equals("1",responseCode)){
            return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
        }else if (StringUtils.equals("9",responseCode)){
            return new ProcessResult(ProcessResult.FAIL, responseOrder.getResponseMsg());
        }else{
            return new ProcessResult(ProcessResult.UNKOWN,"未知返回码");
        }
    }
}
