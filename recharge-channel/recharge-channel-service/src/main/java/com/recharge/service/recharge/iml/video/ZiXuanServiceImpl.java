package com.recharge.service.recharge.iml.video;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Administrator
 * @create 2021/2/1 14:25
 */
@Service
public class ZiXuanServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());


    private String channelId = "";

    private List<String> rechargeErrorCode = Arrays.asList("1000", "1001", "1003", "1004", "2001", "2002", "2003", "2020", "2021", "1006", "2030");

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        VideoRechargeInfoBean videoRechargeInfoBean = (VideoRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(VideoRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String rechargeUrl = configJSONObject.getString("rechargeUrl");
        String szAgentId = configJSONObject.getString("szAgentId");
        String szKey = configJSONObject.getString("szKey");
        String szNotifyUrl = configJSONObject.getString("szNotifyUrl");
        String szOrderId = channelOrder.getChannelOrderId();
        String szPhoneNum = videoRechargeInfoBean.getAccount();
        String nProductClass = "1";
        String nProductType = "1";
        ProductRelation productRelation = queryChannelProductId(channelOrder.getProductName(), channelId);
        String[] split = productRelation.getChannelProductId().split("\\|");
        String szProductId = split[0];
        String nMoney = split[1];
        String nSortType = split[2];
        String szTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String szVerifyString = DigestUtils.md5Hex("szAgentId=" + szAgentId + "&szOrderId=" + szOrderId + "&szPhoneNum=" + szPhoneNum +
                "&nMoney=" + nMoney + "&nSortType=" + nSortType + "&nProductClass=" + nProductClass + "&nProductType=" + nProductType
                + "&szTimeStamp=" + szTimeStamp + "&szKey=" + szKey).toLowerCase();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("szAgentId", szAgentId);
        map.put("szOrderId", szOrderId);
        map.put("szPhoneNum", szPhoneNum);
        map.put("nMoney", nMoney);
        map.put("nSortType", nSortType);
        map.put("nProductClass", nProductClass);
        map.put("nProductType", nProductType);
        map.put("szTimeStamp", szTimeStamp);
        map.put("szProductId", szProductId);
        map.put("szVerifyString", szVerifyString);
        map.put("szNotifyUrl", szNotifyUrl);
        try {
            logger.info("子轩下单接口请求的参数:订单号={}，参数={}", szOrderId, JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(rechargeUrl, map, "utf-8", 5000);
            logger.info("子轩下单接口接收的参数:订单号={}，参数={}", szOrderId, JSONObject.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("nRtn");
            if (StringUtils.equals(code, "0")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                if (rechargeErrorCode.contains(code)) {
                    return new ProcessResult(ProcessResult.FAIL, "提交失败");
                } else {
                    return new ProcessResult(ProcessResult.UNKOWN, "提交可疑，找供货商核实");
                }
            }
        } catch (Exception e) {
            logger.info("子轩下单接口出错的参数:订单号={}，报错信息={}", szOrderId, e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交出错，找供货商核实");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryUrl = configJSONObject.getString("queryUrl");
        String szAgentId = configJSONObject.getString("szAgentId");
        String szKey = configJSONObject.getString("szKey");
        String szOrderId = channelOrder.getChannelOrderId();
        String szVerifyString = DigestUtils.md5Hex("szAgentId=" + szAgentId + "&szOrderId=" + szOrderId + "&szKey=" + szKey).toLowerCase();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("szAgentId", szAgentId);
        map.put("szOrderId", szOrderId);
        map.put("szVerifyString", szVerifyString);
        try {
            logger.info("子轩查询接口请求的参数:订单号={}，参数={}", szOrderId, JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, map, "utf-8", 5000);
            logger.info("子轩查询接口接收的参数:订单号={}，参数={}", szOrderId, JSONObject.toJSONString(responseBody));
            String code = JSONObject.parseObject(responseBody).getString("nRtn");
            if (StringUtils.equals(code, "5012")) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals(code, "5011") || StringUtils.equals(code, "5019")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            }else if(StringUtils.equals(code, "5013")){
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            }else {
                return new ProcessResult(ProcessResult.UNKOWN, "查询可疑，状态码为："+code);
            }
        } catch (Exception e) {
            logger.info("子轩查询接口出错的参数:订单号={}，参数={}", szOrderId, JSONObject.toJSONString(e));
            return new ProcessResult(ProcessResult.UNKOWN, "查询出错："+e);
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
        } else if (StringUtils.equals("3", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "提交失败");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String balanceUrl = configJSONObject.getString("balanceUrl");
        String szAgentId = configJSONObject.getString("szAgentId");
        String szKey = configJSONObject.getString("szKey");
        String szVerifyString = DigestUtils.md5Hex("szAgentId=" + szAgentId + "&szKey=" + szKey).toLowerCase();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("szAgentId", szAgentId);
        map.put("szVerifyString", szVerifyString);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(balanceUrl, map, "utf-8", 5000);
            String code = JSONObject.parseObject(responseBody).getString("nRtn");
            if (StringUtils.equals(code, "0")) {
                String balance = JSONObject.parseObject(responseBody).getString("fBalance");
                return new BigDecimal(balance);
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Test
    void test() {
        Channel channel = new Channel();
        channel.setConfigInfo("{\"rechargeUrl\":\"http://47.98.194.249:10186/plat/api/old/submitorder\",\"szAgentId\":\"200006\",\"szKey\":\"b0ed645d379c4ed4a0d29364fb16707d\",\"szNotifyUrl\":\"http://139.129.85.83:8082/ziXuan/callBack\",\"queryUrl\":\"http://47.98.194.249:10186/plat/api/old/queryorder\",\"balanceUrl\":\"http://47.98.194.249:10186/plat/api/old/queryBalance\",\"szNotifyUrl\":\"http://139.129.85.83:8082/ziXuan/callBack\"}");
        ChannelOrder channelOrder = new ChannelOrder();
//        BigDecimal bigDecimal = balanceQuery(channel);
        channelOrder.setChannelOrderId("cs20210202105011");
//        ProcessResult recharge = recharge(new Channel(), new ChannelOrder(), new RechargeOrderBean());
        ProcessResult query = query(channel, channelOrder);
    }

    @Test
    void test1() {
        String s = "20210434|28|111";
        String[] split = s.split("\\|");
        System.out.println(split[0]);
    }

}
