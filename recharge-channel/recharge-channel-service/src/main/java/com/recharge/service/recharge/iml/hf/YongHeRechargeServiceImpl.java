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
 * @create 2021/3/10 15:10
 */
@Service
public class YongHeRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());


    private List<String> rechargeErrorCode = Arrays.asList("1000", "1001", "1003", "1004", "2001", "2002",
            "2003", "2020", "2021", "1006", "2030", "10018", "10019");
    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String szAgentId = configJSONObject.getString("szAgentId");
        String szKey = configJSONObject.getString("szKey");
        String szOrderId = channelOrder.getChannelOrderId();
//        String szPhoneNum = "17602533368";
        String szPhoneNum = huaFeiRechargeInfoBean.getPhone();
//        String nMoney = "30";
        String nMoney = huaFeiRechargeInfoBean.getAmt().toString();
        String nSortType;
//        String nSortType="2";
        if (StringUtils.equals("移动",huaFeiRechargeInfoBean.getOperator())){
            nSortType="1";
        }else if(StringUtils.equals("联通",huaFeiRechargeInfoBean.getOperator())){
            nSortType="2";
        }else if(StringUtils.equals("电信",huaFeiRechargeInfoBean.getOperator())){
            nSortType="3";
        }else {
            nSortType="";
            return new ProcessResult(ProcessResult.FAIL, "运营商编码出错");
        }
        String nProductClass = "1";
        String nProductType = "1";
        String szTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String szNotifyUrl = configJSONObject.getString("notify_url");
        String szFormat = "JSON";
        StringBuffer sb = new StringBuffer();
        sb.append("szAgentId=").append(szAgentId).append("&szOrderId=").append(szOrderId)
                .append("&szPhoneNum=").append(szPhoneNum).append("&nMoney=").append(nMoney)
                .append("&nSortType=").append(nSortType).append("&nProductClass=").append(nProductClass)
                .append("&nProductType=").append(nProductType).append("&szTimeStamp=").append(szTimeStamp)
                .append("&szKey=").append(szKey);
        String szVerifyString= DigestUtils.md5Hex(sb.toString()).toLowerCase();
        LinkedHashMap<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("szAgentId",szAgentId);
        requestMap.put("szOrderId",szOrderId);
        requestMap.put("szPhoneNum",szPhoneNum);
        requestMap.put("nMoney",nMoney);
        requestMap.put("nSortType",nSortType);
        requestMap.put("nProductClass",nProductClass);
        requestMap.put("nProductType",nProductType);
        requestMap.put("szTimeStamp",szTimeStamp);
        requestMap.put("szVerifyString",szVerifyString);
        requestMap.put("szNotifyUrl",szNotifyUrl);
        requestMap.put("szFormat",szFormat);
        try {
            logger.info("{}永禾,发送充值的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url,requestMap,"utf-8", 5000);
            logger.info("{}永禾,接收充值的参数:{}", rechargeOrderBean.getOrderId(), JSON.toJSONString(responseBody));
            String Code = JSONObject.parseObject(responseBody).getString("nRtn");
            if (StringUtils.equals("0",Code)){
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            }else if(rechargeErrorCode.contains(Code)){
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }else{
                return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
            }
        } catch (Exception e) {
            logger.info("{}永禾,发送充值报错的参数:{}", rechargeOrderBean.getOrderId(), e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "提交报错："+e.getMessage());
        }

    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("queryUrl");
        String szAgentId = configJSONObject.getString("szAgentId");
        String szKey = configJSONObject.getString("szKey");
        String szOrderId = channelOrder.getChannelOrderId();
        StringBuffer sb = new StringBuffer();
        sb.append("szAgentId=").append(szAgentId).append("&szOrderId=").append(szOrderId)
                .append("&szKey=").append(szKey);
        String szVerifyString= DigestUtils.md5Hex(sb.toString()).toLowerCase();
        LinkedHashMap<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("szAgentId",szAgentId);
        requestMap.put("szOrderId",szOrderId);
        requestMap.put("szVerifyString",szVerifyString);
        requestMap.put("szFormat","JSON");
        try {
            logger.info("{}永禾,发送查单的参数:{}", channelOrder.getOrderId(), JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url,requestMap,"utf-8", 5000);
            logger.info("{}永禾,接收查单的参数:{}", channelOrder.getOrderId(), JSON.toJSONString(responseBody));
            String Code = JSONObject.parseObject(responseBody).getString("nRtn");
            if (StringUtils.equals("5012",Code)){
                String szRtnMsg = JSONObject.parseObject(responseBody).getString("szRtnMsg");
                if(!StringUtils.isEmpty(szRtnMsg)){
                    channelOrder.setOutChannelOrderId(szRtnMsg);
                }
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            }else if(StringUtils.equals("5013",Code)){
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            }else if(StringUtils.equals("5005",Code)){
                return new ProcessResult(ProcessResult.UNKOWN, "订单不存在");
            }else {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            }
        } catch (Exception e) {
            logger.info("{}永禾,发送查单报错的参数:{}", channelOrder.getOrderId(),e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "查询报错"+e.getMessage());

        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("3", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("balanceQueryUrl");
        String szAgentId = configJSONObject.getString("szAgentId");
        String szKey = configJSONObject.getString("szKey");
        String szFormat = "JSON";
        StringBuffer sb = new StringBuffer();
        sb.append("szAgentId=").append(szAgentId).append("&szKey=").append(szKey);
        String szVerifyString= DigestUtils.md5Hex(sb.toString()).toLowerCase();
        LinkedHashMap<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("szAgentId",szAgentId);
        requestMap.put("szVerifyString",szVerifyString);
        requestMap.put("szFormat",szFormat);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url,requestMap,"utf-8", 5000);
            String Code = JSONObject.parseObject(responseBody).getString("nRtn");
            if (StringUtils.equals("0",Code)){
                String Balance = JSONObject.parseObject(responseBody).getString("fBalance");
                return new BigDecimal(Balance);
            }else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Test
    void test(){
        ChannelOrder channelOrder = new ChannelOrder();
        Channel channel = new Channel();
        channelOrder.setChannelOrderId("cs202103101546");
        channel.setConfigInfo("{rechargeUrl:\"http://8.129.227.254:10089/plat/api/old/submitorder\",queryUrl:\"http://8.129.227.254:10089/plat/api/old/queryorder\",balanceQueryUrl:\"http://8.129.227.254:10089/plat/api/old/queryBalance\",szAgentId:\"200072\",szKey:\"665e26a0e3a04206a56659c684ed67e6\",notify_url:\"http://115.28.88.114:8083/YongHe/callBack\"}");
//        BigDecimal bigDecimal = balanceQuery(channel);
//        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
        ProcessResult query = query(channel, channelOrder);
    }
}
