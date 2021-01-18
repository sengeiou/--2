package com.recharge.service.recharge.iml.tika;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hisun.crypt.mac.CryptUtilImpl;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.ExtractCardRechargeInfoBean;
import com.recharge.center.bean.HuaFeiRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.*;
import com.recharge.domain.yidian.YiDianActinfo;
import com.recharge.domain.yidian.YiDianCallBackOrderDetail;
import com.recharge.domain.yidian.YiDianCallBackTelephone;
import com.recharge.domain.yidian.YiDianIssueinfo;
import com.recharge.mapper.IBuyCardMapper;
import com.recharge.mapper.IRechargeOrderMapper;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.service.recharge.iml.MerchantCardServiceImpl;
import com.recharge.utils.YiDianDESUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2021/1/11 13:27
 */
@Service
public class YiDianServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IBuyCardMapper iBuyCardMapper;

    @Autowired
    MerchantCardServiceImpl merchantCardServiceImpl;
    @Autowired
    private IRechargeOrderMapper rechargeOrderMapper;

    private List<String> errorCode = Arrays.asList("10002", "10003", "10004", "10005", "10006", "10008", "10009", "10010", "10011", "20001", "20002", "20003", "20004", "20005", "20006", "20008", "20009");

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        ExtractCardRechargeInfoBean extractCardRechargeInfoBean = (ExtractCardRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        JSONObject request = new JSONObject(true);
        JSONObject body = new JSONObject(true);
        JSONObject header = new JSONObject(true);
        JSONObject security = new JSONObject(true);
        ArrayList<YiDianActinfo> actinfo = new ArrayList<YiDianActinfo>();
        YiDianActinfo yiDianActinfo = new YiDianActinfo();
        ArrayList<YiDianIssueinfo> issueinfo = new ArrayList<YiDianIssueinfo>();
        YiDianIssueinfo yiDianIssueinfo = new YiDianIssueinfo();
        String url = configJSONObject.getString("rechargeUrl");
        String md5Key = configJSONObject.getString("md5Key");
        String clientid = configJSONObject.getString("clientId");
        ProductRelation productRelation = queryChannelProductId(rechargeOrderBean.getProductName(), channel.getChannelId());
        String actid = productRelation.getChannelProductId();
        int amount = extractCardRechargeInfoBean.getBuyNumber();
        String telephone = configJSONObject.getString("telephone");
        String clientorderno = channelOrder.getChannelOrderId();
        String orderremark = "xiadan";
        Long reqtime = System.currentTimeMillis();
        String requestid = channelOrder.getChannelOrderId() + "-1";
        String version = configJSONObject.getString("version");
        //私有参数
        yiDianIssueinfo.setAmount(amount);
        yiDianIssueinfo.setTelephone(telephone);
        yiDianActinfo.setActid(actid);
        yiDianActinfo.setIssueinfo(issueinfo);
        issueinfo.add(yiDianIssueinfo);
        actinfo.add(yiDianActinfo);
        body.put("actinfo", actinfo);
        body.put("clientorderno", clientorderno);
        body.put("orderremark", orderremark);
        //通用参数
        header.put("clientid", clientid);
        header.put("reqtime", reqtime);
        header.put("requestid", requestid);
        header.put("version", version);
        request.put("body", body);
        request.put("header", header);
        String signatureValue = DigestUtils.md5Hex(JSON.toJSONString(request) + md5Key).toUpperCase();
        security.put("signvalue", signatureValue);
        request.put("security", security);
        try {
            StringEntity stringEntity = new StringEntity(request.toString());
            logger.info("易点提卡下单接口请求信息:{}", JSONObject.toJSONString(request.toString()));
            String responseData = HttpClientUtils.invokePostString(url, stringEntity, "UTF-8", 5000);
            logger.info("易点提卡下单接口响应信息:{}", JSONObject.toJSONString(responseData));
            String code = JSONObject.parseObject(responseData).getString("code");
            if (StringUtils.equals("0", code)) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals("10001", code)) {
                return new ProcessResult(ProcessResult.UNKOWN, "提交可疑，找供货商核实");
            } else {
                if (errorCode.contains(code)) {
                    return new ProcessResult(ProcessResult.FAIL, "提交失败");
                } else {
                    return new ProcessResult(ProcessResult.UNKOWN, "提交可疑，找供货商核实");
                }
            }
        } catch (Exception e) {
            logger.error("易点提卡下单接口请求异常:{}", e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑，找供货商核实");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        JSONObject request = new JSONObject(true);
        JSONObject body = new JSONObject(true);
        JSONObject header = new JSONObject(true);
        JSONObject security = new JSONObject(true);
        String DESkey = configJSONObject.getString("DESkey");
        String url = configJSONObject.getString("queryUrl");
        String md5Key = configJSONObject.getString("md5Key");
        String clientid = configJSONObject.getString("clientId");
        String orderid = channelOrder.getChannelOrderId();
        Long reqtime = System.currentTimeMillis();
        String requestid = channelOrder.getChannelOrderId() + "-2";
        String version = configJSONObject.getString("version");
        String rechargetelephone = configJSONObject.getString("telephone");
        //通用参数
        header.put("clientid", clientid);
        header.put("reqtime", reqtime);
        header.put("requestid", requestid);
        header.put("version", version);
        //私有参数
        body.put("orderid", orderid);

        request.put("body", body);
        request.put("header", header);

        String signatureValue = DigestUtils.md5Hex(JSON.toJSONString(request) + md5Key).toUpperCase();
        security.put("signvalue", signatureValue);
        request.put("security", security);
        try {
            StringEntity stringEntity = new StringEntity(request.toString());
            logger.info("易点提卡查询接口请求信息:{}", JSONObject.toJSONString(request.toString()));
            String responseData = HttpClientUtils.invokePostString(url, stringEntity, "UTF-8", 5000);
            logger.info("易点提卡查询接口响应信息:{}", JSONObject.toJSONString(responseData));
            String code = JSONObject.parseObject(responseData).getString("code");
            if (StringUtils.equals("0", code)) {
                String data = JSONObject.parseObject(responseData).getString("data");
                String orderstatus = JSONObject.parseObject(data).getString("orderstatus");
                if (StringUtils.equals("3", orderstatus)) {
                    List<Map<String, String>> cardInfos = new ArrayList<>();
                    List<YiDianCallBackOrderDetail> cardDTOS = null;
                    String orderdetail = JSONObject.parseObject(data).getString("orderdetail");
                    JSONArray jsonArray = new JSONArray(JSON.parseArray(orderdetail));
                    cardDTOS = JSONObject.parseArray(jsonArray.toJSONString(), YiDianCallBackOrderDetail.class);
                    for (YiDianCallBackOrderDetail cardDTO : cardDTOS) {
                        Object codedetail = cardDTO.getCodedetail();
                        String telephone = JSONObject.parseObject(JSONObject.toJSONString(codedetail)).getString(rechargetelephone);
                        JSONArray telephoneArray = new JSONArray(JSON.parseArray(telephone));
                        List<YiDianCallBackTelephone> cards = null;
                        cards = JSONObject.parseArray(telephoneArray.toJSONString(), YiDianCallBackTelephone.class);
                        for (YiDianCallBackTelephone card : cards) {
                            Map<String, String> infosMap = new HashMap<>();
                            infosMap.put(BuyCardInfo.KEY_CARD_NO, YiDianDESUtils.decrypt(card.getBarcode(), DESkey));
                            infosMap.put(BuyCardInfo.KEY_CARD_PWD, YiDianDESUtils.decrypt(card.getBarpwd(), DESkey));
                            infosMap.put(BuyCardInfo.KEY_CARD_EXP_TIME, card.getDuedate());
                            cardInfos.add(infosMap);
                        }
                    }
                    RechargeOrder rechargeOrder = rechargeOrderMapper.selectByChannleOrderId(orderid);
                    String productId = rechargeOrder.getProductId();
                    String productName = rechargeOrder.getProductName();
                    String merchantId = rechargeOrder.getMerchantId();

                    List<PlatformCardInfo> platformCardInfos = cardInfos.stream().map(item -> {
                        PlatformCardInfo cardInfo = new PlatformCardInfo();
                        cardInfo.setCardNo(item.get(BuyCardInfo.KEY_CARD_PWD));
                        cardInfo.setCardPwd(item.get(BuyCardInfo.KEY_CARD_PWD));
                        cardInfo.setCustomerId(merchantId);
                        cardInfo.setOrderId(orderid);
                        cardInfo.setProductId(productId);
                        cardInfo.setProductName(productName);
                        cardInfo.setSupId(channel.getChannelId());
                        Date endDate = null;
                        try {
                            endDate = DateUtils.parseDate(item.get(BuyCardInfo.KEY_CARD_EXP_TIME), "yyyy-MM-dd hh:mm:ss");
                        } catch (Exception e) {
                        }

                        cardInfo.setExpireTime(endDate);
                        return cardInfo;
                    }).collect(Collectors.toList());
                    merchantCardServiceImpl.insertByBatch(platformCardInfos,channelOrder.getOrderId());
                    return new ProcessResult(ProcessResult.SUCCESS, "订单成功");
                } else if (StringUtils.equals("4", orderstatus) || StringUtils.equals("5", orderstatus)) {
                    return new ProcessResult(ProcessResult.FAIL, "订单失败");
                } else {
                    return new ProcessResult(ProcessResult.PROCESSING, "订单失败");
                }
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, "查询失败");
            }
        } catch (Exception e) {
            logger.error("易点提卡查询接口请求异常:{}", e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "查询失败");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("3", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("4", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "未知订单");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        JSONObject request = new JSONObject(true);
        JSONObject body = new JSONObject(true);
        JSONObject header = new JSONObject(true);
        JSONObject security = new JSONObject(true);
        CryptUtilImpl cryptUtil = new CryptUtilImpl();
//        String url = "http://test.api.yidianlife.cn/service/getbalance";
        String url = configJSONObject.getString("balanceQueryUrl");
//        String md5Key = "jbo1aHW9me";
        String md5Key = configJSONObject.getString("md5Key");
//        String clientid = "C010075";
        String clientid = configJSONObject.getString("clientId");
        Long reqtime = System.currentTimeMillis();
        String requestid = "BQ" + new Date();
//        String version = "1.0.0";
        String version = configJSONObject.getString("version");
        header.put("clientid", clientid);
        header.put("reqtime", reqtime);
        header.put("requestid", requestid);
        header.put("version", version);
        request.put("body", body);
        request.put("header", header);
        String signatureValue = DigestUtils.md5Hex(JSON.toJSONString(request) + md5Key).toUpperCase();
        security.put("signvalue", signatureValue);
        request.put("security", security);
        try {
            StringEntity stringEntity = new StringEntity(request.toString());
            logger.info("易点提卡查询余额接口请求信息:{}", JSONObject.toJSONString(request.toString()));
            String responseData = HttpClientUtils.invokePostString(url, stringEntity, "UTF-8", 5000);
            logger.info("易点提卡查询余额接口响应信息:{}", JSONObject.toJSONString(responseData));
            String code = JSONObject.parseObject(responseData).getString("code");
            if (StringUtils.equals("0", code)) {
                String data = JSONObject.parseObject(responseData).getString("data");
                String account = JSONObject.parseObject(data).getString("account");
                return new BigDecimal(account);
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            logger.error("易点提卡查询接口请求异常:{}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Test
    void test() {
        ChannelOrder channelOrder = new ChannelOrder();
        Channel channel = new Channel();
        channel.setConfigInfo("{queryUrl:\"https://api.quancangyun.cn/service/getorderbyoutno\",rechargeUrl:\"https://api.quancangyun.cn/service/orderasyc\",balanceQueryUrl:\"https://api.quancangyun.cn/service/getbalance\",md5Key:\"9dZiKDt4GT\",DESkey:\"QN98rj9tK9\",clientId:\"C030074\",telephone:\"18262227748\",version:\"1.0.0\"}");
        channelOrder.setChannelOrderId("cs202101121322");
        channelOrder.setProductId("1");
        channelOrder.setProductName("ceshishangpin");
        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
//        ProcessResult query = query(channel, channelOrder);
        System.out.println("z");
    }
}
