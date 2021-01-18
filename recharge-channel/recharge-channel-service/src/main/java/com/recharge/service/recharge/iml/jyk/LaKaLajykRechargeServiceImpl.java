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
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.utils.desDemo;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020/11/30 17:00
 */
@Service
public class LaKaLajykRechargeServiceImpl extends AbsChannelRechargeService {
    private static final String key = "71bbdb25212abead"; // 加密key ,由拉卡拉提供
    private static final String iv = "55068b7e"; // 加密iv 由拉卡拉提供
    //加密方式和加密模式定义
    private static final String MCRYPT_TRIPLEDES = "DESede";
    private static final String TRANSFORMATION = "DESede/CBC/PKCS5Padding";
    private static String[] telFirst = "134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 加密key值，由拉卡拉提供
     *
     * @return
     */
    public static byte[] getSecretKey() {
        String newKey = desDemo.rightFillMethod(key, 24);
        return newKey.getBytes();
    }

    /**
     * 补位至指定长度
     *
     * @param str
     * @param j
     * @return
     */
    public static String rightFillMethod(String str, int j) {
        if (j > str.length())
            j = j % str.length();

        for (int i = 0; i < j; i++)
            str += "0";
        return str;
    }

    /**
     * 加密用到的IV值，有拉卡拉提供，比如64176527，要按照下面的方式分拆到byte数组中
     *
     * @return
     */
    public static byte[] getIVBytes() {

        return iv.getBytes();
    }

    /**
     * 解密函数
     *
     * @param data 加密字符串
     * @return 解密后的字符串
     */
    public static String decrypt(String data) {
        if (data == null)
            return null;
        String result = null;
        try {
            DESedeKeySpec spec = new DESedeKeySpec(getSecretKey());

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(MCRYPT_TRIPLEDES);
            SecretKey sec = keyFactory.generateSecret(spec);

            IvParameterSpec IvParameters = new IvParameterSpec(getIVBytes());

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, sec, IvParameters);

            result = new String(cipher.doFinal(Base64.decode(data)), "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 加密函数
     *
     * @param data 加密前的字符串
     * @return 加密后的字符串
     */
    public static String encrypt(String data) {
        if (data == null)
            return null;

        String result = null;
        try {
            DESedeKeySpec spec = new DESedeKeySpec(getSecretKey());

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(MCRYPT_TRIPLEDES);
            SecretKey sec = keyFactory.generateSecret(spec);

            IvParameterSpec IvParameters = new IvParameterSpec(getIVBytes());

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, sec, IvParameters);

            result = Base64.encode(cipher.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int getNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    private static String getTel() {
        int index = getNum(0, telFirst.length - 1);
        String first = telFirst[index];
        String second = String.valueOf(getNum(1, 888) + 10000).substring(1);
        String third = String.valueOf(getNum(1, 9100) + 10000).substring(1);
        return first + second + third;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            System.out.println(getTel());
        }
    }

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String partner_id = configJSONObject.getString("merchantId");
        String url = configJSONObject.getString("url") + partner_id;
        int hours = (int) Math.floor(System.currentTimeMillis() / 1000 / 600) * 600;
        String sign_generate_time = Integer.toString(hours);
        String order_id = channelOrder.getChannelOrderId();
        ProductRelation productRelation = queryChannelProductId(rechargeOrderBean.getProductName(), "100110");
        String item_id = productRelation.getChannelProductId();
        String account = jykRechargeInfoBean.getAccount();
        String phone = getTel();
        String client_ip = configJSONObject.getString("client_ip");
        String version = configJSONObject.getString("version");
        String signBF = "account=" + account + "&client_ip=" + client_ip +
                "&item_id=" + item_id + "&order_id=" + order_id + "&partner_id=" + partner_id +
                "&phone=" + phone + "&sign_generate_time=" + sign_generate_time + "&version=" + version;
        String sign = DigestUtils.md5Hex(signBF);
        String Json = "{\"order_id\":\"" + order_id +
                "\",\"item_id\":\"" + item_id +
                "\",\"account\":\"" + account +
                "\",\"phone\":\"" + phone +
                "\",\"client_ip\":\"" + client_ip +
                "\",\"partner_id\":\"" + partner_id +
                "\",\"sign_generate_time\":\"" + sign_generate_time +
                "\",\"version\":\"" + version +
                "\",\"sign\":\"" + sign + "\"}";
        String param = encrypt(Json);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("param", param);
        String requestString = JSONObject.toJSONString(requestMap);
        try {
            logger.info("lakalajyk recharge request param:{}", JSONObject.toJSONString(Json));
            String responseBody = HttpClientUtils.invokePostString(url, new StringEntity(requestString), "utf-8", 5000);
            String responseInfo = decrypt(responseBody);
            logger.info("lakalajyk recharge response body :{}", JSONObject.toJSONString(responseInfo));
            String res = JSONObject.parseObject(responseInfo).getString("res");
            if (res.equals("true")) {
                String data = JSONObject.parseObject(responseInfo).getString("data");
                String state = JSONObject.parseObject(data).getString("delivery_state");
                if (StringUtils.equals(state, "0")) {
                    return new ProcessResult(ProcessResult.SUCCESS, "充值中");
                } else if (StringUtils.equals(state, "1")) {
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                }
            } else {
                String message = JSONObject.parseObject(responseInfo).getString("message");
                String mes = JSONObject.parseObject(message).getString("mes");
                return new ProcessResult(ProcessResult.FAIL, "提交失败,原因为："+mes);
            }
        } catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑原因=" + e.getMessage());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String partner_id = configJSONObject.getString("merchantId");
        String queryurl = configJSONObject.getString("queryurl") + partner_id;
        String order_id = channelOrder.getChannelOrderId();
        String version = configJSONObject.getString("version");
        int hours = (int) Math.floor(System.currentTimeMillis() / 1000 / 600) * 600;
        String sign_generate_time = Integer.toString(hours);
        String signBF = "order_id=" + order_id
                + "&partner_id=" + partner_id
                + "&sign_generate_time=" + sign_generate_time
                + "&version=" + version;
        String sign = DigestUtils.md5Hex(signBF);
        String Json = "{\"order_id\":\"" + order_id +
                "\",\"partner_id\":\"" + partner_id +
                "\",\"sign_generate_time\":\"" + sign_generate_time +
                "\",\"version\":\"" + version +
                "\",\"sign\":\"" + sign + "\"}";
        String param = encrypt(Json);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("param", param);
        String requestString = JSONObject.toJSONString(requestMap);
        try {
            logger.info("lakalajyk query request params:{}", JSONObject.toJSONString(Json));
            String responseBody = HttpClientUtils.invokePostString(queryurl, new StringEntity(requestString), "utf-8", 5000);
            String responseInfo = decrypt(responseBody);
            logger.info("lakalajyk query response params:{}", JSONObject.toJSONString(responseInfo));
            String res = JSONObject.parseObject(responseInfo).getString("res");
            if (res.equals("true")) {
                String data = JSONObject.parseObject(responseInfo).getString("data");
                String state = JSONObject.parseObject(data).getString("delivery_state");
                String voucher_no = JSONObject.parseObject(data).getString("voucher_no");
                if (StringUtils.equals(state, "0")) {
                    return new ProcessResult(ProcessResult.PROCESSING, "充值中");
                } else if (StringUtils.equals(state, "1")) {
                    channelOrder.setOutChannelOrderId(voucher_no);
                    return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
                } else {
                    return new ProcessResult(ProcessResult.FAIL, "充值失败");
                }
            } else {
                String message = JSONObject.parseObject(responseInfo).getString("message");
                String msg = JSONObject.parseObject(message).getString("msg");
                return new ProcessResult(ProcessResult.UNKOWN, msg);
            }
        } catch (Exception e) {
            logger.error("{}send error{}", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "查询失败");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("0", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.PROCESSING, "充值中");
        } else if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.FAIL, responseOrder.getResponseMsg());
        }
    }


}
