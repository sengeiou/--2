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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class DingXinDJRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, String> errorMap = new HashMap<>();

    public DingXinDJRechargeServiceImpl() {
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
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String pwd = configJSONObject.getString("pwd");
        String key = configJSONObject.getString("key");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userid", userId);
        requestMap.put("pwd", pwd);
        requestMap.put("orderid", channelOrder.getChannelOrderId());
        requestMap.put("face", huaFeiRechargeInfoBean.getAmt().toString());
        requestMap.put("account", huaFeiRechargeInfoBean.getPhone());
        requestMap.put("amount", "1");
        requestMap.put("userkey", DigestUtils.md5Hex("userid" + requestMap.get("userid")
                + "pwd" + requestMap.get("pwd") + "orderid" + requestMap.get("orderid") + "face" + requestMap.get("face")
                + "account" + requestMap.get("account") + "amount1" + key).toUpperCase());
        try {
            logger.info("send recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element state = root.element("state");
            Element error = root.element("error");

            if (StringUtils.equals(error.getStringValue(), "0")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals(error.getStringValue(), "9999")) {
                return new ProcessResult(ProcessResult.UNKOWN, "未知错误，需要跟供货商核实");
            } else {
                return new ProcessResult(ProcessResult.FAIL, errorMap.get(error.getStringValue()));
            }

        } catch (Exception e) {
            logger.error("{}send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String queryUrl = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userId");
        String pwd = configJSONObject.getString("pwd");
        String key = configJSONObject.getString("key");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userid", userId);
        requestMap.put("pwd", pwd);
        requestMap.put("orderid", channelOrder.getChannelOrderId());
        requestMap.put("userkey", DigestUtils.md5Hex("userid" + requestMap.get("userid")
                + "pwd" + requestMap.get("pwd") + "orderid" + requestMap.get("orderid")
                + key).toUpperCase());
        try {
            logger.info("send recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element state = root.element("state");
            Element error = root.element("error");

            if (StringUtils.equals(error.getStringValue(), "0")) {
                if (StringUtils.equals(state.getStringValue(), "1")) {
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (StringUtils.equals(error.getStringValue(), "9999")) {
                    return new ProcessResult(ProcessResult.UNKOWN, "未知错误，需要跟供货商核实");
                } else if (StringUtils.equals(state.getStringValue(), "2")) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else {
                    return new ProcessResult(ProcessResult.PROCESSING, "处理中");
                }
            } else {
                return new ProcessResult(ProcessResult.FAIL, errorMap.get(error.getStringValue()));
            }

        } catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      余额查询地址：http:// ip:端口/money_jkuser.do
        String queryUrl = configJSONObject.getString("queryBalanceUrl");
        String userId = configJSONObject.getString("userId");
        String pwd = configJSONObject.getString("pwd");
        String key = configJSONObject.getString("key");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userid", userId);
        requestMap.put("pwd", pwd);
        requestMap.put("userkey", DigestUtils.md5Hex("userid" + requestMap.get("userid")
                + "pwd" + requestMap.get("pwd")
                + key).toUpperCase());
        try {
            logger.info("send queryBalance request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element error = root.element("error");

            if (StringUtils.equals(error.getStringValue(), "0")) {
                Element balance = root.element("lastMoney");
                return new BigDecimal(balance.getStringValue());
            } else {
                return BigDecimal.ZERO;
            }

        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }
    }
}
