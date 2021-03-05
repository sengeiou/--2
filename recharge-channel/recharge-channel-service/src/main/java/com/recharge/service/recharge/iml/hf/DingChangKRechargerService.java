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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Administrator
 * @create 2021/2/23 17:00
 */
@Service
public class DingChangKRechargerService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String agent = configJSONObject.getString("agent");
        String token = configJSONObject.getString("token");
        String out_order_no = channelOrder.getChannelOrderId();
        String amount = huaFeiRechargeInfoBean.getAmt().toString();
        String number = huaFeiRechargeInfoBean.getPhone();
        String returl = configJSONObject.getString("notifyUrl");
        String sign = DigestUtils.md5Hex(agent + out_order_no + amount + number + returl + token).toLowerCase();
        Map<String, String> map = new TreeMap<String, String>();
        map.put("agent", agent);
        map.put("out_order_no", out_order_no);
        map.put("amount", amount);
        map.put("number", number);
        map.put("returl", returl);
        map.put("sign", sign);
        try {
            logger.info("{}鼎昌快充,发送充值的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 10000);
            logger.info("{}鼎昌快充,接收充值的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("code");
            if (StringUtils.equals(code, "1")) {
                String status = JSONObject.parseObject(responseBody).getString("status");
                if (StringUtils.equals(status, "1")) {
                    return new ProcessResult(ProcessResult.SUCCESS, "充值中");
                } else if (StringUtils.equals(status, "4")) {
                    return new ProcessResult(ProcessResult.FAIL, "发送失败");
                }else {
                    return new ProcessResult(ProcessResult.UNKOWN, "发送可疑找供货商核实");
                }
            } else if (StringUtils.equals(code, "5007")) {
                return new ProcessResult(ProcessResult.UNKOWN, "供货商系统错误，找供货商核实");
            } else {
                String note = JSONObject.parseObject(responseBody).getString("note");
                return new ProcessResult(ProcessResult.FAIL, "发送失败:" + note);
            }
        } catch (Exception e) {
            logger.error("鼎昌订单号: {} send error{}", rechargeOrderBean.getOrderId(), e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "发送可疑" + e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String agent = configJSONObject.getString("agent");
        String token = configJSONObject.getString("token");
        String url = configJSONObject.getString("queryUrl");
        String out_order_no = channelOrder.getChannelOrderId();
        String datetime = new SimpleDateFormat("YYYYmmddHHmmss").format(new Date());
        String sign = DigestUtils.md5Hex(agent+out_order_no+datetime+token).toLowerCase();
        Map<String, String> map = new TreeMap<String, String>();
        map.put("agent", agent);
        map.put("out_order_no", out_order_no);
        map.put("datetime", datetime);
        map.put("sign", sign);
        try {
            logger.info("{}鼎昌快充,发送查询的参数:{}", channelOrder.getChannelOrderId(), JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 10000);
            logger.info("{}鼎昌快充,接收查询的参数:{}", channelOrder.getChannelOrderId(), JSON.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("code");
            if (StringUtils.equals(code, "1")) {
                String status = JSONObject.parseObject(responseBody).getString("status");
                if(StringUtils.equals(status, "0")){
                    String refid = JSONObject.parseObject(responseBody).getString("refid");
                    if(!refid.isEmpty()){
                        channelOrder.setOutChannelOrderId(refid);
                    }
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                }else if(StringUtils.equals(status, "4")){
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                }else if(StringUtils.equals(status, "1")){
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                }else if(StringUtils.equals(status, "5")){
                    return new ProcessResult(ProcessResult.FAIL, "订单不存在");
                }else {
                    return new ProcessResult(ProcessResult.UNKOWN, "查询可疑");
                }
            } else {
                String note = JSONObject.parseObject(responseBody).getString("note");
                return new ProcessResult(ProcessResult.UNKOWN, "查询可疑:"+note);
            }
        } catch (Exception e) {
            logger.error("{}鼎昌快充,发送查询出错的参数:{}", channelOrder.getChannelOrderId(), e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "查询可疑:"+e.getMessage());
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("0", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("4", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String agent = configJSONObject.getString("agent");
        String token = configJSONObject.getString("token");
        String url = configJSONObject.getString("queryBalanceUrl");
        String datetime = new SimpleDateFormat("YYYYmmddHHmmss").format(new Date());
        String sign = DigestUtils.md5Hex(agent + datetime + token).toLowerCase();

        Map<String, String> map = new TreeMap<String, String>();
        map.put("agent", agent);
        map.put("datetime", datetime);
        map.put("sign", sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 10000);
            String code = JSONObject.parseObject(responseBody).getString("code");
            if (StringUtils.equals(code, "1")) {
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
        Channel channel = new Channel();
        ChannelOrder channelOrder = new ChannelOrder();
//        channelOrder.setChannelOrderId("cs202101041739");
//        channelOrder.setChannelOrderId("cs202101041807");
//        channelOrder.setChannelOrderId("cs202102231729");
        channelOrder.setChannelOrderId("cs202102231741");
        channel.setConfigInfo("{agent:\"njyj_fast\",rechargeUrl:\"http://120.92.16.66:8080/recharge/submit\",token:\"B1C410DBABEE4F80A500D9D754EBFB2F\",notifyUrl:\"http://139.129.85.83:8082/dingChangK/callBack\",queryUrl:\"http://120.92.16.66:8080/recharge/query\",queryBalanceUrl:\"http://120.92.16.66:8080/recharge/balance\"}");
//        channel.setConfigInfo("{agent:\"pu_up\",rechargeUrl:\"http://120.92.16.66:8080/recharge/submit\",token:\"57215E50CD9142C08632188061BE9A0A\",notifyUrl:\"http://139.129.85.83:8082/dingChang/callback\",queryUrl:\"http://120.92.16.66:8080/recharge/query\",queryBalanceUrl:\"http://120.92.16.66:8080/recharge/balance\"}");
//        channel.setConfigInfo("{agent:\"pu_up\",rechargeUrl:\"http://120.92.16.66:8080/recharge/submit\",token:\"57215E50CD9142C08632188061BE9A0A\",notifyUrl:\"http://139.129.85.83:8082/manYunJyk/callback\",queryUrl:\"http://120.92.16.66:8080/recharge/query\",queryBalanceUrl:\"http://120.92.16.66:8080/recharge/balance\"}");
        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
//        ProcessResult query = query(channel, channelOrder);
        System.out.println("z");
    }
}
