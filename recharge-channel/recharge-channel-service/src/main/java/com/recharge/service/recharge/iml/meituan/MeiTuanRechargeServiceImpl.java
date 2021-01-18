package com.recharge.service.recharge.iml.meituan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.MeiTuanRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MeiTuanRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

//    public static void main(String[] args) {
//        MeiTuanRechargeServiceImpl haoChongRechargeService = new MeiTuanRechargeServiceImpl();
//
//        Channel channel = new Channel();
//        channel.setConfigInfo("{organizerId:\"KaLiFang20190527\",meiTuanDomain:\"http://esales.fhcard.com/onlinepay.do\"," +
//                "key:\"tf43!4#*\","+
//                "iv:\"0123456789abcdef\","+
//                "token:\"kalifang@123\","+
//                "terminalId:\"KaLi1558927784664d2d185e\"," +
//                "meiTuanDomain:\"https://activities-api.dianping.com/\"}");
//        ChannelOrder channelOrder = new ChannelOrder();
//        channelOrder.setChannelOrderId("MT201703221321003");
//        channelOrder.setRechargeNumber("15715141438");
//        RechargeOrderBean rechargeOrderBean= new RechargeOrderBean();
//        rechargeOrderBean.setOrderId("R00000000003");
//        MeiTuanRechargeInfoBean huaFeiRechargeInfoBean = new MeiTuanRechargeInfoBean();
//        huaFeiRechargeInfoBean.setProductId("123123123");
//        huaFeiRechargeInfoBean.setPhone("15715141438");
//        rechargeOrderBean.setRechargeInfoBeanObj(huaFeiRechargeInfoBean);
//        ProcessResult result = haoChongRechargeService.query(channel ,channelOrder);
//
//        System.out.println(channelOrder.getOutChannelOrderId() + result.getMsg());
//    }

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        MeiTuanRechargeInfoBean meiTuanRechargeInfoBean = (MeiTuanRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(MeiTuanRechargeInfoBean.class);


        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String organizerId = configJSONObject.getString("organizerId");
        String terminalId = configJSONObject.getString("terminalId");
        String meiTuanDomain = configJSONObject.getString("meiTuanDomain");
        String key = configJSONObject.getString("key");
        String iv = configJSONObject.getString("iv");
        String token = configJSONObject.getString("token");


        RequestBody requestBody = new RequestBody();
        requestBody.setOrganizerId(organizerId);
        requestBody.setTimestamp(System.currentTimeMillis() + "");

        ProductRelation productRelation = queryChannelProductId(rechargeOrderBean.getProductName(), "100038");

        MessageBody messageBody = new MessageBody();
        messageBody.setTerminalId(terminalId);
        messageBody.setCouponType(productRelation.getChannelProductId().split("_")[0]);
        messageBody.setMobileNo(meiTuanRechargeInfoBean.getPhone());
        messageBody.setIp("127.0.0.1");
        messageBody.setReferer(meiTuanDomain);
        messageBody.setUa("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
        messageBody.setRequestNo(channelOrder.getChannelOrderId());
        messageBody.setPlatform(productRelation.getChannelProductId().split("_")[1]);
        requestBody.setMessage(encrypt(JSONObject.toJSONString(messageBody), key, iv));

        StringBuffer sb = new StringBuffer();
        sb.append(requestBody.getCallType());
        sb.append(requestBody.getFormat());
        sb.append(requestBody.getMessage());
        sb.append(requestBody.getMethod());
        sb.append(requestBody.getOrganizerId());
        sb.append(requestBody.getTimestamp());
        sb.append(requestBody.getVersion());
        sb.append(token);
        String sign = DigestUtils.md5Hex(sb.toString()).toUpperCase();

        requestBody.setSign(sign);

        try {
            logger.info("send requestBody :{}", JSONObject.toJSONString(requestBody));
            StringEntity stringEntity = new StringEntity(JSONObject.toJSONString(requestBody));
            String responseData = HttpClientUtils.invokePostString(meiTuanDomain + "/emidas/third-party/", stringEntity, "GBK", 5000);
            logger.info("receive responseBody :{}", responseData);
            String resultCode = JSONObject.parseObject(responseData).getString("resultCode");
            if (StringUtils.equals(resultCode, "000000")) {
                String winningRecordId = JSONObject.parseObject(responseData).getJSONObject("data").getString("winningRecordId");
                channelOrder.setOutChannelOrderId(winningRecordId);

                ResponseOrder responseOrder = new ResponseOrder();
                responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                responseOrder.setOutChannelOrderId(winningRecordId);
                responseOrder.setResponseCode("00");
                responseOrder.setResponseMsg("充值成功");
                callBack(responseOrder, "100038");
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
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
        String organizerId = configJSONObject.getString("organizerId");
        String terminalId = configJSONObject.getString("terminalId");
        String meiTuanDomain = configJSONObject.getString("meiTuanDomain");
        String key = configJSONObject.getString("key");
        String iv = configJSONObject.getString("iv");
        String token = configJSONObject.getString("token");


        RequestBody requestBody = new RequestBody();
        requestBody.setOrganizerId(organizerId);
        requestBody.setTimestamp(System.currentTimeMillis() + "");
        requestBody.setMethod("13");

        String tem = "1_0";

        MessageBody messageBody = new MessageBody();
        messageBody.setTerminalId(terminalId);
        messageBody.setCouponType(tem.split("_")[0]);
        messageBody.setMobileNo(channelOrder.getRechargeNumber());
        messageBody.setIp("127.0.0.1");
        messageBody.setReferer(meiTuanDomain);
        messageBody.setUa("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
        messageBody.setRequestNo(channelOrder.getChannelOrderId());
        messageBody.setPlatform(tem.split("_")[1]);
        requestBody.setMessage(encrypt(JSONObject.toJSONString(messageBody), key, iv));

        StringBuffer sb = new StringBuffer();
        sb.append(requestBody.getCallType());
        sb.append(requestBody.getFormat());
        sb.append(requestBody.getMessage());
        sb.append(requestBody.getMethod());
        sb.append(requestBody.getOrganizerId());
        sb.append(requestBody.getTimestamp());
        sb.append(requestBody.getVersion());
        sb.append(token);
        String sign = DigestUtils.md5Hex(sb.toString()).toUpperCase();

        requestBody.setSign(sign);

        try {
            logger.info("send query requestBody :{}", JSONObject.toJSONString(requestBody));
            StringEntity stringEntity = new StringEntity(JSONObject.toJSONString(requestBody));
            String responseData = HttpClientUtils.invokePostString(meiTuanDomain + "/emidas/third-party/", stringEntity, "GBK", 5000);
            logger.info("receive query responseBody :{}", responseData);
            String resultCode = JSONObject.parseObject(responseData).getString("resultCode");
            if (StringUtils.equals(resultCode, "000000")) {
                JSONObject jsonObject = JSONObject.parseObject(responseData).getJSONArray("data").getJSONObject(0);
                String winningRecordId = jsonObject.getString("winningRecordId");
                channelOrder.setOutChannelOrderId(winningRecordId);

                ResponseOrder responseOrder = new ResponseOrder();
                responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                responseOrder.setOutChannelOrderId(winningRecordId);
                responseOrder.setResponseCode("00");
                responseOrder.setResponseMsg("充值成功");
                callBack(responseOrder, "100038");
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
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
        if (StringUtils.equals(responseOrder.getResponseCode(), "00")) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        }
        return new ProcessResult(ProcessResult.FAIL, "充值失败");
    }


    /**
     * 解密
     *
     * @param encMessage
     * @param key
     * @param iv
     * @return
     */
    public String decrypt(String encMessage, String key, String iv) {
        String returnValue = null;
        try {
            byte[] keyBytes = key.getBytes();
            byte[] ivBytes = iv.getBytes();

            AESCipher cipher = new AESCipher();
            byte[] encBytesBuf = new byte[encMessage.length() / 2];
            HexUtil.parseHexString(encMessage, encBytesBuf, 0);

            byte[] decBytes = cipher.decrypt(keyBytes, ivBytes, encBytesBuf);

            returnValue = new String(decBytes);

            return returnValue;
        } catch (Exception e) {
            logger.error("decrypt", e);
        }

        return null;
    }

    /**
     * 加密
     *
     * @param original
     * @param key
     * @param iv
     * @return
     */
    public String encrypt(String original, String key, String iv) {
        String returnValue = null;
        try {
            byte[] keyBytes = key.getBytes();
            byte[] ivBytes = iv.getBytes();
            byte[] inputBytes = original.getBytes();

            AESCipher cipher = new AESCipher();
            byte[] encBytes = cipher.encrypt(keyBytes, ivBytes, inputBytes);

            returnValue = HexUtil.toHexString(encBytes);

            return returnValue;
        } catch (Exception e) {
            logger.error("encrypt", e);
        }

        return null;
    }


    class MessageBody {
        /**
         * 终端ID：具体值由服务方提供
         */
        private String terminalId;
        /**
         * 发券类型：由双方约定
         * 1——点评测试券
         * 11——美团测试券
         */
        private String couponType;
        /**
         * 手机号
         */
        private String mobileNo;
        private String ip;
        /**
         * 请求Referer
         */
        private String referer;
        /**
         * 请求UA
         */
        private String ua;
        /**
         * 请求流水号：保证唯一性
         */
        private String requestNo;
        /**
         * 发券平台：0点评，1美团
         */
        private String platform;

        public String getTerminalId() {
            return terminalId;
        }

        public void setTerminalId(String terminalId) {
            this.terminalId = terminalId;
        }

        public String getCouponType() {
            return couponType;
        }

        public void setCouponType(String couponType) {
            this.couponType = couponType;
        }

        public String getMobileNo() {
            return mobileNo;
        }

        public void setMobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getReferer() {
            return referer;
        }

        public void setReferer(String referer) {
            this.referer = referer;
        }

        public String getUa() {
            return ua;
        }

        public void setUa(String ua) {
            this.ua = ua;
        }

        public String getRequestNo() {
            return requestNo;
        }

        public void setRequestNo(String requestNo) {
            this.requestNo = requestNo;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }
    }


    class RequestBody {
        /**
         * 回调类型：1同步响应，2异步响应
         */
        private String callType = "1";
        /**
         * 格式类型：1 XML，2 JSON
         */
        private String format = "2";
        private String version = "1.0";
        /**
         * 组织ID：具体值由服务方提供
         */
        private String organizerId;
        private String timestamp;
        private String sign = "14A331E205F0A108D7BEF5719178C175";
        /**
         * 请求方法：具体值由服务方提供
         */
        private String method = "11";
        /**
         * 消息体，具体参考下表消息体说明
         */
        private String message;

        public String getCallType() {
            return callType;
        }

        public void setCallType(String callType) {
            this.callType = callType;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getOrganizerId() {
            return organizerId;
        }

        public void setOrganizerId(String organizerId) {
            this.organizerId = organizerId;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
