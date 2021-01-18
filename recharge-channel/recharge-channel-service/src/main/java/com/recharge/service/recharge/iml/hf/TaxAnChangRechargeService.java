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
import com.recharge.domain.ProductRelation;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author user
 * @date 2020/3/13 12:53
 */
@Service
public class TaxAnChangRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 业务订购接口
     *
     * @param channel
     * @param channelOrder
     * @param rechargeOrderBean
     * @return
     */
    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        ProductRelation productRelation = new ProductRelation();
        String productId="";

        if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "联通")) {
            productRelation = queryChannelProductId("联通全国直充" , "100061");
            productId=productRelation.getChannelProductId()+huaFeiRechargeInfoBean.getAmt();
            if (productRelation == null) {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "移动")) {
            productRelation = queryChannelProductId("移动全国直充", "100061");
            productId=productRelation.getChannelProductId()+huaFeiRechargeInfoBean.getAmt();
            if (productRelation == null) {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } else {
            productRelation = queryChannelProductId("电信全国直充", "100061");
            productId=productRelation.getChannelProductId()+huaFeiRechargeInfoBean.getAmt();
            if (productRelation == null) {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        }
        String url = configJSONObject.getString("url");
        String customerKey = configJSONObject.getString("customerKey");
        String memberKey = configJSONObject.getString("memberKey");
        String userId = configJSONObject.getString("userid");
        String memberId = configJSONObject.getString("memberid");
        String reqTime = String.valueOf(System.currentTimeMillis() / 1000);
        String prodType = "HUAFEI";
        String orderNo = channelOrder.getChannelOrderId();
        String acctNo = huaFeiRechargeInfoBean.getPhone();
        String reserve1 = "";
        String reserve2 = "";
        String sign = DigestUtils.md5Hex(userId + memberId + reqTime
                + prodType + orderNo + customerKey + memberKey).toUpperCase();

        Map<String, Object> requestMap = new HashMap<>();
        Map<String, String> orderInfo = new HashMap<>();
        orderInfo.put("orderNo", orderNo);
        orderInfo.put("acctNo", acctNo);
        orderInfo.put("reserve1", reserve1);
        orderInfo.put("reserve2", reserve2);
        orderInfo.put("prodCode", productId);

        requestMap.put("orderInfo", orderInfo);
        requestMap.put("userId", userId);
        requestMap.put("memberId", memberId);
        requestMap.put("reqTime", reqTime);
        requestMap.put("prodType", prodType);
        requestMap.put("sign", sign);
        try {
            logger.info("send recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostString(url, new StringEntity(JSON.toJSONString(requestMap)), "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);
            JSONObject json = (JSONObject) JSONObject.parse(responseBody);
            String code = json.getString("code");

            if (StringUtils.equals(code, "0000")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }

    }

    /**
     * 订单查询接口
     *
     * @param channel
     * @param channelOrder
     * @return
     */
    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String queryUrl = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userid");
        String memberId = configJSONObject.getString("memberid");
        String reqTime = String.valueOf(System.currentTimeMillis() / 1000);
        String prodType = "HUAFEI";
        String orderNo = channelOrder.getChannelOrderId();
        String customerKey = configJSONObject.getString("customerKey");
        String memberKey = configJSONObject.getString("memberKey");
        String sign = DigestUtils.md5Hex(userId + memberId + reqTime
                + prodType + orderNo + customerKey + memberKey).toUpperCase();
        Map<String, Object> requestMap = new HashMap<>();
        Map<String, String> orderInfo = new HashMap<>();

        requestMap.put("userId", userId);
        requestMap.put("memberId", memberId);
        requestMap.put("reqTime", reqTime);
        requestMap.put("prodType", prodType);
        orderInfo.put("orderNo", orderNo);
        requestMap.put("orderInfo", orderInfo);
        requestMap.put("sign", sign);


        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostString(queryUrl, new StringEntity(JSON.toJSONString(requestMap)), "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            JSONObject json = (JSONObject) JSONObject.parse(responseBody);

            String status = json.getString("status");

            if (StringUtils.equals(status, "0")) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals(status, "4")) {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            } else {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            }

        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(), "0")) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "4")) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        }
    }

    /**
     * 账户余额查询入口
     *
     * @param channel
     * @return
     */
    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String queryUrl = configJSONObject.getString("queryBalanceUrl");
        String userId = configJSONObject.getString("userid");
        String reqTime = String.valueOf(System.currentTimeMillis() / 1000);
        String customerKey = configJSONObject.getString("customerKey");
        String sign = DigestUtils.md5Hex(userId + reqTime + customerKey).toUpperCase();
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userId", userId);
        requestMap.put("reqTime", reqTime);
        requestMap.put("sign", sign);

        try {
            logger.info("send queryBalance request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostString(queryUrl, new StringEntity(JSON.toJSONString(requestMap)), "utf-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);

            JSONObject json = (JSONObject) JSONObject.parse(responseBody);
            String balance = json.getString("balance");
            String credit = json.getString("credit");
            return new BigDecimal(balance);
        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }
    }


}
