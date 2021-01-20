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
import org.apache.http.conn.ConnectTimeoutException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WanBaoNewRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      请求地址：http://129.204.60.17:8107/MainServiceBusiness/SendPhoneChargeInfo
        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

//        手机号码为1 固话号码为2
        String chargenumbertype = "1";
//        1表示get返回 2表示返回XML信息。目前只支持xml方式。
        String returntype = "2";
        String orderid = channelOrder.getChannelOrderId();
        String chargenumber = huaFeiRechargeInfoBean.getPhone();
        String amountmoney = huaFeiRechargeInfoBean.getAmt().toString();
//        固话时需要输入 移动、联通、电信 传汉字，用utf-8编码，进行MD5加密时不用编码，手机号可空。
        String ispname = "";
//        代理商请填写2，此处务必填写2，否则可能造成提交订单号重复。
        String source = "2";
        String verifystring = DigestUtils.md5Hex("chargenumbertype=" + chargenumbertype +
                "&agentid=" + userId +
                "&returntype=" + returntype +
                "&orderid=" + orderid +
                "&chargenumber=" + chargenumber +
                "&amountmoney=" + amountmoney +
                "&ispname=" + "" +
                "&source=" + source +
                "&merchantKey=" + key);

        try {
            String requestUrl = url + "?chargenumbertype=" + chargenumbertype +
                    "&agentid=" + userId +
                    "&returntype=" + returntype +
                    "&orderid=" + orderid +
                    "&chargenumber=" + chargenumber +
                    "&amountmoney=" + amountmoney +
                    "&ispname=" + "" +
                    "&source=" + source +
                    "&verifystring=" + verifystring;
            logger.info("send recharge request params:{}", requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            String resultmessage = root.elementText("resultmessage");

            if (StringUtils.equals("0000", resultno)) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, resultmessage);
            }

        } catch (Exception e) {
            logger.error("{}send error{}", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.SUCCESS, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      请求地址：http://129.204.60.17:8107/MainServiceBusiness/GetOrderInfo
        String queryUrl = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

//        1表示get返回 2表示返回XML信息。目前只支持xml方式。
        String returntype = "2";
        String orderid = channelOrder.getChannelOrderId();
        String verifystring = DigestUtils.md5Hex("agentid=" + userId +
                "&returntype=" + returntype +
                "&orderid=" + orderid +
                "&merchantKey=" + key);

        try {
            String requestUrl = queryUrl + "?agentid=" + userId +
                    "&returntype=" + returntype +
                    "&orderid=" + orderid +
                    "&verifystring=" + verifystring;
            logger.info("send query request params:{}", requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "utf-8", 5000);
            logger.info("send query response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            String resultmessage = root.elementText("resultmessage");

            if (StringUtils.equals("0014", resultno)) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals("0016", resultno)) {
                return new ProcessResult(ProcessResult.PROCESSING, "处理中");
            } else {
                return new ProcessResult(ProcessResult.FAIL, resultmessage);
            }

        } catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "处理中");
        }
    }


    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
//      0014 成功，0015 失败。
        if (StringUtils.equals("0014", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("0015", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      代理商信息查询地址：http://129.204.60.17:8107/MainServiceBusiness/GetAgentInfo
        String queryBalanceUrl = configJSONObject.getString("queryBalanceUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

        String verifystring = DigestUtils.md5Hex(
                "agentid=" + userId +
                        "&merchantKey=" + key);
        try {
            String requestUrl = queryBalanceUrl + "?agentid=" + userId +
                    "&verifystring=" + verifystring;
            logger.info("send queryBalance request params:{}", requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "utf-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String resultno = root.elementText("resultno");
            String agentbalance = root.elementText("agentbalance");
            if (StringUtils.equals("1000", resultno)) {
                return new BigDecimal(agentbalance);
            } else {
                return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

}
