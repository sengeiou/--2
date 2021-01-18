package com.recharge.service.recharge.iml.hf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qianmi.open.api.ApiException;
import com.qianmi.open.api.DefaultOpenClient;
import com.qianmi.open.api.OpenClient;
import com.qianmi.open.api.request.BmOrderCustomGetRequest;
import com.qianmi.open.api.request.BmRechargeMobilePayBillRequest;
import com.qianmi.open.api.request.FinanceGetAcctInfoRequest;
import com.qianmi.open.api.response.BmOrderCustomGetResponse;
import com.qianmi.open.api.response.BmRechargeMobilePayBillResponse;
import com.qianmi.open.api.response.FinanceGetAcctInfoResponse;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.HuaFeiRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author qi.cao
 */
@Service
public class LiFangHfRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean)rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String rechargeUrl =  configJSONObject.getString("url");
        String appSecret = configJSONObject.getString("appSecret");
        String accessToken =  configJSONObject.getString("accessToken");
        String callBack =  configJSONObject.getString("callBack");
        OpenClient client = new DefaultOpenClient(rechargeUrl, appSecret);

        BmRechargeMobilePayBillRequest req = new BmRechargeMobilePayBillRequest();
        req.setCallback(callBack);
        req.setMobileNo(huaFeiRechargeInfoBean.getPhone());
        req.setRechargeAmount(huaFeiRechargeInfoBean.getAmt().toString());
        req.setOuterTid(channelOrder.getChannelOrderId());
        req.setTimestamp(new Date().getTime());
        try {
            logger.info("lifang recharge request param:{}" , JSONObject.toJSONString(req));
            BmRechargeMobilePayBillResponse response = client.execute(req, accessToken);
            logger.info("lifang recharge response body :{}" , JSONObject.toJSONString(response));

            if (response.getOrderDetailInfo() == null
                    || StringUtils.equals(response.getOrderDetailInfo().getRechargeState() ,"9")){
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
        String queryUrl =  configJSONObject.getString("url");
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
        //  订单充值状态：1（充值成功） 、9（充值失败）
        if (StringUtils.equals("1", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("9", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        }else {
            return new ProcessResult(ProcessResult.UNKOWN, "状态可疑");
        }
    }


    @Override
    public BigDecimal balanceQuery(Channel channel) {

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String queryUrl =  configJSONObject.getString("url");
        String appSecret = configJSONObject.getString("appSecret");
        String accessToken =  configJSONObject.getString("accessToken");
        OpenClient client = new DefaultOpenClient(queryUrl, appSecret);
        FinanceGetAcctInfoRequest req = new FinanceGetAcctInfoRequest();
        try {
            logger.info("liFang send queryBalance request ");
            FinanceGetAcctInfoResponse response = client.execute(req, accessToken);
            logger.info("liFang send queryBalance response :{}", JSONObject.toJSONString(response));

            return new BigDecimal(response.getAcctInfo().getBalance());

        } catch (Exception e) {
            logger.error("send error", e);
            return BigDecimal.ZERO;
        }
    }


}
