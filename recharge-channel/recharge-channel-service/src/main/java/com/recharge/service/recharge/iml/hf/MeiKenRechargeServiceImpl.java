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

/**
 * @author user
 * @create 2020/8/19 22:12
 */
@Service
public class MeiKenRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        //用户编号,代理方用户编号(充值平台方提供)
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");
        String url = configJSONObject.getString("rechargeUrl");
        //商品编号(充值平台方提供)
        ProductRelation productRelation = queryChannelProductId("全国" + huaFeiRechargeInfoBean.getOperator() + "话费" + huaFeiRechargeInfoBean.getAmt() + "元", "100081");
        String itemId = productRelation.getChannelProductId();
        //商品面值(单位厘:1元=1000厘，用于验证上传面值是否跟系统itemId的面值一致)
        String mianzhi = String.valueOf(huaFeiRechargeInfoBean.getAmt().multiply(new BigDecimal("1000")));
        String checkItemFacePrice = mianzhi;
        //充值账号
        String uid = huaFeiRechargeInfoBean.getPhone();
        //合作方商户系统的流水号，全局唯一（不能重复使用，使用同一单号下单造成重复下单这边不负责），即代理方的订单号
        String serialno = channelOrder.getChannelOrderId();
        //合作方交易时间(也可以是订单创建时间，格式为：yyyyMMddHHmmss)
        String spordertime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String dtCreate = spordertime;
        //购买数量,不传默认1。话费、流量只能传1或者不传
        String amt = "1";
        //商品销售价格，单位厘；1元=1000厘（对于代理方而言，就是成本价格）
        String itemPrice = "";
        //备用参数1 ,中石油-传手机号
        String ext1 = "";
        //备用参数2 ,中石油-身份证号
        String ext2 = "";
        //备用参数3 ,中石油-身份证姓名 ；视频卡直充-传IP
        String ext3 = "";
        //签名(MD5)32位小写， sign=MD5(amt+checkItemFacePrice+dtCreate+ext1+ext2+ext3+itemId+itemPrice+serialno+uid+userId+privatekey); 有传有签名，没传不参与签名，比如不带amt，amt则不要参与签名计算
        String sign = DigestUtils.md5Hex(amt + checkItemFacePrice + dtCreate + ext1 + ext2 + ext3 + itemId + itemPrice + serialno + uid + userId + key).toLowerCase();

        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("itemId", itemId);
        map.put("checkItemFacePrice", checkItemFacePrice);
        map.put("uid", uid);
        map.put("serialno", serialno);
        map.put("dtCreate", dtCreate);
        map.put("sign", sign);
        map.put("amt", amt);
        map.put("itemPrice", itemPrice);
        map.put("ext1", ext1);
        map.put("ext2", ext2);
        map.put("ext3", ext3);

        try {
            System.out.println(JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            System.out.println(JSON.toJSONString(responseBody));
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String code = root.elementText("code");
            String desc = root.elementText("desc");
            if (code.equals("00")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (code.equals("23") || code.equals("31") || code.equals("50")) {
                return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
            } else {
                return new ProcessResult(ProcessResult.FAIL, desc);
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
        String userId = configJSONObject.getString("userId");
        String serialno = channelOrder.getChannelOrderId();
        String key = configJSONObject.getString("key");
        String sign = DigestUtils.md5Hex(serialno + userId + key).toLowerCase();
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("serialno", serialno);
        map.put("sign", sign);
        try {
            System.out.println(JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            System.out.println(JSON.toJSONString(responseBody));
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String code = root.elementText("code");
            String desc = root.elementText("desc");
            String statusDesc = root.elementText("statusDesc");
            System.out.println(code);
            if (code.equals("00")) {
                String dataStatus = root.element("data").elementText("status");
                String outOrderNo = root.element("data").elementText("outOrderNo");
                if (dataStatus.equals("0") || dataStatus.equals("1") || dataStatus.equals("4")) {
                    return new ProcessResult(ProcessResult.PROCESSING, "处理中，继续查询");
                } else if (dataStatus.equals("2")) {
                    channelOrder.setOutChannelOrderId(outOrderNo);
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else if (dataStatus.equals("3")) {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                } else {
                    return new ProcessResult(ProcessResult.UNKOWN, statusDesc);
                }
            } else {
                return new ProcessResult(ProcessResult.UNKOWN, desc);
            }

        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }

    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {

        if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("3", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "未知返回码");
        }
    }


    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("balancequeryUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");
        String sign = DigestUtils.md5Hex(userId + key).toLowerCase();
        Map<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("sign", sign);
        try {
            logger.info("send queryBalance request params:{}", JSONObject.toJSONString(map));
            String responseBody = HttpClientUtils.invokePostHttp(url, map, "utf-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String balance = root.elementText("balance");
            return new BigDecimal(balance).divide(new BigDecimal("1000"));
        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }


    }

}
