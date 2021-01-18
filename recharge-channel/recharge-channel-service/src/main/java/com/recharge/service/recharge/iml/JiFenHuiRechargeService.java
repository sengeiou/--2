package com.recharge.service.recharge.iml;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.FlowRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qi.cao on 2017/4/27.
 */
@Service
public class JiFenHuiRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String requestUrl = configJSONObject.getString("rechargeUrl");
        String agentId = configJSONObject.getString("agentId");
        String authCode = configJSONObject.getString("authCode");
        String authPwd = configJSONObject.getString("authPwd");

        FlowRechargeInfoBean flowRechargeInfoBean = (FlowRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(FlowRechargeInfoBean.class);

        ProductRelation productRelation  = queryChannelProductId(rechargeOrderBean.getProductName(),"100004");

        Map<String,String> requestMap = new HashMap<String,String>();
        requestMap.put("phone",flowRechargeInfoBean.getPhone());
        requestMap.put("pdid",productRelation.getChannelProductId());
//        这个金额随便写
        requestMap.put("amt","1");


        Map<String,String> headerMap = new HashMap<String,String>();
        headerMap.put("agentid",agentId);
        headerMap.put("authcode",authCode);
        headerMap.put("authpwd",authPwd);
        try {

            logger.info("orderId : {} ,send JiFenHui request param : {} . header param:{} ",rechargeOrderBean.getOrderId(),
                    JSONObject.toJSONString(requestMap),JSONObject.toJSONString(headerMap));

            String responseBody = HttpClientUtils.invokePostHttpWithHeader(requestUrl, requestMap, headerMap, "UTF-8",5000);

            logger.info("order Id : {} , JiFenHui response :{}",rechargeOrderBean.getOrderId(),responseBody);
            JSONObject responseJSONObject = JSON.parseObject(responseBody);
            String rtcode = responseJSONObject.getString("rtcode");
            String rtdesc = responseJSONObject.getString("rtdesc");
            String orderid = responseJSONObject.getString("orderid");

//            订单号由供货商生成。
            channelOrder.setOutChannelOrderId(orderid);
            if (StringUtils.equals("3000",rtcode)){
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else{
                return new ProcessResult(ProcessResult.FAIL,rtdesc);
            }

        } catch (ConnectTimeoutException connectException){
            logger.error("{} send error",rechargeOrderBean.getOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (Exception e) {
            logger.error("{} send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        return null;
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        return null;
    }
}
