package com.recharge.service.recharge.iml.yx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianmi.open.api.ApiException;
import com.qianmi.open.api.DefaultOpenClient;
import com.qianmi.open.api.OpenClient;
import com.qianmi.open.api.request.BmDirectRechargeGamePayBillRequest;
import com.qianmi.open.api.request.BmOrderCustomGetRequest;
import com.qianmi.open.api.response.BmDirectRechargeGamePayBillResponse;
import com.qianmi.open.api.response.BmOrderCustomGetResponse;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.GameRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author qi.cao
 */
@Service
public class LiFangGameRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        Channel channel = new Channel();
        channel.setConfigInfo("{\"rechargeUrl\":\"http://api.bm001.com/api\",\"queryUrl\":\"http://api.bm001.com/api\",\"appSecret\":\"uUJxCLDdKnCLebaBfjt4La58CuZRhhhn\",\"accessToken\":\"96f4068940fc4acea5bd94e09957dd13\",\"callBackUrl\":\"\"}");
        ChannelOrder channelOrder = new ChannelOrder();
        channelOrder.setChannelOrderId("PS201703221321003");
        channelOrder.setOrderId("R00000000003");
        RechargeOrderBean rechargeOrderBean= new RechargeOrderBean();
        rechargeOrderBean.setOrderId("R00000000003");
        GameRechargeInfoBean gameRechargeInfoBean = new GameRechargeInfoBean();
        gameRechargeInfoBean.setGameId("389063097");
        gameRechargeInfoBean.setIp("218.75.123.99");
        gameRechargeInfoBean.setNumber(new BigDecimal(1));
        rechargeOrderBean.setRechargeInfoBeanObj(gameRechargeInfoBean);

        LiFangGameRechargeServiceImpl jianGuoRechargeService = new LiFangGameRechargeServiceImpl();
        jianGuoRechargeService.query(channel,channelOrder);
    }

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        GameRechargeInfoBean gameRechargeInfoBean = (GameRechargeInfoBean)rechargeOrderBean.getRechargeInfoObj(GameRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String rechargeUrl =  configJSONObject.getString("rechargeUrl");
        String appSecret = configJSONObject.getString("appSecret");
        String accessToken =  configJSONObject.getString("accessToken");
        String callBackUrl = configJSONObject.getString("callBackUrl");

        OpenClient client = new DefaultOpenClient(rechargeUrl, appSecret);

        ProductRelation productRelation  = queryChannelProductId(rechargeOrderBean.getProductName(),"100016");

        BmDirectRechargeGamePayBillRequest req = new BmDirectRechargeGamePayBillRequest();
        req.setItemId(productRelation.getChannelProductId());
        req.setItemNum( gameRechargeInfoBean.getNumber().toString());
        req.setRechargeAccount(gameRechargeInfoBean.getGameId());
        req.setCallback(callBackUrl);
        req.setOuterTid(channelOrder.getChannelOrderId());
        req.setRechargeIp(gameRechargeInfoBean.getIp());
        req.setTimestamp(System.currentTimeMillis());
        try {
            logger.info("lifang request param:{}" , JSONObject.toJSONString(req));
            BmDirectRechargeGamePayBillResponse response = client.execute(req, accessToken);
            logger.info("lifang response body :{}" , JSONObject.toJSONString(response));

            if (response.getOrderDetailInfo() == null
            ||StringUtils.equals(response.getOrderDetailInfo().getRechargeState() ,"9")){
                return new ProcessResult(ProcessResult.FAIL,response.getSubMsg());
            }else{
                return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
            }
        } catch (ApiException e) {
            logger.error("invoke lifang recharge api error" , e);
            return new ProcessResult(ProcessResult.UNKOWN,"交易可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryUrl =  configJSONObject.getString("queryUrl");
        String appSecret = configJSONObject.getString("appSecret");
        String accessToken =  configJSONObject.getString("accessToken");
        OpenClient client = new DefaultOpenClient(queryUrl, appSecret);
        BmOrderCustomGetRequest req = new BmOrderCustomGetRequest();
        req.setOuterTid(channelOrder.getChannelOrderId());
        try {
            logger.info("lifang query request param:{}" , JSONObject.toJSONString(req));
            BmOrderCustomGetResponse response = client.execute(req, accessToken);
            logger.info("lifang query response body :{}" , JSONObject.toJSONString(response));
            if (response.getOrderDetailInfo() == null
            ||StringUtils.equals(response.getOrderDetailInfo().getRechargeState(), "9")){
                return new ProcessResult(ProcessResult.FAIL,response.getSubMsg());
            }else if (StringUtils.equals(response.getOrderDetailInfo().getRechargeState(), "1")){
                return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
            }else if (StringUtils.equals(response.getOrderDetailInfo().getRechargeState(), "0")){
                return new ProcessResult(ProcessResult.PROCESSING,"处理中");
            }else{
                return new ProcessResult(ProcessResult.UNKOWN,"交易可疑");
            }
        } catch (ApiException e) {
            logger.error("invoke lifang query api error");
            return new ProcessResult(ProcessResult.UNKOWN,"交易可疑");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode() ,"1")){
            return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
        }else if(StringUtils.equals(responseOrder.getResponseCode() ,"9")){
            return new ProcessResult(ProcessResult.FAIL,"充值失败");
        }else {
            return new ProcessResult(ProcessResult.UNKOWN,"交易可疑");
        }
    }
}
