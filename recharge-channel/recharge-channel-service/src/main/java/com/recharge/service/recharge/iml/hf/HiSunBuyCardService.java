package com.recharge.service.recharge.iml.hf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hisun.crypt.mac.CryptUtilImpl;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@Service
public class HiSunBuyCardService extends AbsChannelRechargeService {

    private String supId = "hiSun";

    private static final String CHNNO = "11099";//渠道号
    private static final String OPENID = "888030916531808";//OPENID
    private static final String SIGNKEY = "t4ao18K05q";//MD5密钥
    private static final String DESKEY = "947Nx9u4D1";//DES密钥
    private static final String URL = "http://api.quancangyun.com/api/service.htm";

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        BigDecimal bigDecimal = new HiSunBuyCardService().balanceQuery(new Channel());
        System.out.println("bigDecimal = " + bigDecimal);
    }

    /**
     * 通用请求头
     *
     * @return
     */
    public static JSONObject headCommon(String apiId) {

        SimpleDateFormat sdf = new SimpleDateFormat();
        JSONObject header = new JSONObject(true);
        //卡券购买接口
        header.put("apiid", apiId);
        sdf.applyPattern("yyyyMMdd");
        header.put("busdt", sdf.format(new Date())); //业务时间
        header.put("chnno", CHNNO); //渠道编号
        header.put("ipaddr", "127.0.0.1"); //渠道IP

        sdf.applyPattern("yyMMdd");
        String requestJournal = Long.toString(Math.abs(new Random().nextLong()));
        header.put("reqjnl", sdf.format(new Date()) + requestJournal); //请求流水 25位全局唯一，不可重复

        sdf.applyPattern("yyyyMMddHHmmss");
        header.put("reqopetm", sdf.format(new Date())); //请求操作时间
        header.put("version", "1.0");
        return header;
    }

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        return new ProcessResult(ProcessResult.FAIL, "充值失败");
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        return new ProcessResult(ProcessResult.FAIL, "充值失败");
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        return new ProcessResult(ProcessResult.FAIL, "充值失败");
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        //------------------------通用---------------------
        JSONObject request = new JSONObject(true);
        JSONObject header = headCommon("620028");
        request.put("header", header);


        //------------------------业务参数 n---------------------
        JSONObject body = new JSONObject(true);
        body.put("mercid", OPENID);
        request.put("body", body);


        //------------------------加密操作---------------------
        JSONObject security = new JSONObject();

        String desKey = DESKEY + header.getString("reqopetm");
        CryptUtilImpl cryptUtil = new CryptUtilImpl();
        String desvalue = cryptUtil.cryptDes(desKey, desKey);

        String signKey = SIGNKEY + header.getString("reqopetm");
        String signatureValue = cryptUtil.cryptMd5(JSON.toJSONString(request), signKey);
        security.put("desvalue", desvalue);
        security.put("signvalue", signatureValue);
        request.put("security", security);


        //--------------------以上为构建完整的请求参数----------------------------
        String requestData = JSON.toJSONString(request);
        System.out.println(requestData);
        //发送请求
        String responseData = "";
        try {
            StringEntity stringEntity = new StringEntity(requestData);
            responseData = HttpClientUtils.invokePostString(URL, stringEntity, "GBK", 5000);
            JSONObject responseObject = JSONObject.parseObject(responseData);
            JSONObject headerObject = responseObject.getJSONObject("header");
            String respcode = headerObject.getString("respcode");
            if (StringUtils.equals("RMP0000", respcode)) {
                JSONObject bodyObject = responseObject.getJSONObject("body");
                String mertbalance = bodyObject.getString("mertbalance");
                return new BigDecimal(mertbalance).divide(new BigDecimal("100"));
            } else {
                return BigDecimal.ZERO;
            }

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
