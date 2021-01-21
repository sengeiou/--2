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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020/12/22 16:02
 */
@Service
public class YiGuJykRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String userid = configJSONObject.getString("userid");
        String price = jykRechargeInfoBean.getAmt();
        String num = "1";
        String spordertime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String sporderid = channelOrder.getChannelOrderId();
        String back_url = configJSONObject.getString("callback");
        String type = jykRechargeInfoBean.getType();
        String product = null;
        if (StringUtils.equals("ZSH", type)) {
            product = "sinopec";
        } else if (StringUtils.equals("ZSY", type)) {
            product = "cnpc";
        } else {
            return new ProcessResult(ProcessResult.FAIL, "类型不正确");
        }
        String account = jykRechargeInfoBean.getAccount();
        String key = configJSONObject.getString("key");
        String sign = DigestUtils.md5Hex("product=" + product + "&userid=" + userid + "&price=" + price + "&num=" + num + "&account=" + account + "&spordertime=" + spordertime + "&sporderid=" + sporderid + "&key=" + key).toUpperCase();

        Map<String, String> map = new HashMap<>();
        map.put("userid", userid);
        map.put("price", price);
        map.put("num", num);
        map.put("spordertime", spordertime);
        map.put("sporderid", sporderid);
        map.put("sign", sign);
        map.put("back_url", back_url);
        map.put("product", product);
        map.put("account", account);
        try {
            logger.info("yiGuJYK send recharge request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("yiGuJYK send recharge response :{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            if (StringUtils.equals(resultno, "0")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals(resultno, "5006")) {
                return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
            } else if (StringUtils.equals(resultno, "5002")) {
                return new ProcessResult(ProcessResult.FAIL, "余额不足");
            } else if (StringUtils.equals(resultno, "9999")) {
                return new ProcessResult(ProcessResult.UNKOWN, "未知错误，找yigu核实");
            } else if (StringUtils.equals(resultno, "1")) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (Exception e) {
            logger.error("{}send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑,原因为" + e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("queryUrl");
        String userid = configJSONObject.getString("userid");
        String sporderid = channelOrder.getChannelOrderId();
        Map<String, String> map = new HashMap<>();
        map.put("userid", userid);
        map.put("sporderid", sporderid);
        try {
            logger.info("yiGuJYK send query request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("yiGuJYK send query response :{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            if (StringUtils.equals(resultno, "1")) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals(resultno, "0")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else if (StringUtils.equals(resultno, "9999")) {
                return new ProcessResult(ProcessResult.UNKOWN, "未知错误，找yigu核实");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            }
        } catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("0", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        } else if (StringUtils.equals("9999", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.UNKOWN, "未知错误，找yigu核实");
        }else {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String balanceUrl = configJSONObject.getString("balanceUrl");
        String userid = configJSONObject.getString("userid");
        String key = configJSONObject.getString("key");
        String sign = DigestUtils.md5Hex("userid=" + userid + "&key=" + key).toUpperCase();
        Map<String, String> map = new HashMap<>();
        map.put("userid", userid);
        map.put("sign", sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(balanceUrl, map, "utf-8", 5000);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String balance = root.elementText("balance");
            return new BigDecimal(balance);
        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }


    }

    @Test
    public void test() {
        YiGuJykRechargeServiceImpl yiGuJykRechargeService = new YiGuJykRechargeServiceImpl();
        Channel channel = new Channel();
        channel.setConfigInfo("{userid:\"700018\",queryUrl:\"http://115.159.158.106:8899/query.do\",key:\"ZChZ17PH303Mn6By53bKtgypyDW5G0M9\",rechargeUrl:\"http://115.159.158.106:8899/order.do\",balanceUrl:\"http://115.159.158.106:8899/balance.do\",callback:\"http://139.129.85.83:8082/yiGuJyk/callBack\"}");
        ChannelOrder channelOrder = new ChannelOrder();
//        channelOrder.setChannelOrderId("cs202012221637");
        channelOrder.setChannelOrderId("cs202012300946");
//        BigDecimal bigDecimal = yiGuJykRechargeService.balanceQuery(channel);
        ProcessResult recharge = yiGuJykRechargeService.recharge(channel, channelOrder, new RechargeOrderBean());
//        ProcessResult query = yiGuJykRechargeService.query(channel, channelOrder);
    }

}
