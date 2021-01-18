package com.recharge.service.recharge.iml.video;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.center.bean.VideoRechargeInfoBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qi.cao
 */
@Service
public class MoBaoVideoServiceImpl extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {

        VideoRechargeInfoBean videoRechargeInfoBean = (VideoRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(VideoRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("url");
        String md5Key = configJSONObject.getString("md5Key");


        /**
         * 请求参数转换为JSON
         */
        Map<String, String> _rep = new HashMap<String, String>();
        _rep.put("transferId", channelOrder.getChannelOrderId());
        _rep.put("phoneNumber", videoRechargeInfoBean.getAccount());

        // 产品编码
        String pCode = queryChannelProductId(rechargeOrderBean.getProductName() , "100029").getChannelProductId();
//        String pCode = "1212zjydmgtv7d";
        _rep.put("productId", pCode);
        _rep.put("sign", DigestUtils.md5Hex("transferId"+_rep.get("transferId")+"phoneNumber"+_rep.get("phoneNumber")
                +"productId"+_rep.get("productId")+"ykfm_sign_key"+md5Key));


        String iCommString = JSONObject.toJSONString(_rep);

        /**
         * 发送post请求
         */
        String responseBody = null;
        try {
            logger.info("request param:{}",iCommString);
            responseBody = HttpClientUtils.invokePostString(url,new StringEntity(iCommString),"utf-8",5000);
            logger.info("response body :{}", responseBody);
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            String retCode=root.element("result").getStringValue();
            String mes=root.element("mes").getStringValue();
            if (StringUtils.equals("01",retCode)){
                return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
            }else if (StringUtils.equals("02",retCode)){
                return new ProcessResult(ProcessResult.FAIL,mes);
            }else{
                return new ProcessResult(ProcessResult.UNKOWN,mes);
            }
        } catch (Exception e) {
            logger.error("orderId :{} ,send unkown.", channelOrder.getOrderId() ,e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        return new ProcessResult(ProcessResult.PROCESSING,"处理中");
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("success" , responseOrder.getResponseCode())){
            return new ProcessResult(ProcessResult.SUCCESS,"充值成功");
        }else{
            return new ProcessResult(ProcessResult.FAIL,"充值失败");
        }
    }
}
