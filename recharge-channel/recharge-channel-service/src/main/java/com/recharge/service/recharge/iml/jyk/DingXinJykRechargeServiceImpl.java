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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class DingXinJykRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, String> errorMap = new HashMap<>();

    public DingXinJykRechargeServiceImpl() {
        errorMap.put("0", "无错误");
        errorMap.put("1003", "用户ID或接口密码错误");
        errorMap.put("1004", "用户IP错误");
        errorMap.put("1005", "用户接口已关闭");
        errorMap.put("1006", "加密结果错误");
        errorMap.put("1007", "订单号不存在");
        errorMap.put("1011", "号码归属地未知");
        errorMap.put("1013", "手机对应的商品有误或者没有上架");
        errorMap.put("1014", "无法找到手机归属地");
        errorMap.put("1015", "余额不足");
        errorMap.put("1016", "QQ号格式错误");
        errorMap.put("1017", "产品未分配用户，联系商务");
        errorMap.put("1018", "订单生成失败");
        errorMap.put("1019", "充值号码与产品不匹配");
        errorMap.put("1020", "号码运营商未知");
        errorMap.put("9998", "参数有误");
        errorMap.put("9999", "系统错误");

    }

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String uaccount = configJSONObject.getString("uaccount");
        String apiKey = configJSONObject.getString("apiKey");

        ProductRelation productRelation = queryChannelProductId(rechargeOrderBean.getProductName(), "100033");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("orderid", channelOrder.getChannelOrderId());
        requestMap.put("productid", productRelation.getChannelProductId());
        requestMap.put("uaccount", uaccount);
        requestMap.put("account", jykRechargeInfoBean.getAccount());
        requestMap.put("timestamp", System.currentTimeMillis() + "");
        requestMap.put("sign", DigestUtils.md5Hex(
                "orderid" + requestMap.get("orderid")
                        + "productid" + requestMap.get("productid")
                        + "uaccount" + requestMap.get("uaccount")
                        + "account" + requestMap.get("account")
                        + "timestamp" + requestMap.get("timestamp")
                        + apiKey));
        try {
            logger.info("request param:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url + "/trade/gateway", requestMap, "utf-8");
            logger.info("response body :{}", responseBody);

            JSONObject jsonObject = JSON.parseObject(responseBody);
            String code = jsonObject.getString("code");
            String message = jsonObject.getString("message");
            if (StringUtils.equals(code, "10010")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals(code, "9999")
                    || StringUtils.equals(code, "1009")
                    || StringUtils.equals(code, "1006")) {
                return new ProcessResult(ProcessResult.UNKOWN, message);
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (Exception e) {
            logger.error("juhe request error", e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String uaccount = configJSONObject.getString("uaccount");
        String apiKey = configJSONObject.getString("apiKey");


        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("orderid", channelOrder.getChannelOrderId());
        requestMap.put("uaccount", uaccount);
        requestMap.put("sign", DigestUtils.md5Hex(
                "orderid" + requestMap.get("orderid")
                        + "uaccount" + requestMap.get("uaccount") + apiKey));
        try {
            logger.info("request param:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url + "/trade/queryOrder", requestMap, "utf-8");
            logger.info("response body :{}", responseBody);

            JSONObject jsonObject = JSON.parseObject(responseBody);
            String code = jsonObject.getString("code");
            String message = jsonObject.getString("message");
            String state = jsonObject.getString("state");
            if (StringUtils.equals(state, "1")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals(state, "2")) {
                return new ProcessResult(ProcessResult.FAIL, message);
            } else if (StringUtils.equals(state, "5")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
            }
        } catch (Exception e) {
            logger.error("dingxin request error", e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(), "1")) {
            return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "2")) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "5")) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      余额查询地址：http://ip:port/trade/queryBalance
        String url = configJSONObject.getString("queryBalanceUrl");
        String uaccount = configJSONObject.getString("uaccount");
        String apiKey = configJSONObject.getString("apiKey");


        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("uaccount", uaccount);
        requestMap.put("timestamp", System.currentTimeMillis() + "");
        requestMap.put("sign", DigestUtils.md5Hex(
                "uaccount" + requestMap.get("uaccount")
                        + "timestamp" + requestMap.get("timestamp")
                        + apiKey));
        try {
            logger.info("request param:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8");
            logger.info("response body :{}", responseBody);

            JSONObject jsonObject = JSON.parseObject(responseBody);
            String code = jsonObject.getString("code");
            if (StringUtils.equals(code, "10010")) {
                String balance = jsonObject.getString("balance");
                return new BigDecimal(balance);
            } else {
                return new BigDecimal(0);
            }
        } catch (Exception e) {
            logger.error("dingxin request error", e);
            return new BigDecimal(0);
        }
    }
}
