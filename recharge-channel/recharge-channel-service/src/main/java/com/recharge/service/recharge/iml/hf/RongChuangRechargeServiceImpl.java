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
import com.recharge.domain.ChannelOrderSupRelation;
import com.recharge.mapper.IChannelOrderSupRelationMapper;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.utils.RongXiangDESUtil;
import com.recharge.utils.RongXiangMD5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Administrator
 * @create 2021/2/26 17:34
 */
@Service
public class RongChuangRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IChannelOrderSupRelationMapper IChannelOrderSupRelationMapper;

    private List<String> errorCode = Arrays.asList("0003", "0004", "2002", "2003", "3001", "9001", "9002", "9003", "9004", "9008", "9009", "9010", "2999", "2998", "2997", "2995", "2994");

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String ChannelID = configJSONObject.getString("ChannelID");
        String User = configJSONObject.getString("User");
        String md5key = channel.getRemark2();
        String BusiType = "0101";
        String OrderTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String clientOrderId = channelOrder.getChannelOrderId();
        String substring = clientOrderId.substring(clientOrderId.length() - 5, clientOrderId.length());
        String OrderNo = ChannelID + "173" + BusiType + OrderTime + substring;
        ChannelOrderSupRelation newchannelOrder = new ChannelOrderSupRelation();
        newchannelOrder.setChannelOrderId(channelOrder.getChannelOrderId());
        newchannelOrder.setSupOrderId(OrderNo);
        newchannelOrder.setCreateTime(OrderTime);
        newchannelOrder.setSupId(channel.getChannelId());
        IChannelOrderSupRelationMapper.insertOne(newchannelOrder);
        String operator = huaFeiRechargeInfoBean.getOperator();
        String Operators;
//        String Operators = "1";
        if (StringUtils.equals(operator, "移动")) {
            Operators = "1";
        } else if (StringUtils.equals(operator, "联通")) {
            Operators = "2";
        } else if (StringUtils.equals(operator, "电信")) {
            Operators = "3";
        } else {
            return new ProcessResult(ProcessResult.FAIL, "运营商类型错误");
        }
        String PhoneNo = huaFeiRechargeInfoBean.getPhone();
//        String PhoneNo = "13849009312";
        String Cost = huaFeiRechargeInfoBean.getAmt().toString();
//        String Cost = "10";
        String RechargeType = "01";
        String RetURL = configJSONObject.getString("callBackUrl");

        Map<String, String> Data = new LinkedHashMap<>();
        Data.put("OrderNo", OrderNo);
        Data.put("Operators", Operators);
        Data.put("PhoneNo", PhoneNo);
        Data.put("Cost", Cost);
        Data.put("RechargeType", RechargeType);
        Data.put("OrderTime", OrderTime);
        Data.put("RetURL", RetURL);
        String dataString = JSONObject.toJSONString(Data);
        String dataDES = RongXiangDESUtil.encryptToDES(dataString, md5key);

        Map<String, String> map = new LinkedHashMap<>();
        map.put("ChannelID", ChannelID);
        map.put("User", User);
        map.put("BusiType", BusiType);
        map.put("Data", dataDES);
        String Sign = RongXiangMD5Util.MD5("ChannelID" + ChannelID
                + "User" + User
                + "BusiType" + BusiType
                + "Data" + dataDES
                + md5key);
        map.put("Sign", Sign);
        String requestString = JSONObject.toJSONString(map);
        try {
            logger.info("{}荣创,发送充值的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(requestString), "", "utf-8", 5000);
            logger.info("{}荣创,发送充值的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(responseBody));
            String RetCode = JSONObject.parseObject(responseBody).getString("RetCode");
            if (StringUtils.equals("1000", RetCode)) {
                String data = JSONObject.parseObject(responseBody).getString("Data");
                String decryptdata = RongXiangDESUtil.decrypt(data, md5key);
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else if (errorCode.contains(RetCode)) {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            } else {
                String RetMsg = JSONObject.parseObject(responseBody).getString("RetMsg");
                return new ProcessResult(ProcessResult.UNKOWN, "未知错误，错误码为" + RetCode + ",原因为" + RetMsg);
            }
//            String key = JSONObject.parseObject(decryptdata).getString("OrderNo");
        } catch (Exception e) {
            logger.info("荣创订单号: {} send error{}", rechargeOrderBean.getOrderId(), e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "未知错误="+e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        return new ProcessResult(ProcessResult.PROCESSING, "充值中");
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("2000", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (errorCode.contains(responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "可疑");
        }
    }


    @Test
    public String signIn(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String ChannelID = configJSONObject.getString("ChannelID");
        String User = configJSONObject.getString("User");
        String md5key = channel.getRemark2();
        String BusiType = "0001";
        String OrderTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String substring = OrderTime.substring(OrderTime.length() - 6, OrderTime.length() - 1);
        String OrderNo = ChannelID + "173" + BusiType + OrderTime + substring;
        String RequestStr = configJSONObject.getString("RequestStr");


        Map<String, String> Data = new LinkedHashMap<>();
        Data.put("OrderNo", OrderNo);
        Data.put("RequestStr", RequestStr);
        String dataString = JSONObject.toJSONString(Data);
        String dataDES = RongXiangDESUtil.encryptToDES(dataString, md5key);

        Map<String, String> map = new LinkedHashMap<>();
        map.put("ChannelID", ChannelID);
        map.put("User", User);
        map.put("BusiType", BusiType);
        map.put("Data", dataDES);
        String Sign = RongXiangMD5Util.MD5("ChannelID" + ChannelID
                + "User" + User
                + "BusiType" + BusiType
                + "Data" + dataDES
                + md5key);
        map.put("Sign", Sign);
        String requestString = JSONObject.toJSONString(map);
        try {
            logger.info("荣创,刷新token发送的参数:{}", JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(requestString), "", "utf-8", 5000);
            logger.info("荣创,刷新token接收的参数:{}", JSON.toJSONString(responseBody));
            String RetCode = JSONObject.parseObject(responseBody).getString("RetCode");
            if (StringUtils.equals("0000", RetCode)) {
                String data = JSONObject.parseObject(responseBody).getString("Data");
                String decryptdata = RongXiangDESUtil.decrypt(data, md5key);
                JSONObject jsonObject = JSONObject.parseObject(decryptdata.trim());
                String Key = jsonObject.getString("Key");
                System.out.println("z");
                return Key;
            } else if (errorCode.contains(RetCode)) {
                return "fail";
            }
        } catch (Exception e) {
        	logger.info("荣创订单{},刷新token错误:{}",ChannelID,e.getMessage());
            return "fail";
        }
        return "fail";
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String ChannelID = configJSONObject.getString("ChannelID");
        String User = configJSONObject.getString("User");
        String BusiType = "0303";
        String md5key = channel.getRemark2();
        String OrderTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String substring = OrderTime.substring(OrderTime.length() - 7, OrderTime.length() - 2);
        String OrderNo = ChannelID + "173" + BusiType + OrderTime + substring;
        Map<String, String> Data = new LinkedHashMap<>();
        Data.put("OrderNo", OrderNo);
        String dataString = JSONObject.toJSONString(Data);
        String dataDES = RongXiangDESUtil.encryptToDES(dataString, md5key);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("ChannelID", ChannelID);
        map.put("User", User);
        map.put("BusiType", BusiType);
        map.put("Data", dataDES);
        String Sign = RongXiangMD5Util.MD5("ChannelID" + ChannelID
                + "User" + User
                + "BusiType" + BusiType
                + "Data" + dataDES
                + md5key);
        map.put("Sign", Sign);
        String requestString = JSONObject.toJSONString(map);
        try {
            logger.info("荣创,查询余额接口请求的参数:{}", JSON.toJSONString(map));
            String responseBody = HttpClientUtils.invokeJsonString(url, new StringEntity(requestString), "", "utf-8", 5000);
            logger.info("荣创,查询余额接口响应的参数:{}", JSON.toJSONString(responseBody));
            String RetCode = JSONObject.parseObject(responseBody).getString("RetCode");
            if (StringUtils.equals("2000", RetCode)) {
                String data = JSONObject.parseObject(responseBody).getString("Data");
                String decryptdata = RongXiangDESUtil.decrypt(data, md5key);
                JSONObject jsonObject = JSONObject.parseObject(decryptdata.trim());
                String Balance = jsonObject.getString("Balance");
                return new BigDecimal(Balance).divide(new BigDecimal(100),3,BigDecimal.ROUND_HALF_UP);
            }else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Test
    void test() {
        ChannelOrder channelOrder = new ChannelOrder();
        Channel channel = new Channel();
//        channelOrder.setChannelOrderId("cs202101211446");
//        channelOrder.setChannelOrderId("cs202101211550");
        channelOrder.setChannelOrderId("cs202103011519");
//        channel.setRemark2("3132333435363738");
//        channel.setRemark2("22715B7C5A51583F");
//        channel.setRemark2("397634236925343E");
        channel.setRemark2("312F326C76355877");
        channel.setConfigInfo("{rechargeUrl:\"http://117.27.146.62:20098\",ChannelID:\"1406\",User:\"yjwl\",md5key:\"3132333435363738\",callBackUrl:\"http://115.28.88.114:8082/rongChuang/callBack\",RequestStr:\"HUxI8h0a\"}");
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
//        String s = signIn(channel);
        BigDecimal bigDecimal = balanceQuery(channel);
//        String s = signIn(channel);
        System.out.println("z");
    }
}
