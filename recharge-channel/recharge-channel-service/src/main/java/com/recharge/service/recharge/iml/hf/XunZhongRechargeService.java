package com.recharge.service.recharge.iml.hf;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.HuaFeiRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.Base64Util;
import com.recharge.common.utils.DateUtil;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
import com.recharge.domain.callback.XunZhongCallBack;
import com.recharge.service.recharge.AbsChannelRechargeService;

@Service
public class XunZhongRechargeService extends AbsChannelRechargeService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
		HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
		logger.info("xun zhong huafei recharge info : {} ", JSON.toJSONString(huaFeiRechargeInfoBean));
		JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
		String accountID = configJSONObject.getString("accountID");
		String authToken = configJSONObject.getString("authToken");
		String appId = configJSONObject.getString("appid");
		String version = configJSONObject.getString("version");
		String func = configJSONObject.getString("func");
		String funcURL = configJSONObject.getString("funcURL");
		String timeStamp = DateUtil.convertTimestampToDateByPattern(System.currentTimeMillis(), "yyyyMMddHHmmss"); //格式话时间戳
		String authorization = Base64Util.encoder(accountID + "|" + timeStamp);
		String sign = DigestUtils.md5Hex(accountID+authToken+timeStamp);
		//"http://api.ytx.net/201612/sid/4903baef5ce1458d98364927aafaf2d1/Unicom/Order.wx?Sign="
		///http://api.ytx.net/{version}/sid/{accountSID}/func/{funcURL}?Sign={Sign}
		String rechargeUrl = configJSONObject.getString("url")+sign;
		ProductRelation productRelation=new ProductRelation();
        if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(),"联通")){
            productRelation = queryChannelProductId("中国"+huaFeiRechargeInfoBean.getOperator()+"全国"+huaFeiRechargeInfoBean.getAmt()+"元面值" , "100088");
            if(productRelation == null){
                return new ProcessResult(ProcessResult.FAIL,"提交失败,未匹配到产品");
            }
        }else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(),"移动")){
            productRelation = queryChannelProductId("中国"+huaFeiRechargeInfoBean.getOperator()+"全国"+huaFeiRechargeInfoBean.getAmt()+"元面值" , "100088");
            if(productRelation == null){
                return new ProcessResult(ProcessResult.FAIL,"提交失败,未匹配到产品");
            }
        }else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(),"电信")){
        	productRelation = queryChannelProductId("中国"+huaFeiRechargeInfoBean.getOperator()+"全国"+huaFeiRechargeInfoBean.getAmt()+"元面值" , "100088");
            if(productRelation == null){
                return new ProcessResult(ProcessResult.FAIL,"提交失败,未匹配到产品");
            }
        }else{
        	return new ProcessResult(ProcessResult.FAIL,"提交失败,未匹配到运营商");
        }
        //获取渠道产品
        String productCode  = productRelation.getChannelProductId();
        
		Map<String,String> requestMap = new HashMap<String, String>();
        try {
            requestMap.put("accountSID" ,accountID);
            requestMap.put("authToken" ,authToken);
            requestMap.put("version" , version);
            requestMap.put("func" , func);
            requestMap.put("funcURL" , funcURL);
            requestMap.put("Authorization", authorization);
            requestMap.put("Sign" , sign);
            requestMap.put("action","productOrder");
            requestMap.put("appid",appId);
            requestMap.put("rechargeAccount",huaFeiRechargeInfoBean.getPhone());
            requestMap.put("productCode",productCode);
            requestMap.put("customParm",channelOrder.getChannelOrderId());

            logger.info("XunZhongCallBack send recharge request param:{}",JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokeJsonString(rechargeUrl, new StringEntity(JSON.toJSONString(requestMap)),authorization,"utf-8", 5000);
            logger.info("XunZhongCallBack返回值={}",responseBody);
            //{"statusCode":"0","statusMsg":"提交成功","requestId":"20200938270751652886609920902"}
            String code = JSONObject.parseObject(responseBody).getString("statusCode");
            String msg = JSONObject.parseObject(responseBody).getString("statusMsg");
            if (StringUtils.equals(code, "0")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            }else if(StringUtils.equals(code, "-100")){
            	return new ProcessResult(ProcessResult.UNKOWN,"提交可疑原因为"+msg);
            }else {
                return new ProcessResult(ProcessResult.FAIL, msg);
            }

        }catch (Exception e) {
        	logger.error(" {} XunZhong CallBack send error",rechargeOrderBean.getOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑原因为"+e.getMessage());
		}
	}

	@Override
	public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
		JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
		String accountID = configJSONObject.getString("accountID");
		String authToken = configJSONObject.getString("authToken");
		String appId = configJSONObject.getString("appid");
		String version = configJSONObject.getString("version");
		String func = configJSONObject.getString("func");
		String funcURL = configJSONObject.getString("funcURL");
		String timeStamp = DateUtil.convertTimestampToDateByPattern(System.currentTimeMillis(), "yyyyMMddHHmmss"); //格式话时间戳
		String authorization = Base64Util.encoder(accountID + "|" + timeStamp);
		String sign = DigestUtils.md5Hex(accountID+authToken+timeStamp);
		//"http://api.ytx.net/201612/sid/4903baef5ce1458d98364927aafaf2d1/Unicom/Order.wx?Sign="
		String rechargeUrl = configJSONObject.getString("url")+sign;
		Map<String,String> requestMap = new HashMap<String, String>();
		requestMap.put("accountSID" ,accountID);
        requestMap.put("authToken" ,authToken);
        requestMap.put("version" , version);
        requestMap.put("func" , func);
        requestMap.put("funcURL" , funcURL);
        requestMap.put("Authorization",authorization);
        requestMap.put("Sign" , sign);
        requestMap.put("action" , "getOrderResult");
        requestMap.put("appid" , appId);
        requestMap.put("customParm" ,channelOrder.getChannelOrderId());
        try {
            //requestMap.put("requestId" , "20200938270751652886609920902");
            String responseBody = HttpClientUtils.invokeJsonString(rechargeUrl, new StringEntity(JSON.toJSONString(requestMap)), authorization, "utf-8", 5000);
            //{"statusCode":"-704","statusMsg":"未查询到订单信息，稍后再试"}
            //{"requestId":"2016010000000205","appid":"cbc6ceb21fd34664b6e452ca2aa40b3f", "orderSts":"1","remark":"成功","mobile":"188****0804","oriAmount":"2.94","customParm":"123456"}
            logger.info("xuzhong 查询返回值={}",responseBody);
            //String code = JSONObject.parseObject(responseBody).getString("statusCode");
            //String msg = JSONObject.parseObject(responseBody).getString("statusMsg");
            XunZhongCallBack result = JSON.parseObject(responseBody, XunZhongCallBack.class);

        	if(!StringUtils.isBlank(result.getOrderSts()) && result.getOrderSts().equals("1")){
        		return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        	}else if(!StringUtils.isBlank(result.getOrderSts()) && result.getOrderSts().equals("-1")){
        		return new ProcessResult(ProcessResult.FAIL, "充值失败");
        	}else if(!StringUtils.isBlank(result.getOrderSts()) && result.getOrderSts().equals("-100")){
            	return new ProcessResult(ProcessResult.UNKOWN,"查询可疑");
            }else{
        		return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        	}
            
        }catch (Exception e) {
        	logger.error("{} xunzhong send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑原因为"+e.getMessage());
		}
	}

	@Override
	public ProcessResult parseResponse(ResponseOrder responseOrder) {
		if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if(StringUtils.equals("-1", responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }else{
        	return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        }
	}

}
