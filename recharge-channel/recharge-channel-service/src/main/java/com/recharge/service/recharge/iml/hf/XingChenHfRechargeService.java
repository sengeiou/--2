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
import com.recharge.domain.XingChen;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020/10/30 15:14
 */
@Service
public class XingChenHfRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
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
        String merchantOrderID = channelOrder.getOrderId();
        ProductRelation productRelation = new ProductRelation();
        if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "联通")) {
            productRelation = queryChannelProductId("全国联通话费", "100097");
        }else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "移动")){
            productRelation = queryChannelProductId("全国移动话费", "100097");
        }else if (StringUtils.equals(huaFeiRechargeInfoBean.getOperator(), "电信")){
            productRelation = queryChannelProductId("全国电信话费", "100097");
        }else {
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        }
        //获取商品编号
        String productID = productRelation.getChannelProductId();
        //获取充值数量
        String buyamount = huaFeiRechargeInfoBean.getAmt().toString();
        //获取充值笔数
        String multiple = "1";
        //获取充值号码
        String targetAccount = huaFeiRechargeInfoBean.getPhone();
        //获取区名（可为空）
        String areaName = "";
        //获取服名（可为空）
        String serverName = "";
        //获取用户区域（可为空）
        String customerRegion = "";
        //获取传入IP（可为空）
        String afferentIP = "";
        //获取异步通知
        String responseUrl = configJSONObject.getString("hfcallbackUrl");
        //获取md5(merchantOrderID+备注key)小写
        String Remarks = DigestUtils.md5Hex(merchantID + remarkKey).toLowerCase();
        //获取md5(merchantID+merchantOrderID ＋ productID+接口key)小写
        String sign = DigestUtils.md5Hex(merchantID + merchantOrderID + productID + key).toLowerCase();
        //创建一个MAP集合，用来储存相关参数
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
            logger.info("XingChengHF send recharge request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("XingChengHF send recharge response :{}", JSONObject.toJSONString(responseBody));
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
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
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
        String merchantOrderid =channelOrder.getOrderId();
        //获取内部订单号
        String serverOrderID = "";
        String sign = DigestUtils.md5Hex(merchantID + merchantOrderid + key).toLowerCase();
        Map<String, String> map = new HashMap<>();
        map.put("merchantID", merchantID);
        map.put("merchantOrderid", merchantOrderid);
        map.put("serverOrderID", serverOrderID);
        map.put("sign", sign);
        try {
            logger.info("XingChengHf send query request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("XingChengHf send query response :{}", responseBody);
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
                } else if (a.equals("0")){
                    return new ProcessResult(ProcessResult.UNKOWN, "查询可疑");
                }else {
                    return new ProcessResult(ProcessResult.UNKOWN, "查询可疑");
                }
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, msg);
            }
        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(), "2")) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        }else if (StringUtils.equals(responseOrder.getResponseCode(), "3")) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }else if (StringUtils.equals(responseOrder.getResponseCode(), "1")) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        }else if (StringUtils.equals(responseOrder.getResponseCode(), "0")) {
            return new ProcessResult(ProcessResult.UNKOWN, "未处理");
        }else {
            return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        //获取查询余额URL
        String balanceUrl = configJSONObject.getString("queryBalanceUrl");
        //获取用户编号
        String merchantID = configJSONObject.getString("merchantID");
        //获取接口key
        String key = configJSONObject.getString("key");
        //生成签名
        String sign = DigestUtils.md5Hex(merchantID + key).toLowerCase();
        //创建MAP，用来储存相关参数
        Map<String, String> map = new HashMap<>();
        map.put("merchantID", merchantID);
        map.put("sign", sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(balanceUrl, map, "utf-8", 5000);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Map maps = (Map) JSON.parse(JSON.toJSONString(root));
            Map map2 = (Map) JSON.parse(String.valueOf(maps.get("data")));
            Map map3 = (Map) JSON.parse(String.valueOf(map2.get("rt_json")));
            String balance = String.valueOf(map3.get("balance"));
            return new BigDecimal(balance);
        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }
    }



}
