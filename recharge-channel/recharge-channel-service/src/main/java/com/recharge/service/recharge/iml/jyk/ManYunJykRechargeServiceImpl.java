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
import com.recharge.service.recharge.AbsChannelRechargeService;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020/12/31 13:53
 */
@Service
public class ManYunJykRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
//        String url="http://121.196.145.243:8688/recharge/submit";
//        String password="1q2w3e4r5t";
//        String username="manyun001";
        String url = configJSONObject.getString("rechargeUrl");
        String username = configJSONObject.getString("username");
        String password = configJSONObject.getString("password");
//        String notifyUrl="https://www.baidu.com/";
        String notifyUrl = configJSONObject.getString("notifyUrl");
        Long timestamp = System.currentTimeMillis();
//        String orderNo="cs202101040919";
        String orderNo = channelOrder.getChannelOrderId();
//        String accountNumber="1000113200019870059";
        String accountNumber = jykRechargeInfoBean.getAccount();
//        Integer amount=100;
        String amount = jykRechargeInfoBean.getAmt();
        String sign = DigestUtils.md5DigestAsHex((username + timestamp + DigestUtils.md5DigestAsHex(password.getBytes())).getBytes());
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("timestamp", timestamp.toString());
        map.put("sign", sign);
        map.put("orderNo", orderNo);
        map.put("notifyUrl", notifyUrl);
        map.put("accountNumber", accountNumber);
        map.put("amount", amount);
        try {
            logger.info("ManYunJYK send recharge request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map)), "", "utf-8", 5000);
            logger.info("ManYunJYK send recharge request params:{}", JSONObject.toJSONString(responseBody));
            String status = JSONObject.parseObject(responseBody).getString("status");
            if (StringUtil.equals("true", status)) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtil.equals("false", status)) {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
            }
        } catch (Exception e) {
            logger.error("{}ManYunJYK send error{}", rechargeOrderBean.getOrderId(), e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑:" + e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("queryUrl");
        String username = configJSONObject.getString("username");
        String password = configJSONObject.getString("password");
        String orderNo = channelOrder.getChannelOrderId();
//        String orderNo="cs202101040919";
        Long timestamp = System.currentTimeMillis();
        String sign = DigestUtils.md5DigestAsHex((username + timestamp + DigestUtils.md5DigestAsHex(password.getBytes())).getBytes());
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("timestamp", timestamp.toString());
        map.put("sign", sign);
        map.put("orderNo", orderNo);
        try {
            logger.info("ManYunJYK send query request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map)), "", "utf-8", 5000);
            logger.info("ManYunJYK send query request params:{}", JSONObject.toJSONString(responseBody));
            String status = JSONObject.parseObject(responseBody).getString("status");
            if (StringUtil.equals("true", status)) {
                String successcount = JSONObject.parseObject(status).getString("successcount");
                String failurecount = JSONObject.parseObject(status).getString("failurecount");
                String doingcount = JSONObject.parseObject(status).getString("doingcount");
                if (StringUtil.equals("1", successcount)) {
                    String serialNumber = JSONObject.parseObject(status).getString("serialNumber");
                    channelOrder.setOutChannelOrderId(serialNumber);
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (StringUtil.equals("1", failurecount)) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else if (StringUtil.equals("1", doingcount)) {
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                } else {
                    return new ProcessResult(ProcessResult.UNKOWN, "查询失败");
                }
            } else if (StringUtil.equals("false", status)) {
                return new ProcessResult(ProcessResult.UNKOWN, "查询失败");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "查询失败");
            }
        } catch (Exception e) {
            logger.error("{}ManYunJYK send query error{}", orderNo, e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "查询失败:" + e.getMessage());
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("3", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "未知错误，找供货商核实");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
//        String url="http://121.196.145.243:8688/recharge/balance";
        String url = configJSONObject.getString("queryBalanceUrl");
//        String username = "manyun001";
//        String password = "1q2w3e4r5t";
        String username = configJSONObject.getString("username");
        String password = configJSONObject.getString("password");
        Long timestamp = System.currentTimeMillis();
        String sign = DigestUtils.md5DigestAsHex((username + timestamp + DigestUtils.md5DigestAsHex(password.getBytes())).getBytes());
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("timestamp", timestamp.toString());
        map.put("sign", sign);

        try {
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(JSON.toJSONString(map)), "", "utf-8", 5000);
            String status = JSONObject.parseObject(responseBody).getString("status");
            if (StringUtil.equals("true", status)) {
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
    public void test() {
        ChannelOrder channelOrder = new ChannelOrder();
        Channel channel = new Channel();
        channel.setConfigInfo("{username:\"manyun001\",rechargeUrl:\"http://121.196.145.243:8688/recharge/submit\",password:\"1q2w3e4r5t\",notifyUrl:\"http://139.129.85.83:8082/manYunJyk/callback\",queryUrl:\"http://121.196.145.243:8688/recharge/queryOrder\",queryBalanceUrl:\"http://121.196.145.243:8688/recharge/balance\"}");
        channelOrder.setChannelOrderId("cs202101040919");
        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
//        ProcessResult query = query(channel, channelOrder);
        System.out.println("z");
    }
}
