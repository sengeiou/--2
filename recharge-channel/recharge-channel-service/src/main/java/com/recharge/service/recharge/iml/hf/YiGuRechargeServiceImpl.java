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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author user
 * @create 2020/10/19 17:22
 */
@Service
public class YiGuRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String userid = configJSONObject.getString("userid");
        String price = huaFeiRechargeInfoBean.getAmt().toString();
        String num = "1";
        String spordertime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String sporderid = channelOrder.getChannelOrderId();
        String sign = "";
        String back_url = configJSONObject.getString("callback");
        String mobile = huaFeiRechargeInfoBean.getPhone();
        String key = configJSONObject.getString("key");
        sign = DigestUtils.md5Hex("userid=" + userid + "&price=" + price + "&num=" + num + "&mobile=" + mobile + "&spordertime=" + spordertime + "&sporderid=" + sporderid + "&key=" + key).toUpperCase();

        Map<String, String> map = new HashMap<>();
        map.put("userid", userid);
        map.put("price", price);
        map.put("num", num);
        map.put("spordertime", spordertime);
        map.put("sporderid", sporderid);
        map.put("sign", sign);
        map.put("back_url", back_url);
        map.put("mobile", mobile);
        try {
            logger.info("yiGu send recharge request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("yiGu send recharge response :{}", responseBody);
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
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (Exception e) {
            logger.error("{}send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
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
            logger.info("yiGu send query request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("yiGu send query response :{}", responseBody);
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
        } else {
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

}
