package com.recharge.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.recharge.common.utils.SmsService;
import com.recharge.mapper.IChannelMapper;
import com.recharge.mapper.IChannelOrderMapper;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.IOrderExportService;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.center.bean.RechargeStateBean;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.mapper.ISequenceMapper;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.utils.ConstantsUtils;
import com.recharge.utils.OrderState;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by qi.cao on 2016/5/17.
 */
@Service
public class ChannelService {

    @Autowired
    private IChannelMapper iChannelMapper;

    @Autowired
    private IChannelOrderMapper iChannelOrderMapper;

    @Resource(name = "taskMap")
    private Map<String,ThreadPoolTaskExecutor> taskExecutorMap;

    @Resource(name = "channelMap")
    private Map<String,AbsChannelRechargeService> rechargeServiceMap;

    @Autowired
    private IOrderExportService iOrderExportService;

    @Autowired
    private ISequenceMapper iSequenceMapper;

    @Value("${warnPhones}")
    private String warnPhones;

    @Value("${signName}")
    private String signName;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void rechargeBatch(Channel channel ,List<RechargeOrderBean> rechargeOrderBeanList){
        long startTime = System.currentTimeMillis();
        for (RechargeOrderBean rechargeOrderBean : rechargeOrderBeanList) {
            taskExecutorMap.get("rechargeTask").execute(new Runnable() {
                @Override
                public void run() {

                    AbsChannelRechargeService absChannelRechargeService = rechargeServiceMap.get(channel.getChannelId());

                    ChannelOrder channelOrder = new ChannelOrder();
                    channelOrder.setChannelOrderId("PS"+new SimpleDateFormat("yyyyMMddHHmm").format(new Date())+iSequenceMapper.selectNextval("SEQUENCE_CHANNEL_ID"));
                    channelOrder.setOrderId(rechargeOrderBean.getOrderId());
                    channelOrder.setChannelId(channel.getChannelId());
                    channelOrder.setChannelName(channel.getChannelName());
                    channelOrder.setState(OrderState.UN_SEND);

                    channelOrder.setProductId(rechargeOrderBean.getProductId());
                    channelOrder.setProductName(rechargeOrderBean.getProductName());
                    channelOrder.setOrderType(rechargeOrderBean.getOrderType());
                    channelOrder.setRechargeNumber(rechargeOrderBean.getRechargeNumber());
                    channelOrder.setCost(rechargeOrderBean.getCost());
                    iChannelOrderMapper.insertOrder(channelOrder);


                    ProcessResult processResult =null;
                    try {
                        processResult = absChannelRechargeService.recharge(channel, channelOrder ,rechargeOrderBean);
                        if (processResult.isSuccess()){
                            channelOrder.setState(OrderState.SENDED);
                        }else if(StringUtils.equals(ProcessResult.NO_BALANCE,processResult.getCode())){
//                            余额不足报警
//                            balanceWarn(channel.getChannelId());
                            channelOrder.setState(OrderState.FAIL);
                            channelOrder.setResponseMsg(processResult.getMsg());
                        }else if (StringUtils.equals(ProcessResult.FAIL,processResult.getCode())){
                            channelOrder.setState(OrderState.FAIL);
                            channelOrder.setResponseMsg(processResult.getMsg());
                        }else{
                            channelOrder.setState(OrderState.UNKOWN);
                            channelOrder.setResponseMsg(processResult.getMsg());
                        }
                    } catch (Exception e) {
                    	e.printStackTrace();
                        logger.info("订单发送渠道出现异常",e);
                        channelOrder.setState(OrderState.UNKOWN);
                        channelOrder.setResponseMsg("发送渠道异常="+e.getMessage());
                    }

                    resultProcess(channelOrder , ConstantsUtils.TYPE_RECHARGE);
                }
            });
        }

        logger.info("recharge cost {} ms" , System.currentTimeMillis()-startTime);
    }

//    public void balanceWarn(String channelId){
//        try {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("channelId" , "渠道"+channelId);
//            jsonObject.put("balance" , "0");
//            SmsService.sendSms(warnPhones ,signName,jsonObject.toJSONString(),"SMS_133969145");
//        } catch (ClientException e) {
//            logger.error("channel balance warn error");
//        }
//    }

    public void callBack(String channelId , ResponseOrder responseOrder){
        taskExecutorMap.get("callBackTask").execute(new Runnable() {
            @Override
            public void run() {

                try {
                    ProcessResult processResult = null;
                    try {
                        processResult = rechargeServiceMap.get(channelId).parseResponse(responseOrder);
                    } catch (Exception e) {
                        logger.error("orderId{},channelId:{},responseCode{},交易结果处理异常",responseOrder.getOrderId(),
                                responseOrder.getChannelOrderId(),responseOrder.getResponseCode(),e);
                        return ;
                    }

                    ChannelOrder channelOrder = null;

                /*
                * 如果 recharge 平台订单号为空，并且外部渠道订单号不为空，则用外部订单进行搜索，这个是为了兼容某些渠道
                * */
                    if (StringUtils.isEmpty(responseOrder.getChannelOrderId())&&StringUtils.isNotEmpty(responseOrder.getOutChannelOrderId())){
                        channelOrder = iChannelOrderMapper.selectByOutChannelOrderId(responseOrder.getOutChannelOrderId());
                    }else{
                        channelOrder = iChannelOrderMapper.selectByChannelOrderId(responseOrder.getChannelOrderId());
                    }


                    if (StringUtils.isNotEmpty(responseOrder.getOutChannelOrderId())){
                        channelOrder.setOutChannelOrderId(responseOrder.getOutChannelOrderId());
                    }

                    if (processResult.isSuccess()){
                        channelOrder.setState(OrderState.SUCCESS);
                    }else if (StringUtils.equals(processResult.getCode(),ProcessResult.FAIL)){
                        channelOrder.setState(OrderState.FAIL);
                    }else{
                        channelOrder.setState(OrderState.UNKOWN);
                    }
                    channelOrder.setResponseCode(responseOrder.getResponseCode());
                    channelOrder.setResponseMsg(responseOrder.getResponseMsg());

                    resultProcess(channelOrder , ConstantsUtils.TYPE_CALLBACK);
                } catch (Exception e) {
                    logger.error("call back error",e);
                }
            }
        });
    }

    public Channel queryChannelInfo (String channelId){
        return iChannelMapper.selectByChannelId(channelId);
    }

    public void resultProcess(ChannelOrder channelOrder ,String type){

        String[] oldStateArray = new String[]{OrderState.UN_SEND};
        if (StringUtils.equals(ConstantsUtils.TYPE_RECHARGE, type)){
            oldStateArray = new String[]{OrderState.UN_SEND};
        }else if(StringUtils.equals(ConstantsUtils.TYPE_CALLBACK , type)
                ||StringUtils.equals(ConstantsUtils.TYPE_QUERY , type)){
            oldStateArray = new String[]{OrderState.SENDED , OrderState.UNKOWN};
        }else{
            oldStateArray = null;
        }

        if(oldStateArray!=null &&
                iChannelOrderMapper.updateOrderState(channelOrder , oldStateArray) !=1){
            logger.info("channel update state fail,rechargeOderId {} ,channelOrderId {}, oldState:{} newState:{}",channelOrder.getOrderId() ,channelOrder.getChannelOrderId(), JSONObject.toJSONString(oldStateArray) , channelOrder.getState());
            return ;
        }

        if (StringUtils.isNotEmpty(channelOrder.getOutChannelOrderId())){
            iChannelOrderMapper.updateOutChannelId(channelOrder);
        }

        logger.info("resultProcess:{}", JSON.toJSONString(channelOrder));
        if (channelOrder.getState() == OrderState.SENDED){
            return;
        }

        RechargeOrderBean rechargeOrderBean = new RechargeOrderBean();
        rechargeOrderBean.setOrderId(channelOrder.getOrderId());
        rechargeOrderBean.setRechargeId(channelOrder.getChannelOrderId());

//        返回信息里包含余额二字,则返回充值失败
        if (StringUtils.isNotEmpty(channelOrder.getResponseMsg()) && channelOrder.getResponseMsg().indexOf("余额") != -1) {
            rechargeOrderBean.setRechargeFailedReason("充值失败");
        } else {
            rechargeOrderBean.setRechargeFailedReason(channelOrder.getResponseMsg());
        }

        rechargeOrderBean.setExchangeTraded(channelOrder.getOutChannelOrderId());
        rechargeOrderBean.setSupId(channelOrder.getChannelId());

        logger.info("channelOrderId:{},处理结果是:{}",channelOrder.getChannelOrderId(),channelOrder.getState());
        if (channelOrder.getState() == OrderState.UNKOWN){
            rechargeOrderBean.setRechargeState(RechargeStateBean.UNKOWN);
            iOrderExportService.orderToUnknown(rechargeOrderBean);
        }else if (channelOrder.getState() == OrderState.FAIL){
            rechargeOrderBean.setRechargeState(RechargeStateBean.FAIL);
            iOrderExportService.orderToRechargeFail(rechargeOrderBean);
        }else if (channelOrder.getState() == OrderState.SUCCESS){
            rechargeOrderBean.setRechargeState(RechargeStateBean.SUCCESS);
            iOrderExportService.orderToSuccess(rechargeOrderBean);
        }else{
            logger.error("系统异常，orderId:{},channelId:{}",channelOrder.getOrderId(),channelOrder.getChannelId());
        }
    }
}
