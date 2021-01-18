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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
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

/**中石化
 * @author Administrator
 * @create 2020/4/24 17:16
 */
@Service
public class OfpayJykRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url=configJSONObject.getString("url");
        String KeyStr=configJSONObject.getString("KeyStr");
        String userid=configJSONObject.getString("userid");
        String userpws=configJSONObject.getString("userpws");



        String chargeType=null;
        logger.info("request channelId:{} ,  request productName:{} ", channel.getChannelId(),rechargeOrderBean.getProductName());
        if (StringUtils.equals(channel.getChannelId(),"100058") && StringUtils.contains(rechargeOrderBean.getProductName(),"中石化")){
             chargeType="1";
             logger.info("request chargeType:{}",chargeType);
        }else  if (StringUtils.equals(channel.getChannelId(),"100058") && StringUtils.contains(rechargeOrderBean.getProductName(),"中石油")){
            chargeType="2";
            logger.info("request chargeType:{}",chargeType);
        }
        ProductRelation productRelation= queryChannelProductId(rechargeOrderBean.getProductName(), "100058");
        String cardid=productRelation.getChannelProductId();
        String cardnum="1";
        String sporder_id=channelOrder.getChannelOrderId();
        String sporder_time=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String game_userid=jykRechargeInfoBean.getAccount();
        String gasCardTel="";
        String gasCardName="";
        String invoiceFlag="";
        String passWorld= DigestUtils.md5Hex(userpws).toLowerCase();
        String packageBody=userid+passWorld+cardid+cardnum+sporder_id+sporder_time+game_userid+KeyStr;
        String md5_str= DigestUtils.md5Hex(packageBody).toUpperCase();
        String ret_url="";
        String version="6.0";



        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid",userid);
        requestMap.put("userpws",passWorld);
        requestMap.put("cardid",cardid);
        requestMap.put("cardnum",cardnum);
        requestMap.put("sporder_id",sporder_id);
        requestMap.put("sporder_time",sporder_time);
        requestMap.put("game_userid",game_userid);
        requestMap.put("chargeType",chargeType);
        requestMap.put("gasCardTel",gasCardTel);
        requestMap.put("gasCardName",gasCardName);
        requestMap.put("invoiceFlag",invoiceFlag);
        requestMap.put("md5_str",md5_str);
        requestMap.put("ret_url",ret_url);
        requestMap.put("version",version);


        try {
            logger.info("request param:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url, requestMap, "utf-8", 5000);
            logger.info("response body :{}", responseBody);


            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String retcode = root.elementText("retcode");
            String orderinfo = root.elementText("orderinfo");
            String err_msg = root.elementText("err_msg");

            if (StringUtils.equals(retcode,"1")){
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            }else {
                return new ProcessResult(ProcessResult.FAIL ,err_msg);
            }
        } catch (Exception e) {
            logger.error("ofpayJyk request error", e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }

    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryUrl = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userid");
        String userpws = configJSONObject.getString("userpws");
        String KeyStr = configJSONObject.getString("KeyStr");
        String sporder_id=channelOrder.getChannelOrderId();
        String passWorld=DigestUtils.md5Hex(userpws).toLowerCase();
        String resource=userId+passWorld+sporder_id+KeyStr;
        String md5_str= DigestUtils.md5Hex(resource).toUpperCase();
        String version="6.0";

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userid",userId);
        requestMap.put("userpws",passWorld);
        requestMap.put("sporder_id",sporder_id);
        requestMap.put("md5_str",md5_str);
        requestMap.put("version",version);

        try {
            logger.info("{},send query param:{}", channelOrder.getOrderId(), JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, configJSONObject.getString("encoding"));
            logger.info("{},query responseBody:{}", channelOrder.getOrderId(), responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String game_state = root.elementText("game_state");
            if (StringUtils.equals("1", game_state)) {
                return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
            } else if (StringUtils.equals("0", game_state)) {
                return new ProcessResult(ProcessResult.PROCESSING, "处理中");
            } else {
                return new ProcessResult(ProcessResult.PROCESSING, "充值失败");
            }
        } catch (Exception e) {
            logger.error("{}send error", channelOrder.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑");
        }

    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        String tranState = responseOrder.getResponseCode();
        if (StringUtils.equals("1", tranState)) {
            return new ProcessResult(ProcessResult.SUCCESS, "交易成功");
        } else if (StringUtils.equals("9", tranState)) {
            return new ProcessResult(ProcessResult.FAIL, responseOrder.getResponseMsg());
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "未知返回码");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        String queryUrl = configJSONObject.getString("queryBalanceUrl");
        String userid = configJSONObject.getString("userid");
        String userpws = configJSONObject.getString("userpws");
        String version="6.0";
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("userid",userid);
        requestMap.put("userpws",DigestUtils.md5Hex(userpws).toLowerCase());
        requestMap.put("version",version);
        try {
            logger.info("send queryBalance request params:{}", JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl, requestMap, "utf-8", 5000);
            logger.info("send queryBalance response :{}", responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String balance = root.elementText("oufeiBalance");

            return new BigDecimal(balance);
        } catch (Exception e) {
            logger.error("{}send error", e);
            return BigDecimal.ZERO;
        }
    }
}
