package com.recharge.service.recharge.iml;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.center.bean.TianMaoRechargeInfo;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.PromotionActivitySendawardbyidRequest;
import com.taobao.api.response.PromotionActivitySendawardbyidResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TianMaoServiceImpl extends AbsChannelRechargeService{

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ChannelService channelService;
    private String channelId = "100031";

    public static void main(String[] args) {
        new TianMaoServiceImpl().recharge( null ,null , null);
    }

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String requestUrl = configJSONObject.getString("rechargeUrl");
        String appKey = configJSONObject.getString("appKey");
        String appSecret = configJSONObject.getString("appSecret");
        String promotionBizAlias = configJSONObject.getString("promotionBizAlias");
        String sessionKey = configJSONObject.getString("sessionKey");


        TaobaoClient client = new DefaultTaobaoClient(requestUrl, appKey, appSecret);

        TianMaoRechargeInfo tianMaoRechargeInfo = (TianMaoRechargeInfo) rechargeOrderBean.getRechargeInfoObj(TianMaoRechargeInfo.class);
        ProductRelation productRelation = queryChannelProductId(rechargeOrderBean.getProductName(), channelId);
        String activityAlias = productRelation.getChannelProductId().split("_")[0];
        String exchangeItemId = productRelation.getChannelProductId().split("_")[1];
        String awardAlias = productRelation.getChannelProductId().split("_")[2];


        PromotionActivitySendawardbyidRequest req = new PromotionActivitySendawardbyidRequest();
        PromotionActivitySendawardbyidRequest.IsvAwardReqDto obj1 = new PromotionActivitySendawardbyidRequest.IsvAwardReqDto();
//        活动别名
        obj1.setActivityAlias(activityAlias);
//        奖品编号
        obj1.setExchangeItemId(Long.parseLong(exchangeItemId));

//        奖品标号
        obj1.setAwardAlias(awardAlias);
        obj1.setOutId(channelOrder.getChannelOrderId());

//        淘宝会员号
        obj1.setBuyerNick(tianMaoRechargeInfo.getBuyerNick());

        obj1.setExtParas("{\"ttid\",\"toabao\"}");


        obj1.setPromotionBizAlias(promotionBizAlias);

        obj1.setChannel("isv-channel");
        req.setIsvAwardReqDTO(obj1);

        PromotionActivitySendawardbyidResponse rsp =null;
        try {
            rsp = client.execute(req, sessionKey);
        } catch (ApiException e) {
            logger.error("invoke taobao client error" , e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交未知");
        }

        logger.info("tianmao invoke return {}" , JSONObject.toJSONString(rsp));
        PromotionActivitySendawardbyidResponse finalRsp = rsp;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    logger.error("InterruptedException" ,e);
                }
                ResponseOrder responseOrder = new ResponseOrder();
                responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                if(finalRsp.isSuccess() && finalRsp.getResult().getSuccess()){
                    responseOrder.setResponseCode("00");
                }else{
                    responseOrder.setResponseCode("01");
                    responseOrder.setResponseMsg(finalRsp.getResult().getDesc());
                }

                channelService.callBack(channelId,responseOrder);
            }
        }).start();



        if (rsp.isSuccess() && rsp.getResult().getSuccess()){
            return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
        }else{
            return new ProcessResult(ProcessResult.FAIL,rsp.getResult().getDesc());
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
