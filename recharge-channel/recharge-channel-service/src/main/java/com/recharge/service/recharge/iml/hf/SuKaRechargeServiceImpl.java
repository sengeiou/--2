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
 * @create 2021/1/13 13:47
 */
@Service
public class SuKaRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        ProductRelation productRelation = new ProductRelation();
        String userid = configJSONObject.getString("userid");
        productRelation = queryChannelProductId("全国" + huaFeiRechargeInfoBean.getOperator() + huaFeiRechargeInfoBean.getAmt() + "元", "100132");
        if (productRelation == null) {
            return new ProcessResult(ProcessResult.FAIL, "供货商商品ID未配置");
        }
        //设置商品编号
        String productid = productRelation.getChannelProductId();
        //设置商品面值
        String price = huaFeiRechargeInfoBean.getAmt().toString();
        //设置商品数量
        String num = "1";
        //设置充值账号
        String mobile = huaFeiRechargeInfoBean.getPhone();
        //设置订单时间
        String spordertime = new SimpleDateFormat("YYYYMMDD hhMMss").format(new Date());
        //设置订单ID
        String sporderid = channelOrder.getChannelOrderId();
        String key = configJSONObject.getString("key");
        String sign = DigestUtils.md5Hex(
                "userid=" + userid
                        + "&productid=" + productid
                        + "&price=" + price
                        + "&num=" + num
                        + "&mobile=" + mobile
                        + "&spordertime=" + spordertime
                        + "&sporderid=" + sporderid
                        + "&key=" + key);
        String back_url = configJSONObject.getString("callback");
        String operator = huaFeiRechargeInfoBean.getOperator();
        String payType = "";
        if (StringUtils.equals("移动", operator)) {
            payType = "yd";
        } else if (StringUtils.equals("联通", operator)) {
            payType = "lt";
        } else if (StringUtils.equals("电信", operator)) {
            payType = "dx";
        } else {
            return new ProcessResult(ProcessResult.FAIL, "运营商类型错误");
        }
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid", userid);
        requestMap.put("productid", productid);
        requestMap.put("price", price);
        requestMap.put("num", num);
        requestMap.put("mobile", mobile);
        requestMap.put("spordertime", spordertime);
        requestMap.put("sporderid", sporderid);
        requestMap.put("sign", sign);
        requestMap.put("back_url", back_url);
        requestMap.put("paytype", payType);
        try {
            logger.info("速卡下单接口请求参数:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(configJSONObject.getString("rechargeUrl"), requestMap, "utf-8", 5000);
            logger.info("速卡下单接口返回参数 :{}", JSONObject.toJSONString(responseBody));
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            if (StringUtils.equals(resultno, "0")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals(resultno, "5002")) {
                return new ProcessResult(ProcessResult.FAIL, "余额不足");
            } else if (StringUtils.equals(resultno, "9999")) {
                return new ProcessResult(ProcessResult.UNKOWN, "需要跟供货商核实");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (Exception e) {
            logger.error("速卡下单接口报错参数{}+{}", rechargeOrderBean.getOrderId(), e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }

    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String userid = configJSONObject.getString("userid");
        String sporderid = channelOrder.getChannelOrderId();
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid", userid);
        requestMap.put("sporderid", sporderid);
        String url = configJSONObject.getString("queryUrl");
        try {
            logger.info("速卡查询接口请求参数:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("速卡查询接口请求参数:{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            if (StringUtils.equals(resultno, "1")) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals(resultno, "9")) {
                return new ProcessResult(ProcessResult.FAIL, "充值失败已退款");
            } else if (StringUtils.equals(resultno, "0")) {
                return new ProcessResult(ProcessResult.PROCESSING, "等待充值");
            } else if (StringUtils.equals(resultno, "2")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else if (StringUtils.equals(resultno, "9999")) {
                return new ProcessResult(ProcessResult.UNKOWN, "需要跟供货商核实");
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "未知");
            }
        } catch (Exception e) {
            logger.error("速卡查询接口报错参数{}+{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑" + e.getMessage());
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(), "1")) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "0")) {
            return new ProcessResult(ProcessResult.PROCESSING, "等待充值");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "2")) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        }else if (StringUtils.equals(responseOrder.getResponseCode(), "9")) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "未知");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String userid = configJSONObject.getString("userid");
        String sign = DigestUtils.md5Hex("userid=" + userid + "&key=" + configJSONObject.getString("key"));
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid", userid);
        requestMap.put("sign", sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(configJSONObject.getString("queryBalanceUrl"), requestMap, "utf-8", 5000);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String balance = root.elementText("balance");
            return new BigDecimal(balance);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Test
    void test() {
        Channel channel = new Channel();
        ChannelOrder channelOrder = new ChannelOrder();
        channel.setConfigInfo("{\"rechargeUrl\":\"http://47.114.82.209:9086/onlinepay.do\",\"userid\":\"10002474\",\"key\":\"5FZGTMrYE5f4amM5KatHdTDRER3XTSAm\",\"queryUrl\":\"http://47.114.82.209:9086/searchpay.do\",\"queryBalanceUrl\":\"http://47.114.82.209:9086/searchbalance.do\",\"callback\":\"http://139.129.85.83:8082/suKa/callBack\"}");
        channelOrder.setChannelOrderId("cs202101131450");
//        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
        ProcessResult query = query(channel, channelOrder);
        System.out.println("z");
    }
}
