package com.recharge.service.recharge.iml.weixin;

import com.github.binarywang.wxpay.bean.entpay.EntPayRequest;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.recharge.common.utils.HttpClientUtils;
import org.apache.commons.lang3.StringUtils;

public class WeiXinService {

    public static void main(String[] args) throws WxPayException {
        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setAppId("wx7a1c0ffe8fde53c6");
        payConfig.setMchId("1462190202");
        payConfig.setKeyPath("/Users/mbyd/cert/1462190202_20190311_cert/apiclient_cert.p12");

        // 可以指定是否使用沙箱环境
        payConfig.setUseSandboxEnv(false);

        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);

        EntPayRequest request = new EntPayRequest();
        request.setAmount(1);
        request.setMchAppid(payConfig.getAppId());
        request.setMchId(payConfig.getMchId());
        request.setPartnerTradeNo("WXPS2105000001");
        request.setOpenid("");
        wxPayService.getEntPayService().entPay(request);
    }
}
