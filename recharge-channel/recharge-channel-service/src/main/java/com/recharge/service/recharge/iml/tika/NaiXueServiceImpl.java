package com.recharge.service.recharge.iml.tika;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.*;
import com.recharge.mapper.IChannelMapper;
import com.recharge.mapper.IProductRelationMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2020/12/2 16:56
 */
@Service
public class NaiXueServiceImpl {

    @Autowired
    private IProductRelationMapper iProductRelationMapper;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private IChannelMapper iChannelMapper;

    @Resource(name = "configMap")
    private Map<String, String> configMap;

    /**
     * 生成签名
     *
     * @param timestamp
     * @param nonce
     * @param partnerId
     * @param key
     * @return
     */
    public static String getSignature(String timestamp, String nonce, String partnerId, String key) {
        Map<String, String> map = new HashMap<>();
        map.put("Partner-Id", partnerId);
        map.put("Timestamp", timestamp);
        map.put("Nonce", nonce);
        map.put("Secret-Key", key);
        String var1 = buildVar(map);
        return sha1(var1);
    }

    private static String buildVar(Map<String, String> param) {
        SortedMap<String, Object> parameters = new TreeMap(param);
        return parameters.entrySet()
                .stream()
                .map(i -> i.getKey() + i.getValue())
                .collect(Collectors.joining());
    }

    private static String sha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));

            byte[] md = mdTemp.digest();
            int j = md.length;
            char[] buf = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }


    public List<Map<String, String>> NaiXueBuyCode(String orderId,String buynumber,String productName) {
        //获取渠道信息
        Channel channel = iChannelMapper.selectByChannelId(configMap.get("naixue_channel"));
        if (channel == null) {
            logger.info("渠道未配置....无法提卡");
            return new ArrayList<Map<String,String>>();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, 12);
        //读取配置文件，获取对应的信息
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String partnerId = configJSONObject.getString("Partner-Id");
        String secretKey = configJSONObject.getString("secretKey");
        String timestamp = Integer.toString((int) Math.floor(System.currentTimeMillis() / 1000 / 600) * 600);
        String nonce = Integer.toString(new Random(100).nextInt());
        String signature = getSignature(timestamp, nonce, partnerId, secretKey);
        String serialNo = orderId;
        ProductRelation productRelation = iProductRelationMapper.selectByName(productName, configMap.get("naixue_channel"));
        String activeCode = productRelation.getChannelProductId();
        String validStart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String validEnds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
        HashMap<String, String> map = new HashMap<>();
        map.put("serialNo", serialNo);
        map.put("validStart", validStart);
        map.put("validEnds", validEnds);
        map.put("activeCode", activeCode);
        map.put("number",buynumber);
        String requestString = JSONObject.toJSONString(map);
        HashMap<String, String> handermap = new HashMap<>();
        handermap.put("Partner-Id", partnerId);
        handermap.put("Timestamp", timestamp);
        handermap.put("Nonce", nonce);
        handermap.put("Signature", signature);
        List<NaiXueCode> cards = null;
        List<Map<String, String>> cardInfos = new ArrayList<>();
        try {
            logger.info("奈雪卡购买" + "==>请求信息==>" + JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonStringWithHeader(url, new StringEntity(requestString), handermap, "utf-8", 5000);
            logger.info("奈雪卡购买" + "==>响应信息==>" + JSON.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("code");
            if (StringUtils.equals(code, "0")) {
                JSONObject responseObject = JSONObject.parseObject(responseBody);
                JSONObject data = responseObject.getJSONObject("data");
                String codes = data.getString("codes");
                JSONArray jsonArray = new JSONArray(JSON.parseArray(codes));
                cards = JSONObject.parseArray(jsonArray.toJSONString(), NaiXueCode.class);
                for (NaiXueCode card : cards) {
                    Map<String, String> infosMap = new HashMap<>();
                    infosMap.put(BuyCardInfo.KEY_CARD_PWD, card.getCode());
                    infosMap.put(BuyCardInfo.KEY_CARD_EXP_TIME, validEnds);
                    cardInfos.add(infosMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("奈雪提卡出错原因="+e.getMessage());
            return cardInfos;
        }
        return cardInfos;
    }


    public String recycleCode(RechargeOrder rechargeOrder, String code) {
        //获取渠道信息
        Channel channel = iChannelMapper.selectByChannelId(configMap.get("naixue_channel"));
        if (channel == null) {
            logger.info("渠道未配置....无法退款");
            return "退款失败";
        }
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, 1);
        //读取配置文件，获取对应的信息
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("recycleUrl");
        String partnerId = configJSONObject.getString("Partner-Id");
        String timestamp = Integer.toString((int) Math.floor(System.currentTimeMillis() / 1000 / 600) * 600);
        String nonce = Integer.toString(new Random(100).nextInt());
        String secretKey = configJSONObject.getString("secretKey");

        String signature = getSignature(timestamp, nonce, partnerId, secretKey);
        String serialNo = rechargeOrder.getOrderId();
        HashMap<String, String> map = new HashMap<>();
        map.put("serialNo", serialNo);
        map.put("code", code);
        String requestString = JSONObject.toJSONString(map);
        HashMap<String, String> handermap = new HashMap<>();
        handermap.put("Partner-Id", partnerId);
        handermap.put("Timestamp", timestamp);
        handermap.put("Nonce", nonce);
        handermap.put("Signature", signature);
        try {
            logger.info("奈雪卡回收（退款）" + "==>请求信息==>" + JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonStringWithHeader(url, new StringEntity(requestString), handermap, "utf-8", 5000);
            logger.info("奈雪卡回收（退款）" + "==>请求信息==>" + JSON.toJSONString(responseBody));
            String message = JSONObject.parseObject(responseBody).getString("message");
        } catch (Exception e) {
            logger.error("回收失败{}", e.getMessage());
        }
        return "ok";
    }


    public BuyCardInfo querycode(String code) {
        BuyCardInfo buyCardInfo = new BuyCardInfo();
        //获取渠道信息
        Channel channel = iChannelMapper.selectByChannelId(configMap.get("naixue_channel"));
        if (channel == null) {
            logger.info("渠道未配置....无法提卡");
            return buyCardInfo;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, 1);
        //读取配置文件，获取对应的信息
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String partnerId = configJSONObject.getString("Partner-Id");
        String timestamp = Integer.toString((int) Math.floor(System.currentTimeMillis() / 1000 / 600) * 600);
        String nonce = Integer.toString(new Random(100).nextInt());
        String secretKey = configJSONObject.getString("secretKey");
        String signature = getSignature(timestamp, nonce, partnerId, secretKey);
        String url = configJSONObject.getString("queryUrl");
        HashMap<String, String> map = new HashMap<>();
        map.put("code", code);
        String requestString = JSONObject.toJSONString(map);
        HashMap<String, String> handermap = new HashMap<>();
        handermap.put("Partner-Id", partnerId);
        handermap.put("Timestamp", timestamp);
        handermap.put("Nonce", nonce);
        handermap.put("Signature", signature);
        List<Map<String, String>> cardInfos = new ArrayList<>();
        try {
            logger.info("奈雪卡查询" + "==>请求信息==>" + JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonStringWithHeader(url, new StringEntity(requestString), handermap, "utf-8", 5000);
            logger.info("奈雪卡查询" + "==>请求信息==>" + JSON.toJSONString(responseBody));
            String rescode = JSONObject.parseObject(responseBody).getString("code");
            if (StringUtils.equals(rescode, "0")) {
                JSONObject responseObject = JSONObject.parseObject(responseBody);
                JSONObject data = responseObject.getJSONObject("data");
                String codes = data.getString("code");
                String validEnds = data.getString("validEnds");
                Map<String, String> infosMap = new HashMap<>();
                infosMap.put(BuyCardInfo.KEY_CARD_PWD, codes);
                infosMap.put(BuyCardInfo.KEY_CARD_EXP_TIME, validEnds);
                cardInfos.add(infosMap);
            } else {
                return buyCardInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return buyCardInfo;
        }
        logger.info("奈雪卡查询成功.......");
        buyCardInfo.setCardInfo(JSONObject.toJSONString(cardInfos));
        return buyCardInfo;
    }

}
