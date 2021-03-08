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
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2021/3/2 9:24
 */
@Service
public class WoHuNoHaveVoucherJykRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String channelId = "100145";

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String app_id = configJSONObject.getString("app_id");
        String password = configJSONObject.getString("password");
        String order_id = channelOrder.getChannelOrderId();
        String account=jykRechargeInfoBean.getAccount();
        ProductRelation productRelation = queryChannelProductId("中石化加油卡直充" + jykRechargeInfoBean.getAmt() + "元", channelId);
        if (productRelation == null) {
            return new ProcessResult(ProcessResult.FAIL, "供货商商品编号查询失败");
        }
        String product_code=productRelation.getChannelProductId();
        String amount = "1";
        String name = "固定值";
        String phone = "15959100000";
        String idcard = "100000000000000";
        String timestamp = Long.toString(System.currentTimeMillis() / 1000);
        Map<String, String> map = new LinkedHashMap<>();
        Map<String, String> other_param = new LinkedHashMap<>();
        other_param.put("name", name);
        other_param.put("phone", phone);
        other_param.put("idcard", idcard);
        StringBuffer sb = new StringBuffer();
        sb.append("password=").append(password)
                .append("&order_id=").append(order_id)
                .append("&account=").append(account)
                .append("&product_code=").append(product_code)
                .append("&amount=").append(amount)
                .append("&other_param=").append(JSONObject.toJSONString(other_param))
                .append("&timestamp=").append(timestamp);
        String sign = DigestUtils.md5Hex(sb.toString()).toUpperCase();
        map.put("app_id", app_id);
        map.put("sign", sign);
        map.put("order_id", order_id);
        map.put("account", account);
        map.put("product_code", product_code);
        map.put("amount", amount);
        map.put("other_param", JSONObject.toJSONString(other_param));
        map.put("timestamp", timestamp);
        try {
            logger.info("沃虎加油卡(无透传)下单接口请求的参数:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map), "UTF-8"), "", "utf-8", 5000);
            logger.info("沃虎加油卡(无透传)下单接口响应的参数:{}", responseBody);
            String result_code = JSONObject.parseObject(responseBody).getString("result_code");
            if (StringUtils.equals("00000", result_code)) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                String result_msg = JSONObject.parseObject(responseBody).getString("result_msg");
                return new ProcessResult(ProcessResult.FAIL, "提交失败：" + result_msg);
            }
        } catch (Exception e) {
            logger.error("{}沃虎加油卡(无透传)下单接口请求报错的参数{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑：" + e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("queryUrl");
        String app_id = configJSONObject.getString("app_id");
        String password = configJSONObject.getString("password");
        String account = channelOrder.getRechargeNumber();
        String order_id = channelOrder.getChannelOrderId();
        StringBuffer sb = new StringBuffer();
        sb.append("password=").append(password)
                .append("&order_id=").append(order_id)
                .append("&account=").append(account);
        String sign = DigestUtils.md5Hex(sb.toString()).toUpperCase();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("app_id", app_id);
        map.put("sign", sign);
        map.put("order_id", order_id);
        map.put("account", account);
        try {
            logger.info("沃虎加油卡(无透传)订单查询接口请求的参数:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map), "UTF-8"), "", "utf-8", 5000);
            logger.info("沃虎加油卡(无透传)订单查询接口响应的参数:{}", responseBody);
            String result_code = JSONObject.parseObject(responseBody).getString("result_code");
            if (StringUtils.equals("00000", result_code)) {
                String order_status = JSONObject.parseObject(responseBody).getString("order_status");
                if(StringUtils.equals("finish",order_status)){
                    String operator_no = JSONObject.parseObject(responseBody).getString("operator_no");
                    if(StringUtils.isEmpty(operator_no)){
                        channelOrder.setOutChannelOrderId(operator_no);
                    }
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                }else if(StringUtils.equals("fail",order_status)){
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                }else {
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                }
            }else {
                String result_msg = JSONObject.parseObject(responseBody).getString("result_msg");
                return new ProcessResult(ProcessResult.UNKOWN, "查询失败："+result_msg);
            }
        } catch (Exception e) {
            logger.error("{}沃虎加油卡(无透传)查询订单接口请求报错的参数{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "查询失败："+e.getMessage());
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("finish", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("fail", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("querybalanceUrl");
        String app_id = configJSONObject.getString("app_id");
        String password = configJSONObject.getString("password");
        String sign = DigestUtils.md5Hex("password=" + password).toUpperCase();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("app_id", app_id);
        map.put("sign", sign);
        try {
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map), "UTF-8"), "", "utf-8", 5000);
            String result_code = JSONObject.parseObject(responseBody).getString("result_code");
            if (StringUtils.equals("00000", result_code)) {
                String balance = JSONObject.parseObject(responseBody).getString("balance");
                return new BigDecimal(balance);
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Test
    void test() {
        Channel channel = new Channel();
        channel.setConfigInfo("{rechargeUrl:\"http://frp.fjwolf.com:9090/frp/api/order/recharge\",app_id:\"172B5EC42C7A39DF20636441F9BDF976\",querybalanceUrl:\"http://frp.fjwolf.com:9090/frp/api/merchant/querybalance\",queryUrl:\"http://frp.fjwolf.com:9090/frp/api/order/querysts\",password:\"39FE92E7AB676F52836F826F56FA0377\"}");
        ChannelOrder channelOrder = new ChannelOrder();
        channelOrder.setChannelOrderId("cs202103021637");
        channelOrder.setRechargeNumber("1000113200019870059");
//        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
        ProcessResult query = query(channel, channelOrder);
        System.out.println("z");
    }
}
