package com.recharge.service.recharge.iml.video;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.center.bean.VideoRechargeInfoBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
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
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Administrator
 * @create 2020/4/20 9:50
 */
@Service
public class OfPayCardVideoServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        VideoRechargeInfoBean videoRechargeInfoBean = (VideoRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(VideoRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("url");
        String KeyStr = configJSONObject.getString("KeyStr");
        String userid = configJSONObject.getString("userid");
        String passWorld = configJSONObject.getString("userpws");
        String userpws = DigestUtils.md5Hex(passWorld).toLowerCase();
        ProductRelation productRelation = queryChannelProductId(rechargeOrderBean.getProductName(), "100059");
        String cardid = productRelation.getChannelProductId();

        String cardnum = "1";
        String sporder_id = channelOrder.getChannelOrderId();
        String sporder_time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String game_userid = videoRechargeInfoBean.getAccount();
        String game_userpsw = "";
        String game_area = "";
        String game_srv = "";
        String resourse = userid + userpws + cardid + cardnum + sporder_id + sporder_time + game_userid + game_area + game_srv + KeyStr;
        String md5_str = DigestUtils.md5Hex(resourse).toUpperCase();
        String phoneno = "";
        String ret_url = "";
        String version = "6.0";
        String userip = "";


        Map<String, String> requestMap = new TreeMap<>();
        requestMap.put("userid", userid);
        requestMap.put("userpws", userpws);
        requestMap.put("cardid", cardid);
        requestMap.put("cardnum", cardnum);
        requestMap.put("sporder_id", sporder_id);
        requestMap.put("sporder_time", sporder_time);
        requestMap.put("game_userid", game_userid);
        requestMap.put("game_userpsw", game_userpsw);
        requestMap.put("game_area", game_area);
        requestMap.put("game_srv", game_srv);
        requestMap.put("md5_str", md5_str);
        requestMap.put("phoneno", phoneno);
        requestMap.put("ret_url", ret_url);
        requestMap.put("version", version);
        requestMap.put("userip", userip);

        try {
            logger.info("send recharge request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String game_state = root.elementText("game_state");
            if (StringUtils.equals(game_state, "1")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals(game_state, "0")) {
                return new ProcessResult(ProcessResult.PROCESSING, "处理中");
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

        String url = configJSONObject.getString("queryUrl");
        String userid = configJSONObject.getString("userid");
        String passWorld = configJSONObject.getString("userpws");
        String KeyStr = configJSONObject.getString("KeyStr");
        String userpws = DigestUtils.md5Hex(passWorld).toLowerCase();
        String sporder_id = channelOrder.getChannelOrderId();
        String resourse = userid + userpws + sporder_id + KeyStr;
        String md5_str = DigestUtils.md5Hex(resourse).toUpperCase();
        String version = "6.0";
        Map<String, String> requestMap = new TreeMap<>();
        requestMap.put("userid", userid);
        requestMap.put("userpws", userpws);
        requestMap.put("sporder_id", sporder_id);
        requestMap.put("md5_str", md5_str);
        requestMap.put("version", version);
        try {
            logger.info("send query request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String game_state = root.elementText("game_state");
            if (StringUtils.equals(game_state, "1")) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals(game_state, "0")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            }
        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        String tranState = responseOrder.getResponseCode();
        if (StringUtils.equals("1", tranState)) {
            return new ProcessResult(ProcessResult.SUCCESS, "交易成功");
        } else if (StringUtils.equals("9", tranState)) {
            return new ProcessResult(ProcessResult.FAIL, responseOrder.getResponseMsg());
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "未知返回码");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String queryUrl = configJSONObject.getString("queryBalanceUrl");
        String userid = configJSONObject.getString("userid");
        String passWorld = configJSONObject.getString("userpws");
        String userpws = DigestUtils.md5Hex(passWorld).toLowerCase();
        String version = "6.0";

        Map<String, String> requestMap = new TreeMap<>();
        requestMap.put("userid", userid);
        requestMap.put("userpws", userpws);
        requestMap.put("version", version);
        try {
            logger.info("send queryBalance request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String balance = root.elementText("totalBalance");
            return new BigDecimal(balance);

        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }
    }

}
