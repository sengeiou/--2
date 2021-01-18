package com.recharge.utils; /**
 * java 3DES加密类
 * 加密模式：CBC
 * @author liuzehong@lakala.com
 *
 */

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
//import com.hjtx.common.Configure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.util.List;

public class desDemo {

    private static final String key = "3ce59702214d4b94"; // 加密key ,由拉卡拉提供
    private static final String iv = "9a91c7d0"; // 加密iv 由拉卡拉提供
    /**
     * 加密key值，由拉卡拉提供
     * @return
     */
    public static byte[] getSecretKey() {
        String newKey = desDemo.rightFillMethod(key,24);
        return newKey.getBytes();
    }

    /**
     * 补位至指定长度
     * @param str
     * @param j
     * @return
     */
    public static String rightFillMethod(String str,int j){
        if(j>str.length())
            j=j%str.length();

        for(int i=0;i<j;i++)
            str+="0";
        return str;
    }

    /**
     * 加密用到的IV值，有拉卡拉提供，比如64176527，要按照下面的方式分拆到byte数组中
     * @return
     */
    public static byte[] getIVBytes() {

        return iv.getBytes();
    }

    //加密方式和加密模式定义
    private static final String MCRYPT_TRIPLEDES = "DESede";
    private static final String TRANSFORMATION = "DESede/CBC/PKCS5Padding";

    /**
     * 解密函数
     * @param data 加密字符串
     * @return  解密后的字符串
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

    /**
     * 加解密测试
     * @param args
     */
    public static void main(String[] args) {
//        String json = "{\"url\":\"http://t-vouchers.51jfg.com/vouchers/getvoucher/p/hjtx-tst\",\"memberid\":\"2035\",\"userid\":\"1002404\",\"customerKey\":\"728f064b90\",\"memberKey\":\"e47690a564dfc60b9b10cf218c85adda\",\"queryUrl\":\"http://39.100.76.29:9090/interface/Order.Query\",\"queryBalanceUrl\":\"http://39.100.76.29:9090/interface/Order.Balance\",\"callback\":\"http://139.129.85.83:8082/newAnChangSZ/callBack\"}";
//        JSONObject configJSONObject = JSON.parseObject(json);
//        String order_id = "111222";
//        String item_id = "aimengsz1";
//        String phone = "18360969388";
//        String num = "1";
//        String title = "1";
//        int hours = (int) Math.floor(System.currentTimeMillis() / 1000 / 600)*600;
//
//        String sign = DigestUtils.md5Hex("order_id="+order_id+"&item_id="+item_id+"&phone="+phone+"&num="+num+"title"+title+"sign_generate_time"+hours);
//        String  Json = "{\"order_id\":\""+order_id+"\",\"item_id\":\""+item_id+"\",\"phone\":\""+phone+"\",\"num\":\""+num+"\",\"title\":\""+title+"\",\"sign\":\""+sign+"\"}";
//
//        String encryptStr = desDemo.encrypt(Json);
//        System.out.println(encryptStr+"111");
        String decryptStr = desDemo.decrypt("INFKaR3UacwuKX2ZLHCYBNKnzL3yftXnWszoIFb4soHXew77BiBkzJNHeeVPvOh/QToOdH+JxY/++EWq4oECVAbQVJjaCwGMIsRNUixy096aGEOoQPnOACO3QQ9OJSeEUhRUM+ec2pXeB0tscFhooS+nbVgQb+0o6BGQwoMgKLPOugTL+kXbYvs5+/2pxKs5h+67ULQG1mJitF1MQvt42a37TJ5eJjXr/n2s0/ocggTw4eSSA+aU4QuLxDcsYl1Q23VEC2+uZH7vONTv92P2GA==\n");
        String code = JSONObject.parseObject(decryptStr).getString("res");
        System.out.println(code);
        String data = JSONObject.parseObject(decryptStr).getString("data");
        String orderid = JSONObject.parseObject(data).getString("order_id");
        System.out.println(orderid);
        String voucherList = JSONObject.parseObject(data).getString("voucherList");
        System.out.println("voucherList:" + voucherList);
        List list = JSONArray.parseArray(voucherList);
        System.out.println(list);
        JSONArray jsonArray = new JSONArray(JSON.parseArray(voucherList));
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String voucher_num = jsonObject.getString("voucher_num");
            String voucher_no = jsonObject.getString("voucher_no");
            String voucher_pass = jsonObject.getString("voucher_pass");
            String url = jsonObject.getString("url");
            String end_time = jsonObject.getString("end_time");
            String price = jsonObject.getString("price");
            String delivery_state = jsonObject.getString("delivery_state");
            String show_type = jsonObject.getString("show_type");
            System.out.println("voucher_num"+voucher_num+"===voucher_no"+voucher_no+"===vourcher_pass"+voucher_pass+"===url"+url+"===end_time"+end_time+"===price"+price+"===delivery_state"+delivery_state+"===show_type"+show_type);
        }
        System.out.println(decryptStr + "222");

    }
}
