package com.recharge.service.recharge.iml.qb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.GameRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
import com.recharge.domain.XingChen;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020/12/18 16:19
 */
@Service
public class XingChenQBRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        GameRechargeInfoBean gameRechargeInfoBean = (GameRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(GameRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        //获取接口key
        String key = configJSONObject.getString("key");
        //获取备注key
        String remarkKey = configJSONObject.getString("remarkKey");
        //获取URL
        String url = configJSONObject.getString("rechargeUrl");
        //获取用户编号
        String merchantID = configJSONObject.getString("merchantID");
        //获取我们自己的订单号(PS)
        String merchantOrderID = channelOrder.getChannelOrderId();
        //必传 商品编号
        logger.info("xingchengQB recharge productname="+rechargeOrderBean.getProductName());
        ProductRelation productRelation = queryChannelProductId(rechargeOrderBean.getProductName(), "100115");
        String productID="";
        if (productRelation == null) {
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        }else {
            productID = productRelation.getChannelProductId();
        }
        //必传 充值数量
        String buyamount = gameRechargeInfoBean.getNumber().toString();
        //必传 充值笔数  注意：只能填1
        String multiple = "1";
        //必传 充值号码
        String targetAccount = gameRechargeInfoBean.getGameId();
        //必传 区名      可传空值
        String areaName = "";
        //必传 服名      可传空值
        String serverName = "";
        //必传 用户区域  可传空值
        String customerRegion = "";
        //必传 传入IP    可传空值
        String afferentIP = "";
        //必传 异步通知  改用绑定不用传值
        String responseUrl = configJSONObject.getString("callbackUrl");
        //必传 md5(merchantOrderID+备注key)小写    该备注key在开户时，我方会发给你与接口key不同
        String Remarks = DigestUtils.md5Hex(merchantID + remarkKey).toLowerCase();
        //必传 md5(merchantID+merchantOrderID ＋ productID+接口key)小写
        String sign = DigestUtils.md5Hex(merchantID + merchantOrderID + productID + key).toLowerCase();
        Map<String, String> map = new HashMap<>();
        map.put("merchantID", merchantID);
        map.put("merchantOrderID", merchantOrderID);
        map.put("productID", productID);
        map.put("buyamount", buyamount);
        map.put("multiple", multiple);
        map.put("targetAccount", targetAccount);
        map.put("areaName", areaName);
        map.put("serverName", serverName);
        map.put("customerRegion", customerRegion);
        map.put("afferentIP", afferentIP);
        map.put("responseUrl", responseUrl);
        map.put("Remarks", Remarks);
        map.put("sign", sign);
        try {
            logger.info("XingChengQB send recharge request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("XingChengQB send recharge response :{}", JSONObject.toJSONString(responseBody));
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Map maps = (Map) JSON.parse(JSON.toJSONString(root));
            Map map2 = (Map) JSON.parse(String.valueOf(maps.get("data")));
            String code = String.valueOf(map2.get("trade_state"));
            String msg = String.valueOf(map2.get("msg"));
            if (StringUtils.equals(code, "1")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (StringUtils.equals(code, "601")) {
                return new ProcessResult(ProcessResult.UNKOWN, "外部单号已存在");
            } else if (StringUtils.equals(code, "801")) {
                return new ProcessResult(ProcessResult.UNKOWN, "数据已操作");
            } else if (StringUtils.equals(code, "821")) {
                return new ProcessResult(ProcessResult.UNKOWN, "数据已存在");
            } else if (StringUtils.equals(code, "850")) {
                return new ProcessResult(ProcessResult.UNKOWN, "订单提交失败");
            } else if (StringUtils.equals(code, "851")) {
                return new ProcessResult(ProcessResult.UNKOWN, "订单提交可疑");
            } else if (StringUtils.equals(code, "999")) {
                return new ProcessResult(ProcessResult.UNKOWN, "系统异常");
            } else if (StringUtils.equals(code, "703")) {
                return new ProcessResult(ProcessResult.UNKOWN, "订单不存在");
            } else {
                return new ProcessResult(ProcessResult.FAIL, msg);
            }
        } catch (Exception e) {
            logger.error("{}send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑原因="+e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        //获取用户编号
        String merchantID = configJSONObject.getString("merchantID");
        //获取key
        String key = configJSONObject.getString("key");
        //获取查询URL
        String url = configJSONObject.getString("queryUrl");
        //获取外部订单号（PS）
        String merchantOrderID = channelOrder.getChannelOrderId();
        //获取内部订单号
        String serverOrderID = "";
        String sign = DigestUtils.md5Hex(merchantID + merchantOrderID + key).toLowerCase();
        Map<String, String> map = new HashMap<>();
        map.put("merchantID", merchantID);
        map.put("merchantOrderID", merchantOrderID);
        map.put("serverOrderID", serverOrderID);
        map.put("sign", sign);
        try {
            logger.info("XingChengQB send query request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("XingChengQB send query response :{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Map maps = (Map) JSON.parse(JSON.toJSONString(root));
            Map map2 = (Map) JSON.parse(String.valueOf(maps.get("data")));
            String trade_state = String.valueOf(map2.get("trade_state"));
            String msg = String.valueOf(map2.get("msg"));
            String rt_json = String.valueOf(map2.get("rt_json"));
            if (trade_state.equals("1")) {
                Map map1 = (Map) JSON.parse(rt_json);
                String list = String.valueOf(map1.get("list"));
                List<XingChen> list1 = JSONObject.parseArray(list, XingChen.class);
                String a = String.valueOf(list1.get(0).getOrder_state());
                if (a.equals("2")) {
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (a.equals("1")) {
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                } else if (a.equals("3")) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else if (a.equals("0")) {
                    return new ProcessResult(ProcessResult.UNKOWN, "查询可疑");
                } else {
                    return new ProcessResult(ProcessResult.UNKOWN, "查询可疑");
                }
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, msg);
            }
        } catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(), "2")) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "3")) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "1")) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "0")) {
            return new ProcessResult(ProcessResult.PROCESSING, "未处理");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        //获取查询余额URL
        String url = configJSONObject.getString("queryBalanceUrl");
        //获取用户编号
        String merchantID = configJSONObject.getString("merchantID");
        //获取接口key
        String key = configJSONObject.getString("key");
        String sign = DigestUtils.md5Hex(merchantID + key).toLowerCase();
        Map<String, String> map = new HashMap<>();
        map.put("merchantID", merchantID);
        map.put("sign", sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Map maps = (Map) JSON.parse(JSON.toJSONString(root));
            Map map2 = (Map) JSON.parse(String.valueOf(maps.get("data")));
            Map map3 = (Map) JSON.parse(String.valueOf(map2.get("rt_json")));
            String balance = String.valueOf(map3.get("balance"));
            return new BigDecimal(balance);
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    @Test
    public void test() {
        XingChenQBRechargeServiceImpl xingChenQBRechargeService = new XingChenQBRechargeServiceImpl();
        ChannelOrder channelOrder = new ChannelOrder();
        Channel channel = new Channel();
        channel.setConfigInfo("{queryBalanceUrl:\"http://118.31.164.94/webservice/WebService1.asmx/QueryMerchant\",merchantID:\"ZM_mid0000000000000000043\",rechargeUrl:\"http://118.31.164.94/webservice/WebService1.asmx/pay\",queryUrl:\"http://118.31.164.94/webservice/WebService1.asmx/queryOrder\",key:\"009f4a55897fdfc3c12356a52ddf18922e4b593d4b672c221b4937fbab671a55ea8ee2605d25718c5e6c0fa16810ae433fba67930a36cdf69df95b7dc0e4dd2f\",remarkKey:\"123.55d63ww\",callbackUrl:\"http://139.129.85.83:8082/xingChenQB/callBack\"}");
        channelOrder.setChannelOrderId("cs202012181837");
//        BigDecimal bigDecimal = xingChenQBRechargeService.balanceQuery(channel);
//        ProcessResult recharge = xingChenQBRechargeService.recharge(channel, channelOrder, new RechargeOrderBean());
        ProcessResult query = xingChenQBRechargeService.query(channel, channelOrder);
        System.out.println("zzz");
    }
}
