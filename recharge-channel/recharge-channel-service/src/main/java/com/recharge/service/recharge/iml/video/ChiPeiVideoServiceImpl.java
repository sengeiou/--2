package com.recharge.service.recharge.iml.video;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.center.bean.VideoRechargeInfoBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.utils.ChiPeiUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qi.cao
 */
@Service
public class ChiPeiVideoServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());



    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        VideoRechargeInfoBean videoRechargeInfoBean = (VideoRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(VideoRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("url");
        String account = configJSONObject.getString("account");
        String pwd = configJSONObject.getString("pwd");
        String callBackUrl = configJSONObject.getString("callBackUrl");

        /**
         * 获取请求参数
         */
        // 时间戳（毫秒，该时间与服务端时间相差不得超过5min）
        String timestamp = String.valueOf(System.currentTimeMillis());
        // 产品编码
        String pCode = queryChannelProductId(rechargeOrderBean.getProductName() , "100028").getChannelProductId();
//        String pCode = "11121130001";
        // 需要充值的用户账号，喜马拉雅&优酷&芒果会员请务必输入正确的手机号码；腾讯视频会员请输入正确的QQ号码；
        String username= videoRechargeInfoBean.getAccount();
        String usernameStr=username;
        String md5pwd = ChiPeiUtils.strToMd5(pwd, "utf-8");
        //加密username
        String secretKey = md5pwd.substring(0, 16);
        String vector = md5pwd.substring(16, md5pwd.length());
        username = ChiPeiUtils.encrypt(username, secretKey, vector);
        callBackUrl= new Base64().encodeToString(callBackUrl.getBytes());
        // 腾讯视频会员为必填项，充值通知信息发送到该号码上
        String mobile="1";
        // 腾讯视频会员为必填项，填‘1’，同时在username中填写QQ号；腾讯视频会员以外不填或填‘0’
        String type="0";
        // 客户订单号，6-32位字符串,(字母或数字)
        String clientOrderId = channelOrder.getChannelOrderId();

        // 获取sign加密前的原字符串，密码进行MD5加密
        String signBef = account+usernameStr + md5pwd + timestamp + pCode + clientOrderId;
        // 进行sign加密
        String signAft = ChiPeiUtils.strToMd5(signBef, "utf-8");

        /**
         * 请求参数转换为JSON
         */
        Map<String, String> _rep = new HashMap<String, String>();
        _rep.put("timestamp", timestamp);
        _rep.put("account", account);
        _rep.put("pCode", pCode);
        _rep.put("clientOrderNo", clientOrderId);
        _rep.put("username", username);
        _rep.put("callBackUrl", callBackUrl);
        _rep.put("mobile", mobile);
        _rep.put("type", type);
        _rep.put("sign", signAft);


        String iCommString = JSONObject.toJSONString(_rep);

        /**
         * 发送post请求
         */
        String responseBody = null;
        try {
            logger.info("request param:{}",iCommString);
            responseBody = HttpClientUtils.invokePostString(url,new StringEntity(iCommString),"utf-8",5000);
            logger.info("response body :{}", responseBody);

            String retCode = JSONObject.parseObject(responseBody).getString("status");
            String msg = JSONObject.parseObject(responseBody).getString("msg");
            if (StringUtils.equals("0",retCode)){
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else if (StringUtils.equals("1",retCode)){
                return new ProcessResult(ProcessResult.FAIL,msg);
            }else{
                return new ProcessResult(ProcessResult.UNKOWN,msg);
            }
        } catch (Exception e) {
            logger.error("orderId :{} ,send unkown.", channelOrder.getOrderId() ,e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryUrl = configJSONObject.getString("queryUrl");
        String account = configJSONObject.getString("account");
        String pwd = configJSONObject.getString("pwd");

        /**
         * 请求参数转换为JSON
         */
        Map<String, String> _rep = new HashMap<String, String>();
        _rep.put("account", account);
        _rep.put("clientOrderNo", channelOrder.getChannelOrderId());
        _rep.put("sign", DigestUtils.md5Hex(account+DigestUtils.md5Hex(pwd)));


        String iCommString = JSONObject.toJSONString(_rep);

        /**
         * 发送post请求
         */
        String responseBody = null;
        try {
            logger.info("request param:{}",iCommString);
            responseBody = HttpClientUtils.invokePostString(queryUrl,new StringEntity(iCommString),"utf-8",5000);
//            responseBody = responseBody.replaceAll("\\\\","");
            logger.info("response body :{}", responseBody);
            String retCode = JSONObject.parseObject(responseBody).getString("status");
            String msg = JSONObject.parseObject(responseBody).getString("msg");
            if (StringUtils.equals("0",retCode)){
                String data = JSONObject.parseObject(responseBody).getString("data");
                String orderStatus = JSONObject.parseObject(data).getString("orderStatus");
                if (StringUtils.equals("0" , orderStatus)){
                    return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
                }else if (StringUtils.equals("1" , orderStatus)){
                    return new ProcessResult(ProcessResult.PROCESSING,"充值中");
                }else if (StringUtils.equals("2" , orderStatus)){
                    String errorDesc = JSONObject.parseObject(responseBody).getJSONObject("data").getString("errorDesc");
                    return new ProcessResult(ProcessResult.PROCESSING,errorDesc);
                }
                return new ProcessResult(ProcessResult.UNKOWN,msg);
            }else if (StringUtils.equals("-1",retCode)){
                return new ProcessResult(ProcessResult.FAIL,msg);
            }else{
                return new ProcessResult(ProcessResult.UNKOWN,msg);
            }
        } catch (Exception e) {
            logger.error("orderId :{} ,send unkown.", channelOrder.getOrderId() ,e);
            return new ProcessResult(ProcessResult.PROCESSING,"submit query request unknown");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("100028" , responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
        }else{
            return new ProcessResult(ProcessResult.FAIL,responseOrder.getResponseMsg());
        }
    }


}
