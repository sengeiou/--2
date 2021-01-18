package com.recharge.service.recharge.iml.jyk;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.JykRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
import com.recharge.domain.XingChen;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author user
 * @create 2020/8/17 17:13
 */
@Service
public class XingChenJykRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String merchantID = configJSONObject.getString("merchantID");
        String remarkKey = configJSONObject.getString("remarkKey");
        String key = configJSONObject.getString("key");
        String afferentIP = configJSONObject.getString("ip");
        String url = configJSONObject.getString("rechargeUrl");
        //外部订单号PS
        String merchantOrderID = channelOrder.getChannelOrderId();
        ProductRelation productRelation = queryChannelProductId(rechargeOrderBean.getProductName(), "100080");
        //产品ID
        String productID = productRelation.getChannelProductId();
        //充值数量
        String buyamount = jykRechargeInfoBean.getAmt();
        //充值笔数  填1
        String multiple = "1";
        //充值账号
        String targetAccount = jykRechargeInfoBean.getAccount();
        //区名 (传空)
        String areaName = configJSONObject.getString("phone");
        //服名 (传空)
        String serverName = "";
        //用户区域 (传空)
        String customerRegion = "";
        //ip (可传空)
        //回调地址(绑定后即可不传)
        String responseUrl = "";
        //md5(merchantOrderID+备注key)小写
        String Remarks = DigestUtils.md5Hex(merchantOrderID + remarkKey).toLowerCase();
        //md5(merchantID+merchantOrderID ＋ productID+接口key)小写
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
            logger.info("XingChenJyk send recharge request requestUrl:{}", map);
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("XingChenJyk send recharge response :{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Map maps = (Map) JSON.parse(JSON.toJSONString(root));
            Map map2 = (Map) JSON.parse(String.valueOf(maps.get("data")));
            String code = String.valueOf(map2.get("trade_state"));
            String msg = String.valueOf(map2.get("msg"));
            if (code.equals("1")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, msg);
            }
        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }

    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String merchantID = configJSONObject.getString("merchantID");
        String key = configJSONObject.getString("key");
        String url = configJSONObject.getString("queryUrl");
        String merchantOrderid = channelOrder.getChannelOrderId();
        String serverOrderID = "";

        String sign = DigestUtils.md5Hex(merchantID + merchantOrderid + key).toLowerCase();
        Map<String, String> map = new HashMap<>();
        map.put("merchantID", merchantID);
        map.put("merchantOrderid", merchantOrderid);
        map.put("serverOrderID", serverOrderID);
        map.put("sign",sign);

        try {
            logger.info("发送充值的参数:{}", JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("收到充值的响应:{}", responseBody);

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
                } else if (a.equals("1") || a.equals("0")) {
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                } else if (a.equals("3")) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else {
                    return new ProcessResult(ProcessResult.UNKOWN, "查询可疑");
                }
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, msg);
            }

        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", channelOrder.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }

    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(), "成功")) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals(responseOrder.getResponseCode(), "失败")) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }
        return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
    }


    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String merchantID = configJSONObject.getString("merchantID");
        String key = configJSONObject.getString("key");
        String queryBalanceUrl = configJSONObject.getString("queryBalanceUrl");
        String sign = DigestUtils.md5Hex(merchantID + key).toLowerCase();
        Map<String, String> map = new HashMap<>();
        map.put("merchantID", merchantID);
        map.put("sign", sign);
        try {

            String responseBody = HttpClientUtils.invokePostHttp(queryBalanceUrl, map, "utf-8", 5000);
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
