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
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class FuHanRechargeService extends AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        HuaFeiRechargeInfoBean huaFeiRechargeInfoBean = (HuaFeiRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(HuaFeiRechargeInfoBean.class);

        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

//      请求地址：http://120.77.214.165:12345/hxpay/order.do
        String url = configJSONObject.getString("url");
        String cid = configJSONObject.getString("cid");
        String key = configJSONObject.getString("key");

        StringBuffer productName = new StringBuffer();
        String[] arr = new String[]{"电信", "移动", "联通"};
        for (String s : arr) {
            if (rechargeOrderBean.getProductName().indexOf(s) != -1) {
                productName.append(rechargeOrderBean.getProductName().substring(rechargeOrderBean.getProductName().indexOf(s) + 2)).append("话费");
                break;
            }
        }

//        String pid = "3001";
        String pid = queryChannelProductId(productName.toString(), "100047").getChannelProductId();
        String phone = huaFeiRechargeInfoBean.getPhone();
        String sign = DigestUtils.md5Hex("cid=" + cid + "&pid=" + pid + "&phone=" + phone + "&key=" + key);

        String requestUrl = url + "?cid=" + cid + "&pid=" + pid + "&phone=" + phone + "&sign=" + sign;

        try {
            logger.info("send recharge request params:{}", requestUrl);
            String responseBody = HttpClientUtils.invokeGetHttp(requestUrl, "utf-8", 5000);
            logger.info("send recharge response :{}", responseBody);

            String code = JSONObject.parseObject(responseBody).getString("retCode");
            String msg = JSONObject.parseObject(responseBody).getString("retMsg");
            if (StringUtils.equals(code, "8888")) {
                String order = JSONObject.parseObject(responseBody).getString("order");
                channelOrder.setOutChannelOrderId(order);
                return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
            } else {
                return new ProcessResult(ProcessResult.FAIL, msg);
            }

        } catch (ConnectTimeoutException connectException) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), connectException);
            return new ProcessResult(ProcessResult.FAIL, "提交失败");
        } catch (Exception e) {
            logger.error("{}send error", rechargeOrderBean.getOrderId(), e);
            return new ProcessResult(ProcessResult.SUCCESS, "提交可疑");
        }
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        return new ProcessResult(ProcessResult.PROCESSING, "处理中");
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
//       成功status =2,失败status =3
        if (StringUtils.equals("2", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("3", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
        }
    }

    public static void main(String[] args) {
        FuHanRechargeService fuHanRechargeService = new FuHanRechargeService();

        Channel channel = new Channel();
        channel.setChannelId("100047");
        channel.setConfigInfo("{\"url\":\"http://120.77.214.165:12345/hxpay/order.do\",\"cid\":\"1220\",\"key\":\"aa0210ba66ef40248fb196f9754854bd\"}");

        ChannelOrder channelOrder = new ChannelOrder();
        channelOrder.setChannelOrderId("PS31566055263330102");

        RechargeOrderBean rechargeOrderBean = new RechargeOrderBean<>();
        HuaFeiRechargeInfoBean rechargeInfoBean = new HuaFeiRechargeInfoBean();
        rechargeInfoBean.setPhone("13951754240");
        rechargeInfoBean.setAmt(new BigDecimal(20));
        rechargeOrderBean.setProductName("江苏移动20元");
        rechargeOrderBean.setRechargeInfoBeanObj(rechargeInfoBean);

        ProcessResult recharge = fuHanRechargeService.recharge(channel, channelOrder, rechargeOrderBean);

        System.out.println(recharge.getCode() + ":" + recharge.getMsg());
    }

}
