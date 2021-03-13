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
 * @create 2020/11/3 17:33
 */
@Service
public class YuanFeiDJRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String url = configJSONObject.getString("rechargeUrl");
        String userid = configJSONObject.getString("userid");
        String key = configJSONObject.getString("key");
        String productid = "";
        String price = huaFeiRechargeInfoBean.getAmt().toString();
        String num = "1";
        String mobile = huaFeiRechargeInfoBean.getPhone();
        String spordertime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String sporderid = channelOrder.getChannelOrderId();
        String gascardtel = "";
        String gascardname = "";
        String sign = DigestUtils.md5Hex("userid=" + userid + "&productid=" + productid + "&price=" + price + "&num=" + num + "&mobile=" + mobile + "&spordertime=" + spordertime + "&sporderid=" + sporderid + "&key=" + key);
        String back_url = configJSONObject.getString("callback");
        String paytype = "";
        if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "联通")) {
            paytype = "lt";
        } else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "移动")) {
            paytype = "yd";
        } else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "电信")) {
            paytype = "dx";
        } else {
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        }


        Map<String, String> map = new HashMap<>();
        map.put("userid", userid);
        map.put("productid", productid);
        map.put("price", price);
        map.put("num", num);
        map.put("mobile", mobile);
        map.put("spordertime", spordertime);
        map.put("sporderid", sporderid);
        map.put("gascardtel", gascardtel);
        map.put("gascardname", gascardname);
        map.put("sign", sign);
        map.put("back_url", back_url);
        map.put("paytype", paytype);
        try {
            logger.info("yuanFeiDJ send recharge request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("yuanFeiDJ send recharge response :{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            if (StringUtils.equals("0", resultno)) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals("1", resultno)) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals("2", resultno)) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值中");
            } else if (StringUtils.equals("9999", resultno)) {
                return new ProcessResult(ProcessResult.UNKOWN, "需要找供货商核实");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }

        } catch (Exception e) {
            logger.info("{}send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑="+e.getMessage());
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
            logger.info("yuanFeiDJ send query request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("yuanFeiDJ query response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            String remark1 = root.elementText("remark1");
            if (resultno.equals("0")) {
                return new ProcessResult(ProcessResult.PROCESSING, "等待充值");
            } else if (resultno.equals("1")) {
                channelOrder.setOutChannelOrderId(remark1);
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (resultno.equals("9999")) {
                return new ProcessResult(ProcessResult.UNKOWN, "找供货商核实");
            } else if (resultno.equals("2")) {
                return new ProcessResult(ProcessResult.PROCESSING, "等待充值");
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
        } else if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        } else if (StringUtils.equals("0", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.PROCESSING, "等待充值");
        } else if (StringUtils.equals("9999", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.UNKOWN, "需要找供货商核实");
        } else {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String url = configJSONObject.getString("queryBalanceUrl");
        String userid = configJSONObject.getString("userid");
        String key = configJSONObject.getString("key");
        String a = "userid=" + userid + "&key=" + key;
        String sign = DigestUtils.md5Hex(a);
        Map<String, String> map = new HashMap<>();
        map.put("userid", userid);
        map.put("sign", sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String balance = root.elementText("balance");
            return new BigDecimal(balance);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }

    }

}
