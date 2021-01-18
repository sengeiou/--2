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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author user
 * @create 2020/7/15 14:28
 */
@Service
public class XunYinRechargeService extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean){
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String key = configJSONObject.getString("key");
        String url = configJSONObject.getString("url");
        //商户编号
        String cpid = configJSONObject.getString("cpid");
        //商品编号
        String gamegoodid = "qg";
        //时间戳
        String createtime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        //充值号码
        String account = huaFeiRechargeInfoBean.getPhone();
        //商户订单号
        String orderid = channelOrder.getChannelOrderId();
        //购买数量
        String buynum = "1";
        //回调地址
        String returnurl =configJSONObject.getString("returnurl");
        //面值
        String buyvalue =huaFeiRechargeInfoBean.getAmt().toString();
        String sign = DigestUtils.md5Hex("cpid="+cpid+"&gamegoodid="+gamegoodid+"&createtime="+createtime+"&account="+account+"&orderid="+orderid+"&buynum="+buynum+key).toLowerCase();
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("cpid",cpid);
        requestMap.put("gamegoodid",gamegoodid);
        requestMap.put("createtime",createtime);
        requestMap.put("account",account);
        requestMap.put("orderid",orderid);
        requestMap.put("buynum",buynum);
        requestMap.put("returnurl",returnurl);
        requestMap.put("buyvalue",buyvalue);
        requestMap.put("sign",sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url,requestMap, "utf-8", 5000);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String Code = root.elementText("Code");
            if (Code.equals("0000")) {
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
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
        String cpid = configJSONObject.getString("cpid");
        String queryUrl = configJSONObject.getString("queryUrl");
        String OrderID =channelOrder.getChannelOrderId();
        String key = configJSONObject.getString("key");
        String sign = DigestUtils.md5Hex(cpid+OrderID+key).toLowerCase();
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("cpid",cpid);
        requestMap.put("OrderID",OrderID);
        requestMap.put("sign",sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl,requestMap, "utf-8", 5000);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String state = root.elementText("state");
            if (StringUtils.equals(state, "8888")) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            }  else if (StringUtils.equals(state, "0000")) {
                return new ProcessResult(ProcessResult.PROCESSING, "充值中");
            } else {
                return new ProcessResult(ProcessResult.FAIL, "充值失败");
            }
        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getChannelOrderId(), e);
            return new ProcessResult(ProcessResult.PROCESSING, "提交可疑");
        }

    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("SUCCESS", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("queryBalanceUrl");
        String cpid =configJSONObject.getString("cpid");
        String key =configJSONObject.getString("key");
        String sign = DigestUtils.md5Hex(cpid+key).toLowerCase();
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("cpid",cpid);
        requestMap.put("sign",sign);
        try {
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String balance = root.elementText("balance");
            return new BigDecimal(balance);
        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }

    }

}
