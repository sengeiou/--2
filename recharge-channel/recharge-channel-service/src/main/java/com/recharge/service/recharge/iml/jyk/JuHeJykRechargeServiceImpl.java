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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class JuHeJykRechargeServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        JykRechargeInfoBean jykRechargeInfoBean = (JykRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(JykRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String url = configJSONObject.getString("url");
        String openId = configJSONObject.getString("openId");
        String apiKey = configJSONObject.getString("apiKey");

        ProductRelation productRelation = queryChannelProductId(rechargeOrderBean.getProductName() , "100013");

        /*10000(中石化50元加油卡)[暂不支持]
10001(中石化100元加油卡)
10002(中石化200元加油卡)
10003(中石化500元加油卡)
10004(中石化1000元加油卡)
10007(中石化任意金额充值)[暂不支持]
10008(中石油任意金额充值))*/
        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("proid",productRelation.getChannelProductId());
        requestMap.put("cardnum","1");
        requestMap.put("orderid",channelOrder.getChannelOrderId());
        requestMap.put("game_userid",jykRechargeInfoBean.getAccount());
        requestMap.put("gasCardTel","18900000000");
        requestMap.put("key",apiKey);
        requestMap.put("sign",DigestUtils.md5Hex(openId
                +apiKey+requestMap.get("proid")+"1" +requestMap.get("game_userid")+requestMap.get("orderid")).toLowerCase());
        try {
            logger.info("request param:{}",JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(url ,requestMap,"utf-8");
            logger.info("response body :{}", responseBody);

//            {
//                "reason": "提交充值成功",
//                    "result": {
//                "cardnum": "1", /*充值数量*/
//                        "ordercash": "95.5", /*进货价格*/
//                        "cardname": "全国加油卡", /*充值名称*/
//                        "sporder_id": "S20141125221812330", /*商家订单号*/
//                        "game_userid": "100011320000991****", /*加油卡卡号*/
//                        "game_state": "0", /*充值状态:0充值中 1成功 9撤销，刚提交成功的单子状态均为充值中*/
//                        "uorderid": "S2014111111115" /*商户自定的订单号*/
//            },
//                "error_code": 0
//            }

            JSONObject jsonObject = JSON.parseObject(responseBody);
            if (StringUtils.equals(jsonObject.getString("error_code"),"0")){
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else if (StringUtils.equals(jsonObject.getString("error_code"),"10014")
                    || StringUtils.equals(jsonObject.getString("error_code"),"208710")){
                return new ProcessResult(ProcessResult.UNKOWN,"系统异常，提交可疑");
            }else{
                return new ProcessResult(ProcessResult.FAIL,"提交失败");
            }
        } catch (Exception e) {
            logger.error("juhe request error",e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());


        String queryUrl = configJSONObject.getString("queryUrl");
        String apiKey = configJSONObject.getString("apiKey");

        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("orderid",channelOrder.getChannelOrderId());
        requestMap.put("key",apiKey);
        try {
            logger.info("request param:{}",JSONObject.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(queryUrl ,requestMap,"utf-8");
            logger.info("response body :{}", responseBody);

//            {
//                "reason": "查询成功",
//                    "result": {
//                "cardname": "全国 中石化加油 固定面值加油卡 直充100元",/*商品名称*/
//                        "game_userid": "1000119000002494353",/*加油卡卡号*/
//                        "uordercash": "100.000",/*订单消耗金额*/
//                        "sporder_id": "S17070400085272603302001",/*聚合订单号*/
//                        "game_state": "9",/*状态 1:成功 9:失败 0：充值中*/
//                        "err_msg": "只能给主卡且卡状态正常的加油卡充值"/*描述，订单失败时返回失败原因*/
//            },
//                "error_code": 0
//            }
            JSONObject jsonObject = JSON.parseObject(responseBody);
            if (StringUtils.equals(jsonObject.getString("error_code"), "0")){
                JSONObject result = jsonObject.getJSONObject("result");
                String juHeOrderId = result.getString("sporder_id");
                String gameState = result.getString("game_state");
                String errMsg = result.getString("err_msg");
                channelOrder.setOutChannelOrderId(juHeOrderId);
                if (StringUtils.equals(gameState , "1")){
                    return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
                }else if (StringUtils.equals(gameState , "9")){
                    return new ProcessResult(ProcessResult.FAIL,errMsg);
                }else if (StringUtils.equals(gameState , "0")){
                    return new ProcessResult(ProcessResult.PROCESSING,"充值中");
                }else{
                    return new ProcessResult(ProcessResult.UNKOWN,"查询可疑");
                }
            }else{
                return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
            }
        } catch (Exception e) {
            logger.error("juhe request error",e);
            return new ProcessResult(ProcessResult.PROCESSING,"查询异常,充值中");
        }
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals(responseOrder.getResponseCode(),"1")){
            return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
        }else if(StringUtils.equals(responseOrder.getResponseCode(),"9")){
            return new ProcessResult(ProcessResult.FAIL,responseOrder.getResponseMsg());
        }else{
            return new ProcessResult(ProcessResult.UNKOWN,responseOrder.getResponseMsg());
        }
    }
}
