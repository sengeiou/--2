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
import org.apache.http.conn.ConnectTimeoutException;
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

/**安畅手支
 * @author user
 * @date 2020/3/13 12:53
 */
@Service
public class AnChangSZRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String key = configJSONObject.getString("key");
        String userid = configJSONObject.getString("userid");
        String back_url=configJSONObject.getString("callback");
        String productid = "";
        String price = huaFeiRechargeInfoBean.getAmt().toString();
        String num = "1";
        String mobile = huaFeiRechargeInfoBean.getPhone();
        String spordertime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        //这是充值订单号，PS开头的，
        String sporderid = channelOrder.getChannelOrderId();
        String gascardtel = "";
        String gascardname = "";
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid", userid);
        requestMap.put("productid", productid);
        requestMap.put("price", price);
        requestMap.put("num", num);
        requestMap.put("mobile", mobile);
        requestMap.put("spordertime", spordertime);
        requestMap.put("sporderid", sporderid);
        requestMap.put("gascardtel", gascardtel);
        requestMap.put("gascardname", gascardname);
        requestMap.put("sign", DigestUtils.md5Hex("userid=" + userid
                + "&productid=" + productid
                + "&price=" + price
                + "&num=" + num
                + "&mobile=" + mobile
                + "&spordertime=" + spordertime
                + "&sporderid=" + sporderid
                + "&key=" + key));
        requestMap.put("back_url",back_url);
        try {
            logger.info("send recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");

            if (StringUtils.equals(resultno, "1") || StringUtils.equals(resultno, "0") || StringUtils.equals(resultno, "2")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }

        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String queryUrl = configJSONObject.getString("queryUrl");
        String userid = configJSONObject.getString("userid");
        String sporderid = channelOrder.getChannelOrderId();

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userid", userid);
        requestMap.put("sporderid", sporderid);
        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");

            if (StringUtils.equals(resultno, "1")) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals(resultno, "0")) {
                return new ProcessResult(ProcessResult.PROCESSING, "等待充值");
            } else if (StringUtils.equals(resultno, "2")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            }

        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
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
        } else {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String queryUrl = configJSONObject.getString("queryBalanceUrl");
        String userId = configJSONObject.getString("userid");
        String key = configJSONObject.getString("key");

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userid", userId);
        requestMap.put("sign", DigestUtils.md5Hex("userid=" + requestMap.get("userid")
                + "&key=" + key
        ));
        try {
            logger.info("send queryBalance request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);

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
