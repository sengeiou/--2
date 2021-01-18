package com.recharge.service.recharge.iml.hf;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.HuaFeiRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.utils.desDemo;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author user
 * @create 2020/7/8 15:50
 */
@Service
public class LakalaRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        ProductRelation productRelation = new ProductRelation();
        productRelation = queryChannelProductId("全国话费"+huaFeiRechargeInfoBean.getAmt()+"元" , "100075");
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String partner_id =  configJSONObject.getString("partner_id");
        String url =  configJSONObject.getString("url");
        String order_id = channelOrder.getChannelOrderId();
        String phone =huaFeiRechargeInfoBean.getPhone();
        String num = "";
        String title = "";
        String version = "1.0";
        int hours = (int) Math.floor(System.currentTimeMillis() / 1000 / 600) * 600;
        String text = "item_id=" + productRelation.getChannelProductId()
                + "&num=" + num
                + "&order_id=" + order_id
                + "&partner_id=" + partner_id
                + "&phone=" + phone
                + "&sign_generate_time=" + hours
                + "&title=" + title
                + "&version=" + version;
        String sign = DigestUtils.md5Hex(text);
        String Json = "{\"partner_id\":\"" + partner_id + "\",\"sign_generate_time\":\"" + hours + "\",\"order_id\":\"" + order_id + "\",\"item_id\":\"" + productRelation.getChannelProductId() + "\",\"phone\":\"" + phone + "\",\"num\":\"" + num + "\",\"title\":\"" + title + "\",\"version\":\"" + version + "\",\"sign\":\"" + sign + "\"}";
        String param = desDemo.encrypt(Json);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("param", param);
        try {
            String requestString = JSONObject.toJSONString(requestMap);
            String responseBody = HttpClientUtils.invokePostString(url, new StringEntity(requestString), "utf-8", 5000);
            String responseInfo = desDemo.decrypt(responseBody);
            String code = JSONObject.parseObject(responseInfo).getString("res");
            if (code.equals("true")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            }else {
                return new ProcessResult(ProcessResult.FAIL, "提交失败");
            }
        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String partner_id = configJSONObject.getString("partner_id");
        String queryUrl = configJSONObject.getString("queryUrl");
        String version = "1.0";
        int hours = (int) Math.floor(System.currentTimeMillis() / 1000 / 600) * 600;
        String text = "order_id=" + channelOrder.getChannelOrderId()
                + "&partner_id=" + partner_id
                + "&sign_generate_time=" + hours
                + "&version=" + version;
        String sign = DigestUtils.md5Hex(text);
        String Json = "{\"order_id\":\"" + channelOrder.getChannelOrderId() + "\",\"partner_id\":\""+partner_id+"\",\"sign_generate_time\":\""+hours+"\",\"version\":\""+version+"\",\"sign\":\"" + sign + "\"}";
        String param = desDemo.encrypt(Json);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("param", param);
        try {
            String requestString = JSONObject.toJSONString(requestMap);
            logger.info("send lakala query request params:{}", JSONObject.toJSONString(Json));
            String responseBody = HttpClientUtils.invokePostString(queryUrl, new StringEntity(requestString), "utf-8", 5000);
            String responseInfo = desDemo.decrypt(responseBody);
            logger.info("send lakala query response params:{}", JSONObject.toJSONString(responseInfo));
            String data = JSONObject.parseObject(responseInfo).getString("data");
            String voucherList = JSONObject.parseObject(data).getString("voucherList");
            JSONArray jsonArray = new JSONArray(JSON.parseArray(voucherList));
            String delivery_state=null;
            String voucher_no=null;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                delivery_state = jsonObject.getString("delivery_state");
                voucher_no = jsonObject.getString("voucher_no");
            }
            if (delivery_state.equals("0")){
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            }else if (delivery_state.equals("1")){
                channelOrder.setOutChannelOrderId(voucher_no);
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            }else {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            }


        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }

    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }
    }

}
