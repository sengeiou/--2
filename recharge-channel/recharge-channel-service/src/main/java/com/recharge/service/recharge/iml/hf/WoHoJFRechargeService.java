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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author user
 * @create 2020/12/4 17:51
 */
@Service
public class WoHoJFRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        ProductRelation productRelation = new ProductRelation();
        if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "联通")) {
            productRelation = queryChannelProductId("全国联通话费"+huaFeiRechargeInfoBean.getAmt() + "元" , "100111");
            if (productRelation == null) {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "移动")) {
            productRelation = queryChannelProductId("全国移动话费"+huaFeiRechargeInfoBean.getAmt()+"元", "100111");
            if (productRelation == null) {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } else {
            productRelation = queryChannelProductId("全国电信话费"+huaFeiRechargeInfoBean.getAmt()+"元", "100111");
            if (productRelation == null) {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        }
        String url = configJSONObject.getString("url");
        String app_id = configJSONObject.getString("app_id");
        String password = configJSONObject.getString("password");
        String amount = String.valueOf(huaFeiRechargeInfoBean.getAmt());
        String order_id = channelOrder.getChannelOrderId();
        String account = huaFeiRechargeInfoBean.getPhone();
        String product_code = productRelation.getChannelProductId();
        String other_param = "";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = DigestUtils.md5Hex("password=" + password + "&order_id=" + order_id + "&account=" + account + "&product_code=" + product_code + "&amount=" + amount + "&other_param=" + other_param + "&timestamp=" + timestamp).toUpperCase();
        Map<String, String> requestMap = new HashMap<>();
        System.out.println(sign);
        requestMap.put("sign", sign);
        requestMap.put("timestamp", timestamp);
        requestMap.put("amount", amount);
        requestMap.put("product_code", product_code);
        requestMap.put("other_param", other_param);
        requestMap.put("account", account);
        requestMap.put("app_id", app_id);
        requestMap.put("order_id", order_id);
        try {
            logger.info("woHujf send recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostString(url, new StringEntity(JSON.toJSONString(requestMap)), "UTF-8", 5000);
            logger.info("woHujf send recharge response :{}", responseBody);
            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            String code = jsonObject.getString("result_code");
            String message = jsonObject.getString("result_msg");
            if (StringUtils.equals(code, "00000")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, message);
            }
        } catch (Exception e) {
            logger.error("{}woHujf send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryUrl = configJSONObject.getString("queryUrl");
        String app_id = configJSONObject.getString("app_id");
        String order_id = channelOrder.getChannelOrderId();
        String account = channelOrder.getRechargeNumber();
        String passowrd = configJSONObject.getString("password");
        String sign = DigestUtils.md5Hex("password=" + passowrd + "&order_id=" + order_id + "&account=" + account).toUpperCase();
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("app_id", app_id);
        requestMap.put("sign", sign);
        requestMap.put("order_id", order_id);
        requestMap.put("account", account);
        try {
            logger.info("woHujf send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostString(queryUrl, new StringEntity(JSON.toJSONString(requestMap)), "UTF-8", 5000);
            logger.info("woHujf send query response :{}", responseBody);
            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            String code = jsonObject.getString("result_code");
            String status = jsonObject.getString("order_status");
            if (StringUtils.equals(code, "00000")) {
                if (StringUtils.equals(status, "finish")) {
                    String operator_no = jsonObject.getString("operator_no");
                    if (!StringUtils.isEmpty(operator_no)) {
                        channelOrder.setOutChannelOrderId(operator_no);
                    }
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                }else if (StringUtils.equals(status, "processing")||StringUtils.equals(status, "wait")||StringUtils.equals(status, "reserve")){
                    return new ProcessResult(ProcessResult.PROCESSING,"充值中");
                }else {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                }
            } else {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            }
        } catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(), "finish")) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "fail")) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }else if (StringUtils.equals(responseOrder.getResponseCode(), "processing")) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        }else if (StringUtils.equals(responseOrder.getResponseCode(), "wait")) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        }
        return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String balanceQueryUrl = configJSONObject.getString("balanceQueryUrl");
        String app_id = configJSONObject.getString("app_id");
        String password = configJSONObject.getString("password");
        String sign = DigestUtils.md5Hex("password=" + password).toUpperCase();

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("app_id", app_id);
        requestMap.put("sign", sign);
        System.out.println(JSON.toJSONString(requestMap));
        try {
            logger.info("send queryBalance request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostString(balanceQueryUrl, new StringEntity(JSON.toJSONString(requestMap)), "UTF-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);

            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            String balance = jsonObject.getString("balance");
            return new BigDecimal(balance);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

}
