package com.recharge.service.recharge.iml.tmxtk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.center.bean.TianMaoRechargeInfo;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TmallPurchaseCardBuyRequest;
import com.taobao.api.response.TmallPurchaseCardBuyResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @create 2020/11/17 10:38
 */
@Service
public class TianMaoXTKServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ChannelService channelService;
    private String channelId = "100106";

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        //读取配置文件，获取对应的信息
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String appkey = configJSONObject.getString("appKey");
        String secret = configJSONObject.getString("appSecret");
        //创建淘宝客户端连接对象
        TaobaoClient client = new DefaultTaobaoClient(url, appkey, secret);
        //创建天猫超市享淘卡参数对象
        TmallPurchaseCardBuyRequest req = new TmallPurchaseCardBuyRequest();
        TmallPurchaseCardBuyRequest.CardBuyRequest cardBuyRequest = new TmallPurchaseCardBuyRequest.CardBuyRequest();
        TianMaoRechargeInfo tianMaoRechargeInfo = (TianMaoRechargeInfo) rechargeOrderBean.getRechargeInfoObj(TianMaoRechargeInfo.class);
        //设置外部订单ID(必填)
        cardBuyRequest.setOuterOrderId(channelOrder.getOrderId());
        //设置卡类型(1: 猫超卡，必填)
        cardBuyRequest.setCardType(1L);
        //设置面值,单位分(必填)
        cardBuyRequest.setParValue(Long.valueOf(NameToParValue(channelOrder.getProductName()))*100);
        //设置购卡数量，不能大于10000(必填)
        cardBuyRequest.setAmount(1L);
        //设置充值账号(选填)
        cardBuyRequest.setRechargeAccount(tianMaoRechargeInfo.getBuyerNick());
        //如果获取到的账户为空则返回FALL
        if (StringUtils.isEmpty(cardBuyRequest.getRechargeAccount())){
            return new ProcessResult(ProcessResult.FAIL, "淘宝账号为空");
        }
        //将cardBuyRequest写入到TmallPurchaseCardBuyRequest中
        req.setCardBuyReq(cardBuyRequest);
        TmallPurchaseCardBuyResponse rsp = null;
        try {
            logger.info("tianmaoXTK send recharge request param:{}",JSON.toJSONString(req));
            rsp = client.execute(req);
            logger.info("tianmaoXTK send recharge response param:{}",JSON.toJSONString(rsp));
        } catch (ApiException e) {
            logger.error("invoke TianMaoXTK client error", e);
            return new ProcessResult(ProcessResult.UNKOWN, "提交未知原因="+e.getErrMsg());
        }
        logger.info("TianMaoXTK invoke return {}", JSONObject.toJSONString(rsp));
        TmallPurchaseCardBuyResponse finalRsp = rsp;
        //天猫回调为同步回调，所以新开一个线程拿着同步返回的信息完成回调
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    logger.error("InterruptedException", e);
                }
                ResponseOrder responseOrder = new ResponseOrder();
                responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                if (finalRsp.isSuccess() && finalRsp.getResult().getSuccess()) {
                    responseOrder.setResponseCode("00");
                } else {
                    responseOrder.setResponseCode("01");
                    responseOrder.setResponseMsg(finalRsp.getResult().getResultCode());
                }
                channelService.callBack(channelId,responseOrder);
            }
        }).start();
        //根据同步返回的消息判断是否提交成功
        if (rsp.isSuccess() && rsp.getResult().getSuccess()) {
            return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
        } else {
            return new ProcessResult(ProcessResult.FAIL, rsp.getResult().getResultCode());
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        return new ProcessResult(ProcessResult.PROCESSING, "处理中");
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("00", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("01", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
        }
    }

    public String NameToParValue(String str) {
        str = str.trim();
        String str2 = "";
        if (str != null && !"".equals(str)) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                    str2 += str.charAt(i);
                }
            }
        }
        return str2;
    }
}
