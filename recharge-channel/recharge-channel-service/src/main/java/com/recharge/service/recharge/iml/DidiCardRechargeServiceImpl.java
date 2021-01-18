package com.recharge.service.recharge.iml;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.CardToRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DidiCardRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        CardToRechargeInfoBean cardToRechargeInfoBean = (CardToRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(CardToRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String channelCode = configJSONObject.getString("channel");

        String requestUrl = url + "?code="+cardToRechargeInfoBean.getCode() +"&phone="+cardToRechargeInfoBean.getAccount() + "&channel="+ channelCode;
        try {
            logger.info("send recharge requestUrl:{}",requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl,"utf-8",10000);
            logger.info("send recharge response :{}",responseBody);
            String code = JSONObject.parseObject(responseBody).getString("code");
            String msg = JSONObject.parseObject(responseBody).getString("msg");
            if (StringUtils.equals( code, "00")){

                ResponseOrder responseOrder = new ResponseOrder();
                responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                responseOrder.setResponseCode("00");
                responseOrder.setResponseMsg("充值成功");
                callBack(responseOrder , "100039");
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else{
                return new ProcessResult(ProcessResult.FAIL,msg);
            }

        }catch (ConnectTimeoutException connectException){
            logger.error("{}send error",rechargeOrderBean.getOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (Exception e) {
            logger.error("{}send error",rechargeOrderBean.getOrderId(),e);
            return new ProcessResult(ProcessResult.PROCESSING,"提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        return new ProcessResult(ProcessResult.PROCESSING,"处理中");
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("00" , responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
        }else if (StringUtils.equals("01" , responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.FAIL,"充值失败");
        }else{
            return new ProcessResult(ProcessResult.UNKOWN,"结果可疑");
        }
    }
}
