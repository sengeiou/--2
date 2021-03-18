package com.recharge.service.recharge.iml.zfb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayFundTransCommonQueryRequest;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.response.AlipayFundTransCommonQueryResponse;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.center.bean.ZhifubaoRechargeInfoBean;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.service.recharge.AbsChannelRechargeService;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ZhiFuBaoRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        ZhifubaoRechargeInfoBean zhifubaoRechargeInfoBean = (ZhifubaoRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(ZhifubaoRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String rechargeUrl = configJSONObject.getString("rechargeUrl");
        String appId = configJSONObject.getString("appId");
        String appPrivateKey = configJSONObject.getString("appPrivateKey");
        String certPath = configJSONObject.getString("certPath");
        String alipayPublicCertPath = configJSONObject.getString("alipayPublicCertPath");
        String rootCertPath = configJSONObject.getString("rootCertPath");

        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(rechargeUrl);
        certAlipayRequest.setAppId(appId);
        certAlipayRequest.setPrivateKey(appPrivateKey);
        certAlipayRequest.setFormat("json");
        certAlipayRequest.setCharset("GBK");
        certAlipayRequest.setSignType("RSA2");
        certAlipayRequest.setCertPath(certPath);
        certAlipayRequest.setAlipayPublicCertPath(alipayPublicCertPath);
        certAlipayRequest.setRootCertPath(rootCertPath);
        try {
            DefaultAlipayClient defaultAlipayClient = new DefaultAlipayClient(certAlipayRequest);
            //传输金额
            BigDecimal transAmount = new BigDecimal(zhifubaoRechargeInfoBean.getNum());
            transAmount = transAmount.setScale(2,BigDecimal.ROUND_HALF_UP);
            logger.info("zhifubao transAmount {}", transAmount.toPlainString());
            AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
            request.setBizContent("{" +
                    "\"out_biz_no\":\"" + channelOrder.getChannelOrderId() + "\"," +
                    "\"trans_amount\":\"" + transAmount.toString() + "\"," +
                    "\"product_code\":\"TRANS_ACCOUNT_NO_PWD\"," +
                    "\"biz_scene\":\"DIRECT_TRANSFER\"," +
                    "\"payee_type\":\"ALIPAY_LOGONID\"," +
                    "\"payee_info\":{" +
                    "\"identity\":\"" + zhifubaoRechargeInfoBean.getIncomeAccount() + "\"," +
                    "\"identity_type\":\"ALIPAY_LOGON_ID\"," +
                    "\"name\":\"" + zhifubaoRechargeInfoBean.getIncomeUserName() + "\"" +
                    "    }," +
                    "\"remark\":\"" + zhifubaoRechargeInfoBean.getIncomeRemark() + "\"," +
                    "  }");
            logger.info("zhifubao request param {}", JSON.toJSONString(request));
            try {
                AlipayFundTransUniTransferResponse response = defaultAlipayClient.certificateExecute(request);
                logger.info("支付宝下单请求订单号{},返回值{}",channelOrder.getChannelOrderId(),JSON.toJSONString(response));
                if (response.isSuccess()) {
                    return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
                } else {
                    if (StringUtils.equals("付款方余额不足", response.getSubMsg())) {
                        return new ProcessResult(ProcessResult.UNKOWN, response.getSubMsg());
                    } else {
                        return new ProcessResult(ProcessResult.FAIL, response.getSubMsg());
                    }
                }
            } catch (AlipayApiException e) {
                logger.info("zhifubao invoke error", e.getErrMsg());
                return new ProcessResult(ProcessResult.UNKOWN, "zhifubao invoke error="+e.getMessage());
            }
        } catch (AlipayApiException e) {
            logger.info("zhifubao invoke error", e);
            return new ProcessResult(ProcessResult.UNKOWN, "zhifubao invoke error="+e.getMessage());
        }
    }


    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String rechargeUrl = configJSONObject.getString("rechargeUrl");
        String appId = configJSONObject.getString("appId");
        String appPrivateKey = configJSONObject.getString("appPrivateKey");
        String certPath = configJSONObject.getString("certPath");
        String alipayPublicCertPath = configJSONObject.getString("alipayPublicCertPath");
        String rootCertPath = configJSONObject.getString("rootCertPath");
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(rechargeUrl);
        certAlipayRequest.setAppId(appId);
        certAlipayRequest.setPrivateKey(appPrivateKey);
        certAlipayRequest.setFormat("json");
        certAlipayRequest.setCharset("GBK");
        certAlipayRequest.setSignType("RSA2");
        certAlipayRequest.setCertPath(certPath);
        certAlipayRequest.setAlipayPublicCertPath(alipayPublicCertPath);
        certAlipayRequest.setRootCertPath(rootCertPath);
        DefaultAlipayClient alipayClient = null;
        try {
            alipayClient = new DefaultAlipayClient(certAlipayRequest);


            AlipayFundTransCommonQueryRequest request = new AlipayFundTransCommonQueryRequest();
            request.setBizContent("{" +
                    "\"product_code\":\"TRANS_ACCOUNT_NO_PWD\"," +
                    "\"biz_scene\":\"DIRECT_TRANSFER\"," +
                    "\"out_biz_no\":\"" + channelOrder.getChannelOrderId() + "\"," +
                    "  }");
            AlipayFundTransCommonQueryResponse response = alipayClient.certificateExecute(request);
            logger.info("支付宝查询请求订单号{},返回值{}",channelOrder.getChannelOrderId(),JSON.toJSONString(response));
            if (response.isSuccess()) {
                if (StringUtils.equals("SUCCESS", response.getStatus())) {
                    return new ProcessResult(ProcessResult.SUCCESS, "转账成功");
                } else if (StringUtils.equals("FAIL", response.getStatus())) {
                    return new ProcessResult(ProcessResult.FAIL, response.getFailReason());
                } else {
                    return new ProcessResult(ProcessResult.PROCESSING, "处理中");
                }
            } else {
                logger.info("支付宝查询处理中对应的订单号{}",channelOrder.getChannelOrderId());
                return new ProcessResult(ProcessResult.PROCESSING, "查询未知="+response.getFailReason());
            }
        } catch (AlipayApiException e) {
            logger.info("支付宝查询接口发送失败对应的订单号{}",channelOrder.getChannelOrderId());
            return new ProcessResult(ProcessResult.PROCESSING, "接口发送失败="+e.getMessage());
        }

    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        return null;
    }
}
