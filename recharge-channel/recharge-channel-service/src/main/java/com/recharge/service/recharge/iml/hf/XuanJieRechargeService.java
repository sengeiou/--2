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
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class XuanJieRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      请求地址：http://120.55.72.146:10051/api/submitOrder.do
        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");
        String notifyUrl = configJSONObject.getString("callback");


//      运营商编码   1移动 2联通 3电信
        String nSortType = "";
        if (StringUtils.equals("移动", huaFeiRechargeInfoBean.getOperator())) {
            nSortType = "1";
        }
        if (StringUtils.equals("联通", huaFeiRechargeInfoBean.getOperator())) {
            nSortType = "2";
        }
        if (StringUtils.equals("电信", huaFeiRechargeInfoBean.getOperator())) {
            nSortType = "3";
        }

//      产品类别，固定值：1
        String nProductClass = "1";
//      产品类型，1话费快充  2话费慢充
        String nProductType = "1";

        String szTimeStamp = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");

//      签名
        String info = "szAgentId=" + userId// 用户ID
                + "&szOrderId=" + channelOrder.getChannelOrderId()  //订单号
                + "&szPhoneNum=" + huaFeiRechargeInfoBean.getPhone() //手机号
                + "&nMoney=" + huaFeiRechargeInfoBean.getAmt().toString()// 面值
                + "&nSortType=" + nSortType// 运营商编码，详情见接口文档
                + "&nProductClass=" + nProductClass
                + "&nProductType=" + nProductType
                + "&szTimeStamp=" + szTimeStamp
                + "&szKey=" + key;

        String szVerifyString = DigestUtils.md5Hex(info).toLowerCase();


//        请求参数
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("szAgentId", userId);
        requestMap.put("szOrderId", channelOrder.getChannelOrderId());
        requestMap.put("szPhoneNum", huaFeiRechargeInfoBean.getPhone());
        requestMap.put("nMoney", huaFeiRechargeInfoBean.getAmt().toString());
        requestMap.put("nSortType", nSortType);
        requestMap.put("nProductClass", nProductClass);
        requestMap.put("nProductType", nProductType);
        requestMap.put("szTimeStamp", szTimeStamp);
        requestMap.put("szVerifyString", szVerifyString);
        requestMap.put("szNotifyUrl", notifyUrl);
        requestMap.put("szFormat", "JSON");


        try {
            logger.info("send recharge request params:{}", requestMap);
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            String code = JSONObject.parseObject(responseBody).getString("nRtn");

            if (StringUtils.equals("0", code) || StringUtils.equals("3003", code) ||
                    StringUtils.equals("3004", code) || StringUtils.equals("2051", code) ||
                    StringUtils.equals("2050", code) || StringUtils.equals("999", code)) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }

        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      请求地址：话费订单查询地址1：http://120.55.72.146:10521/IXJCWIQueryOrder/XJQueryOrderHF.aspx
//              话费订单查询地址2：http://120.55.75.52:10051/api/query/queryOrder.do
        String url = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

        //签名串
        String info = "szAgentId=" + userId// 用户ID
                + "&szOrderId=" + channelOrder.getChannelOrderId()
                + "&szKey=" + key;// 密钥

        // 签名小写
        String szVerifyString = DigestUtils.md5Hex(info).toLowerCase();

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("szAgentId", userId);
        requestMap.put("szOrderId", channelOrder.getChannelOrderId());
        requestMap.put("szVerifyString", szVerifyString);
        requestMap.put("szFormat", "JSON");

        try {
            logger.info("send query request params:{}", requestMap);
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            String code = JSONObject.parseObject(responseBody).getString("nRtn");
            // 返回状态。
            // 5010初始化(处理中)
            // 5011处理中（处理中）
            // 5012充值成功（成功）成功
            // 5013充值失败（失败）失败退款
            // 5019对账状态（处理中）
            // 5005无此订单（无订单）可补充或退款
            // 其余状态均不可处理为失败，需再次发起查询
            if (StringUtils.equals("5012", code)) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals("5013", code) || StringUtils.equals("5005", code)) {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            } else {
                return new ProcessResult(ProcessResult.PROCESSING, "处理中");
            }

        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "处理中");
        }

    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
//       2成功    3失败
        if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("3", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      请求地址：http://120.55.72.146:10521/IXJCWIQueryBalance/XJQueryBalance.aspx
        String url = configJSONObject.getString("queryBalanceUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

        //签名串
        String info = "szAgentId=" + userId// 用户ID
                + "&szKey=" + key;// 密钥

        // 签名小写
        String szVerifyString = DigestUtils.md5Hex(info).toLowerCase();

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("szAgentId", userId);
        requestMap.put("szVerifyString", szVerifyString);
        requestMap.put("szFormat", "JSON");

        try {
            logger.info("send queryBalance request params:{}", requestMap);
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);

            String code = JSONObject.parseObject(responseBody).getString("nRtn");
            if (StringUtils.equals("0", code)) {
                String balance = JSONObject.parseObject(responseBody).getString("fBalance");
                return new BigDecimal(balance);
            } else {
                return BigDecimal.ZERO;
            }

        } catch (Exception e) {
            logger.error("send error", e);
            return BigDecimal.ZERO;
        }
    }

    @Test
    void test() {
        Channel channel = new Channel();
        channel.setConfigInfo("{\"url\":\"http://120.55.72.146:10051/api/submitOrder.do\",\"userId\":\"pushang\",\"key\":\"cfa3a65ffe6d4c79a7f9dc81ad31da1b\",\"queryUrl\":\"http://120.55.72.146:10521/IXJCWIQueryOrder/XJQueryOrderHF.aspx\",\"queryBalanceUrl\":\"http://120.55.72.146:10521/IXJCWIQueryBalance/XJQueryBalance.aspx\",\"callback\":\"http://139.129.85.83:8082/xuanJie/callBack\"}");
        ChannelOrder channelOrder = new ChannelOrder();
        channelOrder.setChannelOrderId("cs202101051415");
//        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
        ProcessResult query = query(channel, channelOrder);
        System.out.println("z");
    }

}
