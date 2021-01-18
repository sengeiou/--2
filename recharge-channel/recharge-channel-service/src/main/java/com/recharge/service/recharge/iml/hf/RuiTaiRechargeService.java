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

/**
 * @author qi.cao
 * 瑞泰上海话费充值
 */
@Service
public class RuiTaiRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");

        try {

            String productId = "1011"+StringUtils.leftPad(huaFeiRechargeInfoBean.getAmt().toString(),4,"0");
            String dtCreate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String requestUrl = url + "?userId="+userId+"&productId="+productId
                    +"&mobile="+huaFeiRechargeInfoBean.getPhone()+"&serialno="+channelOrder.getChannelOrderId()+"&dtCreate="+dtCreate
                    +"&sign="+DigestUtils.md5Hex(dtCreate+huaFeiRechargeInfoBean.getPhone()+productId
                    +channelOrder.getChannelOrderId()+userId+key);
            logger.info("send recharge request url:{}",requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl,"utf-8",5000);
            logger.info("send recharge response :{}",responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String code=root.elementText("code");
            String status = root.elementText("status");
            if (StringUtils.equals("success", status)){
                if (StringUtils.equals("00", code)){
                    channelOrder.setOutChannelOrderId(root.elementText("serialno"));
                    return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
                }else if (StringUtils.equals("31", code)
                        ||StringUtils.equals("61", code)
                        ||StringUtils.equals("50", code)
                        ||StringUtils.equals("51", code)){
                    return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
                }else{
                    return new ProcessResult(ProcessResult.FAIL,"提交失败");
                }

            }else {
                return new ProcessResult(ProcessResult.FAIL,"提交失败");
            }
        }catch (ConnectTimeoutException connectException){
            logger.error("{}send error",rechargeOrderBean.getOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (Exception e) {
            logger.error("{}send error",rechargeOrderBean.getOrderId(),e);
            return new ProcessResult(ProcessResult.SUCCESS,"提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String queryUrl = configJSONObject.getString("queryUrl");
        String userId = configJSONObject.getString("userId");
        String key = configJSONObject.getString("key");
        try {
            queryUrl = queryUrl +"?userId="+userId +"&serialno="+channelOrder.getChannelOrderId()+
                    "&sign="+DigestUtils.md5Hex(channelOrder.getChannelOrderId()+userId+key);
            logger.info("send query request queryUrl:{}",queryUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(queryUrl,"utf-8",5000);
            logger.info("send query response :{}",responseBody);

            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String code=root.elementText("code");
            String status = root.elementText("status");
            if (StringUtils.equals("success", status)){
                String orderStatus = root.element("data").elementText("orderStatus");
                if (StringUtils.equals("0",orderStatus)){
                    return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
                }else if(StringUtils.equals("1",orderStatus)
                        ||StringUtils.equals("2",orderStatus)){
                    return new ProcessResult(ProcessResult.PROCESSING,"处理中");
                }else if(StringUtils.equals("3",orderStatus)){
                    return new ProcessResult(ProcessResult.FAIL,"充值失败");
                }else{
                    return new ProcessResult(ProcessResult.PROCESSING,"处理中");
                }
            }else {
                return new ProcessResult(ProcessResult.PROCESSING,"处理中");
            }

        }catch (Exception e) {
            logger.error("{}send error",channelOrder.getChannelOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("0",responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
        }else if(StringUtils.equals("1",responseOrder.getResponseCode())
                ||StringUtils.equals("2",responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.PROCESSING,"处理中");
        }else if(StringUtils.equals("3",responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.FAIL,"充值失败");
        }else{
            return new ProcessResult(ProcessResult.UNKOWN,"充值可疑");
        }
    }
}
