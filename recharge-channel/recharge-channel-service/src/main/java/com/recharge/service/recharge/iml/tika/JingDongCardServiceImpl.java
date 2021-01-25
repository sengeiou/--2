package com.recharge.service.recharge.iml.tika;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.*;
import com.recharge.mapper.IChannelMapper;
import com.recharge.utils.JDDESUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;

/**
 * @author Administrator
 * @create 2020/12/14 9:44
 */
@Service
public class JingDongCardServiceImpl {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IChannelMapper iChannelMapper;
    
    @Resource(name = "configMap")
    private Map<String, String> configMap;

    public void getToken() {
        //1.1 获取Access Token
        //client_id:     vbrQnzHvh4dwom60O5t6
        //
        //clientSecret:     AwGBeJfUJlBHCmS45z4d
        Channel channel = iChannelMapper.selectByChannelId(configMap.get("JD_channel"));
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("getTokenUrl");
        String client_id = configJSONObject.getString("clientId");
        String client_secret = configJSONObject.getString("clientSecret");
        String username = configJSONObject.getString("username");
        String password = DigestUtils.md5Hex(configJSONObject.getString("password"));
        String grant_type = "access_token";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String scope = "";
        String sign = DigestUtils.md5Hex(client_secret + timestamp + client_id + username + password
                + grant_type + scope + client_secret).toUpperCase();
        HashMap<String, String> map = new HashMap<>();
        map.put("grant_type", grant_type);
        map.put("client_id", client_id);
        map.put("timestamp", timestamp);
        map.put("username", username);
        map.put("password", password);
        map.put("scope", scope);
        map.put("sign", sign);
        try {
            logger.info("京东E卡官方接口,获取token接口请求的参数:{}", JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("京东E卡官方接口,获取token接口接收的参数:{}", JSON.toJSONString(responseBody));
            String result = JSONObject.parseObject(responseBody).getString("result");
            String refresh_token = JSONObject.parseObject(result).getString("refresh_token");
            String access_token = JSONObject.parseObject(result).getString("access_token");
            iChannelMapper.updateToken(access_token, refresh_token, configMap.get("JD_channel"));
        } catch (Exception e) {
            logger.error("京东E卡官方接口: 获取token接口请求报错{}", e.getMessage());
        }
    }

    public void flushToken() {
        //1.2 使用Refresh Token 刷新 Access Token
        Channel channel = iChannelMapper.selectByChannelId(configMap.get("JD_channel"));
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String refresh_token = channel.getRemark2();
        String client_id = configJSONObject.getString("clientId");
        String client_secret = configJSONObject.getString("clientSecret");
        String url = configJSONObject.getString("flushUrl");
        HashMap<String, String> map = new HashMap<>();
        map.put("refresh_token", refresh_token);
        map.put("client_id", client_id);
        map.put("client_secret", client_secret);
        try {
            logger.info("京东E卡官方接口,刷新token接口请求的参数:{}", JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("京东E卡官方接口,刷新token接口接收的参数:{}", JSON.toJSONString(responseBody));
            String access_token = JSONObject.parseObject(responseBody).getString("access_token");
            int i = iChannelMapper.updateToken(access_token, refresh_token, configMap.get("JD_channel"));
            System.out.println(responseBody);
        } catch (Exception e) {
            logger.error("京东E卡官方接口: 刷新token请求报错{}", e.getMessage());
        }
    }

    public List<Map<String, String>> recharge(String OrderId, String buynumber,String productName) {
        //2.2.1 礼品卡下单接口
        Channel channel = iChannelMapper.selectByChannelId(configMap.get("JD_channel"));
        logger.info("recharge # 京东渠道号={},channel={}", configMap.get("JD_channel"),JSON.toJSONString(channel));
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String token = channel.getRemark();
        String thirdOrder = OrderId;
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(productName);
        String num = m.replaceAll(" ").trim();
        double f = Double.parseDouble(num);
        DecimalFormat df = new DecimalFormat("#.00");
        String format = df.format(f);
        String sku = "[{\"price\":" + format + ", \"num\":" + buynumber + "}]";
        String mobile = configJSONObject.getString("mobile");//"18262227748";
        String paymentType =configJSONObject.getString("paymentType");// "4";
        String invoiceContent = configJSONObject.getString("invoiceContent");//"44";
        String companyName = configJSONObject.getString("companyName");
        String regCode = configJSONObject.getString("regCode");
        String invoicePhone = configJSONObject.getString("invoicePhone");
        String ifSendMsg = configJSONObject.getString("ifSendMsg");//"false";
        String url = configJSONObject.getString("rechargeUrl");
        HashMap<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("thirdOrder", thirdOrder);
        map.put("sku", sku);
        map.put("mobile", mobile);
        map.put("paymentType", paymentType);
        map.put("invoiceContent", invoiceContent);
        map.put("companyName", companyName);
        map.put("regCode ", regCode);
        map.put("invoicePhone", invoicePhone);
        map.put("ifSendMsg", ifSendMsg);
        List<Map<String, String>> cardInfos = new ArrayList<>();
        try {
            logger.info("京东E卡官方接口,{}发送下单请求的参数:{}", thirdOrder, JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("京东E卡官方接口,{}接收下单请求的参数:{}", thirdOrder, JSON.toJSONString(responseBody));
            String success = JSONObject.parseObject(responseBody).getString("success");
            if (StringUtils.equals(success, "true")) {
                String result = JSONObject.parseObject(responseBody).getString("result");
                String jdOrderId = JSONObject.parseObject(result).getString("jdOrderId");
                List<Map<String, String>> maps = this.queryCardPWD(jdOrderId);
                if (maps.isEmpty()) {
                    return cardInfos;
                }
                return maps;
            } else {
                String resultCode = JSONObject.parseObject(responseBody).getString("resultCode");
                if (StringUtils.equals(resultCode, "403")) {
                    this.flushToken();
                    Map<String, String> infosMap = new HashMap<>();
                    infosMap.put(BuyCardInfo.KEY_CARD_PWD, "ToKen校验失败");
                    cardInfos.add(infosMap);
                    return cardInfos;
                }
                return cardInfos;
            }
        } catch (Exception e) {
            logger.info("京东E卡官方接口: {}发送下单请求报错{}", thirdOrder, e.getMessage());
            return null;
        }
    }

    public List<Map<String, String>> queryCardPWD(String jdOrderId) {
        //2.3 查询E卡卡密信息接口
        Channel channel = iChannelMapper.selectByChannelId(configMap.get("JD_channel"));
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String client_secret = configJSONObject.getString("clientSecret");
        String token = channel.getRemark();
        String url = configJSONObject.getString("queryCardPWDUrl");
//        String jdOrderId = "134179099451";
        HashMap<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("jdOrderId", jdOrderId);
        List<JingDongCard> cards = null;
        List<Map<String, String>> cardInfos = new ArrayList<>();
        try {
            logger.info("京东E卡官方接口,{}查询卡密接口请求的参数:{}", jdOrderId, JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("京东E卡官方接口,{}查询卡密接口返回的参数:{}", jdOrderId, JSON.toJSONString(responseBody));
            String success = JSONObject.parseObject(responseBody).getString("success");
            if (StringUtils.equals(success, "true")) {
                String result = JSONObject.parseObject(responseBody).getString("result");
                JSONArray jsonArray = new JSONArray(JSON.parseArray(result));
                cards = JSONObject.parseArray(jsonArray.toJSONString(), JingDongCard.class);
                for (JingDongCard card : cards) {
                    Map<String, String> infosMap = new HashMap<>();
                    infosMap.put(BuyCardInfo.KEY_CARD_PWD, JDDESUtil.decrypt(card.getPwdKey(), client_secret.substring(0, 8)));
                    infosMap.put(BuyCardInfo.KEY_CARD_EXP_TIME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(card.getActived()));
                    cardInfos.add(infosMap);
                }
                return cardInfos;
            } else {
                String resultCode = JSONObject.parseObject(responseBody).getString("resultCode");
                if (StringUtils.equals(resultCode, "403")) {
                    this.flushToken();
                    return cardInfos;
                } else {
                    String resultMessage = JSONObject.parseObject(responseBody).getString("resultMessage");
                    System.out.println(resultMessage);
                    return cardInfos;
                }
            }
        } catch (Exception e) {
            logger.error("京东E卡官方接口: {}发送查询卡密请求报错{}", jdOrderId, e.getMessage());
            return cardInfos;
        }
    }


    public void queryOrder(String jdOrderId) {
        //2.4 查询订单详情
        Channel channel = iChannelMapper.selectByChannelId(configMap.get("JD_channel"));
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String token = channel.getRemark();
        String url = configJSONObject.getString("queryOrderUrl");
        HashMap<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("jdOrderId", jdOrderId);
        try {
            logger.info("京东E卡官方接口,{}查询订单详情接口请求的参数:{}", jdOrderId, JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("京东E卡官方接口,{}查询订单详情接口返回的参数:{}", jdOrderId, JSON.toJSONString(responseBody));
            String success = JSONObject.parseObject(responseBody).getString("success");
            if (StringUtils.equals(success, "true")) {
                String result = JSONObject.parseObject(responseBody).getString("result");
                System.out.println(result);
            } else {
                String resultMessage = JSONObject.parseObject(responseBody).getString("resultMessage");
                System.out.println(resultMessage);
            }
        } catch (Exception e) {
            logger.error("京东E卡官方接口: {}发送查询卡密请求报错{}", jdOrderId, e.getMessage());
            e.printStackTrace();
        }
    }


    public String queryJDOrderId(String orderid) {
        //2.6 反查京东订单信息接口
        Channel channel = iChannelMapper.selectByChannelId(configMap.get("JD_channel"));
        logger.info("queryJDOrderId # 京东渠道号={},channel={}", configMap.get("JD_channel"),JSON.toJSONString(channel));
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String token = channel.getRemark();
        String url = configJSONObject.getString("queryJDOrderIdUrl");
        String thirdOrder = orderid;
        HashMap<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("thirdOrder", thirdOrder);
        try {
            logger.info("京东E卡官方接口,{}反查京东订单信息请求的参数:{}", orderid, JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("京东E卡官方接口,{}反查京东订单信息请求的参数:{}", orderid, JSON.toJSONString(responseBody));
            String success = JSONObject.parseObject(responseBody).getString("success");
            if (StringUtils.equals(success, "true")) {
                String result = JSONObject.parseObject(responseBody).getString("result");
                String jdOrderId = JSONObject.parseObject(result).getString("jdOrderId");
                return jdOrderId;
            } else {
                String code = JSONObject.parseObject(responseBody).getString("resultCode");
                if ("404".equals(code)) {
                    return "404";
                }
                return null;
            }
        } catch (Exception e) {
            logger.error("京东E卡官方接口: {}反查京东订单信息请求报错{}", orderid, e.getMessage());
            return null;
        }
    }

    public void balanceQuery() {
        //2.7 查询用户余额接口
        Channel channel = iChannelMapper.selectByChannelId(configMap.get("JD_channel"));
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String token = channel.getRemark();
        String url = configJSONObject.getString("balanceQueryUrl");
        String type = "4";
        HashMap<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("type", type);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            String success = JSONObject.parseObject(responseBody).getString("success");
            if (StringUtils.equals(success, "true")) {
                String result = JSONObject.parseObject(responseBody).getString("result");
                System.out.println(result);
            } else {
            }
            System.out.println(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
