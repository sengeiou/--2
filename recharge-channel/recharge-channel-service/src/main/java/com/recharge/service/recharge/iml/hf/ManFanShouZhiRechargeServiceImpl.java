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

@Service
public class ManFanShouZhiRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        ManFanShouZhiRechargeServiceImpl jiaoFei100Service = new ManFanShouZhiRechargeServiceImpl();

        Channel channel = new Channel();
        channel.setConfigInfo("{userId:\"673\",url:\"http://47.93.197.171:8760/unicomAync/buy.do\"," +
                "pwd:\"jSCK7rF4GSmWnwcG4mwm7eiNcD6CW4TS\"," +
                "key:\"efebb546d1aa1be88236e4c88bac73f29570f3371d82b6de36659adf109a4837\"," +
                "balanceQueryUrl:\"http://47.93.197.171:8760/unicomAync/queryBalance.do\"," +
                "queryUrl:\"http://47.93.197.171:8760/unicomAync/queryBizOrder.do\"}");
        ChannelOrder channelOrder = new ChannelOrder();
        channelOrder.setChannelOrderId("JF201703221321001");
        RechargeOrderBean rechargeOrderBean = new RechargeOrderBean();
        rechargeOrderBean.setOrderId("R00000000003");
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = new HuaFeiRechargeInfoBean();
        huaFeiRechargeInfoBean.setAmt(new BigDecimal(30));
        huaFeiRechargeInfoBean.setPhone("15715141438");
        rechargeOrderBean.setRechargeInfoBeanObj(huaFeiRechargeInfoBean);
//        BigDecimal balance = jiaoFei100Service.balanceQuery(channel );
//        System.out.println(balance);
        ProcessResult result = jiaoFei100Service.query(channel, channelOrder);
        System.out.println(result.getMsg());


//        ProcessResult result = jiaoFei100Service.recharge(channel ,channelOrder ,rechargeOrderBean);
//        System.out.println(result.getMsg());
    }


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

        try {
            ProductRelation productRelation = queryChannelProductId("全国"+huaFeiRechargeInfoBean.getOperator()+huaFeiRechargeInfoBean.getAmt() , "100045");
            if(productRelation == null){
                return new ProcessResult(ProcessResult.FAIL,"提交失败");
            }
            String productId = productRelation.getChannelProductId();
            String dtCreate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            String requestUrl = url + "?sign="
                    + DigestUtils.md5Hex(huaFeiRechargeInfoBean.getAmt().multiply(new BigDecimal(1000)) + dtCreate + productId + channelOrder.getChannelOrderId() + huaFeiRechargeInfoBean.getPhone() + userId + key)
                    + "&checkItemFacePrice=" + huaFeiRechargeInfoBean.getAmt().multiply(new BigDecimal(1000))
                    + "&uid=" + huaFeiRechargeInfoBean.getPhone()
                    + "&dtCreate=" + dtCreate
                    + "&userId=" + userId
                    + "&itemId=" + productId
                    + "&serialno=" + channelOrder.getChannelOrderId();
            logger.info("send recharge request url:{}", requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String code = root.elementText("code");
            String status = root.elementText("status");
            if (StringUtils.equals("00", code)) {
                if (StringUtils.equals("success", status)) {
                    return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
                } else {
                    return new ProcessResult(ProcessResult.FAIL, "提交失败");
                }

            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.SUCCESS, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String queryUrl = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

        try {

            String requestUrl = queryUrl + "?sign="
                    + DigestUtils.md5Hex(channelOrder.getChannelOrderId() + userId + key)
                    + "&userId=" + userId
                    + "&serialno=" + channelOrder.getChannelOrderId();
            logger.info("send query request url:{}", requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String code = root.elementText("code");
            String status = root.elementText("status");
            if (StringUtils.equals("00", code)) {
                if (StringUtils.equals("success", status)) {
                    String dataStatus = root.element("data").elementText("status");
                    if (StringUtils.equals(dataStatus, "2")) {
                        return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                    } else if (StringUtils.equals(dataStatus, "3")) {
                        return new ProcessResult(ProcessResult.FAIL, "充值失败");
                    } else {
                        return new ProcessResult(ProcessResult.PROCESSING, "处理中，继续查询");
                    }
                } else {
                    return new ProcessResult(ProcessResult.PROCESSING, "查询状态失败，继续查询");
                }

            }  else if (StringUtils.equals("22", code)){
                return new ProcessResult(ProcessResult.FAIL,"充值失败");
            }else {
                return new ProcessResult(ProcessResult.PROCESSING, "查询状态失败，继续查询");
            }
        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "查询状态失败，继续查询");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(), "2")) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "3")) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "状态可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String balanceQueryUrl = configJSONObject.getString("balanceQueryUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

        try {

            String requestUrl = balanceQueryUrl
                    + "?userId=" + userId
                    + "&sign=" + DigestUtils.md5Hex(userId + key);
            ;
            logger.info("send recharge request url:{}", requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String code = root.elementText("code");
            String status = root.elementText("status");
            if (StringUtils.equals("00", code)) {
                if (StringUtils.equals("success", status)) {
                    return new BigDecimal(root.elementText("balance")).divide(new BigDecimal(1000));
                } else {
                    return new BigDecimal("0");
                }
            } else {
                return new BigDecimal("0");
            }
        } catch (Exception e) {
            logger.error("balance query error", e);
            return new BigDecimal("0");
        }
    }
}
