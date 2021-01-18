package com.recharge.service.recharge.iml.hf;

import java.math.BigDecimal;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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

@Service
public class NuoXiangRechargeService extends AbsChannelRechargeService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
		HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
		JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
		
		String orderId = channelOrder.getChannelOrderId();
		String account = huaFeiRechargeInfoBean.getPhone();
		String amount = String.valueOf(huaFeiRechargeInfoBean.getAmt());
		String shopId =configJSONObject.getString("ShopId");
		String userId =configJSONObject.getString("UserId");
		String num =configJSONObject.getString("Num");
		String productType=configJSONObject.getString("ProductType");
		String timeStamp =String.valueOf(System.currentTimeMillis());
		String key=configJSONObject.getString("key");//"48cb6c1319237b25fe13d8c737288d1d";
		String sign =DigestUtils.md5Hex(shopId+userId+productType+orderId+account+amount+num+timeStamp+key);
		String rechargeUrl = configJSONObject.getString("submitUrl");
		String url =rechargeUrl+"?OrderId"
				+ "="+orderId+"&Account="+account+"&Amount="+amount+"&ShopId="+shopId
				+ "&UserId="+userId+"&Sign="+sign+"&Num="+num+"&ProductType="+productType
				+"&Timestamp="+timeStamp;
		try {
            logger.info("nuo xiang send recharge request :{}",url);
            String responseBody = HttpClientUtils.invokeGetHttp(url, "utf-8", 10000);
            //{"Timestamp":"1600784803","Code":10012,"Message":"订单提交成功","Price":29.16,"SysOrderId":"100171600784803050","ProductName":"NULL"}
            String code = JSONObject.parseObject(responseBody).getString("Code");
            String msg = JSONObject.parseObject(responseBody).getString("Message");
            if(code.equals("10012")){
            	return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            }else{
            	return new ProcessResult(ProcessResult.FAIL, msg);
            }
        }catch (Exception e) {
        	logger.error(" {} nuoxiang CallBack send error",rechargeOrderBean.getOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑原因为"+e.getMessage());
		}
	}

	@Override
	public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
		JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
		String orderId = channelOrder.getChannelOrderId();
		String shopId =configJSONObject.getString("ShopId");
		String userId =configJSONObject.getString("UserId");
		String timeStamp =String.valueOf(System.currentTimeMillis());
		String key=configJSONObject.getString("key");
		String rechargeUrl= configJSONObject.getString("queryUrl");
		String nuoXiangOrderId = userId+orderId;
		String sign =DigestUtils.md5Hex(shopId+userId+nuoXiangOrderId+timeStamp+key);
		String url =rechargeUrl+"?ShopId="+shopId
				+ "&UserId="+userId+"&Sign="+sign+"&Timestamp="+timeStamp+"&OrderId="+nuoXiangOrderId;
		try {
            logger.info("nuoxiang send query request requestUrl:{}",url);
            String responseBody = HttpClientUtils.invokeGetHttp(url, "utf-8", 5000);
            //打印返回值={"Timestamp":1600786027,"Code":"4","OrderId":"100171600784803050","Account":"17798529951","ProductId":1,"ProductName":"话费","Price":"","Message":"充值成功","VoucherType":"0","VoucherContent":"1000000083420092222320454410"}
            String code = JSONObject.parseObject(responseBody).getString("Code");
            String msg = JSONObject.parseObject(responseBody).getString("Message");
            if(code.equals("4")){
            	return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            }else if(code.equals("5")){
            	return new ProcessResult(ProcessResult.FAIL, msg);
            }else if(code.equals("0")){
            	return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            }else{
            	return new ProcessResult(ProcessResult.UNKOWN, "订单不存在");
            }
            
        }catch (Exception e) {
        	logger.error("{} nuoxiang send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑原因为"+e.getMessage());
		}
	}
	
	
	@Override
	public ProcessResult parseResponse(ResponseOrder responseOrder) {
		if (StringUtils.equals("4", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if(StringUtils.equals("5", responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }else if(StringUtils.equals("0", responseOrder.getResponseCode())){
        	return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        }else{
        	return new ProcessResult(ProcessResult.UNKOWN, "未知状态="+responseOrder.getResponseCode());
        }
	}

	@Override
	public BigDecimal balanceQuery(Channel channel) {
		//sss
		JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
		String shopId =configJSONObject.getString("ShopId");
		String userId =configJSONObject.getString("UserId");
		String timeStamp =String.valueOf(System.currentTimeMillis());
		String key=configJSONObject.getString("key");
		String sign =DigestUtils.md5Hex(shopId+userId+timeStamp+key);
		String queryBalance=configJSONObject.getString("queryBalance");
		//sss
		String url =queryBalance+"?ShopId="+shopId
				+ "&UserId="+userId+"&Sign="+sign+"&Timestamp="+timeStamp;
		 try {
			String responseBody = HttpClientUtils.invokeGetHttp(url, "utf-8", 5000);
			String code = JSONObject.parseObject(responseBody).getString("Code");
            //String data = JSONObject.parseObject(responseBody).getString("Message");
            //String balance = JSONObject.parseObject(responseBody).getString("Balance");
            if(code.equals("10000")){
            	 String balance = JSONObject.parseObject(responseBody).getString("Balance");
            	 return new BigDecimal(balance);
            } 
		} catch (Exception e) {
			logger.error("{} nuoxiang query balance send error", e);
			 return BigDecimal.ZERO;
		}
		 return BigDecimal.ZERO;
	}

}
