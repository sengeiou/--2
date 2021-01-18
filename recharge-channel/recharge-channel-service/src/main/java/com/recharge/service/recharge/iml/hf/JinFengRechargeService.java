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
import com.recharge.utils.HmacUtils;

import jodd.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020/12/4 17:46
 */
@Service
public class JinFengRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String P1_agentcode = configJSONObject.getString("agentcode");
        String notifyUrl = configJSONObject.getString("notifyUrl");
        String key = configJSONObject.getString("appKey");
        String P0_biztype = "mobiletopup";
        String P2_mobile = huaFeiRechargeInfoBean.getPhone();
        String P3_parvalue = huaFeiRechargeInfoBean.getAmt().toString();
        //1、移动                 SHKC
        //2、联通                 SHKC_CU
        //3、电信                 SHKC_CT
        String P4_productcode="";
        if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(),"移动")) {
            P4_productcode = "SHKC";
        } else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "电信")) {
            P4_productcode = "SHKC_CT";
        } else {
            P4_productcode = "SHKC_CU";
        }
        String P5_requestid = channelOrder.getChannelOrderId();
        String P6_callbackurl = notifyUrl;
        String P7_extendinfo = "FaSongChongZhi";
        String mac = P0_biztype + P1_agentcode + P2_mobile + P3_parvalue + P4_productcode + P5_requestid + P6_callbackurl + P7_extendinfo;
        String hmac = HmacUtils.hmacSign(mac, key);
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("P0_biztype", P0_biztype);
        requestMap.put("P1_agentcode", P1_agentcode);
        requestMap.put("P2_mobile", P2_mobile);
        requestMap.put("P3_parvalue", P3_parvalue);
        requestMap.put("P4_productcode", P4_productcode);
        requestMap.put("P5_requestid", P5_requestid);
        requestMap.put("P6_callbackurl", P6_callbackurl);
        requestMap.put("P7_extendinfo", P7_extendinfo);
        requestMap.put("hmac", hmac);
        try {
            logger.info("JinFeng recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 10000);
            logger.info("JinFeng recharge response params:{}", JSONObject.toJSONString(responseBody));
            if (StringUtils.equals(responseBody, "000000")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals(responseBody, "100032")) {
                return new ProcessResult(ProcessResult.UNKOWN, "需要运营去jingfeng核实");
            } else {
            	if(StringUtil.isNotEmpty(responseBody) && responseBody.length()!=6){
            		 return new ProcessResult(ProcessResult.UNKOWN, "提交可疑返回码为="+responseBody);
            	}
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (Exception e) {
            logger.info("JinFeng recharge 异常:{}", e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑原因为=" + e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String P1_agentcode = configJSONObject.getString("agentcode");
        String key = configJSONObject.getString("appKey");
        String url = configJSONObject.getString("queryUrl");
        String P5_requestid = channelOrder.getChannelOrderId();
        String mac = P1_agentcode + P5_requestid;
        String s = HmacUtils.hmacSign(mac, key);
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("P1_agentcode", P1_agentcode);
        requestMap.put("P5_requestid", P5_requestid);
        requestMap.put("hmac", s);
        try {
            logger.info("JinFeng query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("JinFeng query response params:{}",responseBody);
            String[] split = responseBody.split("\\|");
            if (StringUtils.equals(split[5], "2")) {
                channelOrder.setOutChannelOrderId(split[7]);
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals(split[5], "1")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else if (StringUtils.equals(split[5], "0")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else if (StringUtils.equals(split[5], "6")) {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "需要找供货商核实");
            }
        } catch (Exception e) {
        	logger.info("JinFeng query 异常:{}", e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "订单未知");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        } else if (StringUtils.equals("0", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        } else {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("queryBalanceUrl");
        String P1_agentcode = configJSONObject.getString("agentcode");
        String key = configJSONObject.getString("appKey");
        String mac = P1_agentcode;
        String s = HmacUtils.hmacSign(mac, key);
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("P1_agentcode", P1_agentcode);
        requestMap.put("hmac", s);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            String[] split = responseBody.split("\\|");
            return new BigDecimal(split[2]);
        } catch (Exception e) {
        	logger.info("JinFeng balanceQuery 异常:{}", e.getMessage());
            return null;
        }
    }


    @Test
    public void test() {
        JinFengRechargeService jinFengRechargeService = new JinFengRechargeService();
        Channel channel = new Channel();
        ChannelOrder channelOrder = new ChannelOrder();
        channel.setConfigInfo("{\n" +
                "\tagentcode: \"NJYJ202012041135\",\n" +
                "\trechargeUrl: \"http://czapi2.1jinb.net/youpintongAgentByInterfaceServlet\",\n" +
                "\tqueryBalanceUrl: \"http://czapi2.1jinb.net/youpintongAgentBalanceInterfaceServlet\",\n" +
                "\tnotifyUrl: \"http://139.129.85.83:8082/jinFeng/callBack\",\n" +
                "\tappKey: \"e10adc3949ba59abbe56e057f20f883e\",\n" +
                "\tqueryUrl: \"http://czapi2.1jinb.net/AgentNewQueryInterfaceServlet\"\n" +
                "}");
//        channelOrder.setChannelOrderId("cs202012041903");
        channelOrder.setChannelOrderId("cs202012071121");
//        ProcessResult recharge = jinFengRechargeService.recharge(channel, channelOrder, new RechargeOrderBean());
        ProcessResult query = jinFengRechargeService.query(channel, channelOrder);
//        BigDecimal bigDecimal = jinFengRechargeService.balanceQuery(channel);
    }
}
