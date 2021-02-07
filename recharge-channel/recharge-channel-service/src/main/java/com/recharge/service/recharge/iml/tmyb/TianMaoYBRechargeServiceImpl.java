package com.recharge.service.recharge.iml.tmyb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.center.bean.TianMaoYuanBaoRechargeInfo;
import com.recharge.common.utils.DateUtil;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.AliYuanBaoParam;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.UserPointInfo;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Administrator
 * @create 2021/2/5 16:54
 */
@Service
public class TianMaoYBRechargeServiceImpl extends AbsChannelRechargeService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ChannelService channelService;
    private String channelId = "100106";

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        TianMaoYuanBaoRechargeInfo tianMaoYuanBaoRechargeInfo = (TianMaoYuanBaoRechargeInfo) rechargeOrderBean.getRechargeInfoObj(TianMaoYuanBaoRechargeInfo.class);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        Map<String, Object> param = new HashMap<String, Object>();
        String method = configJSONObject.getString("method");
        String appKey = configJSONObject.getString("appKey");
        String v = configJSONObject.getString("v");
        String sign_method = configJSONObject.getString("sign_method");
        String user_type = configJSONObject.getString("user_type");
        String corp_id = configJSONObject.getString("corp_id");
        String isv_corp_id = configJSONObject.getString("isv_corp_id");
        String url = configJSONObject.getString("rechargeUrl");
        String appSecret = configJSONObject.getString("appSecret");

        String timestamp = DateUtil.convertDateToStr(new Date());
        //排序
        SortedMap<String, String> sortParams = new TreeMap<>();
        //公有参数
        sortParams.put("method", method);
        sortParams.put("app_key", appKey);
        sortParams.put("sign_method", sign_method);
        sortParams.put("timestamp", timestamp);
        sortParams.put("format", "xml");
        sortParams.put("v", v);
        //业务参数
        AliYuanBaoParam ywParam = new AliYuanBaoParam();
        List<UserPointInfo> users = new ArrayList<UserPointInfo>();
        UserPointInfo upi = new UserPointInfo();
        upi.setUser_type(user_type);
        upi.setTb_account(tianMaoYuanBaoRechargeInfo.getTbAccount());
        upi.setPoint(tianMaoYuanBaoRechargeInfo.getPoint().longValue());
        users.add(upi);
        ywParam.setCorp_id(corp_id);
        ywParam.setIsv_corp_id(isv_corp_id);
        ywParam.setTask_name("周年");
        ywParam.setBlessing("祝福语");
        ywParam.setUser_info_list(users);
        String param0 = JSON.toJSONString(ywParam);//URLEncoder.encode(JSON.toJSONString(ywParam), "UTF-8");
        sortParams.put("parm0", param0);
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : sortParams.entrySet()) {
            String str = entry.getKey() + entry.getValue();
            sb.append(str);
        }
        param.put("method", method);
        param.put("app_key", appKey);
        param.put("sign_method", "md5");
        param.put("format", "xml");
        param.put("timestamp", timestamp);
        param.put("v", "2.0");
        param.put("sign", DigestUtils.md5Hex(appSecret + sb.toString() + appSecret).toUpperCase());
        param.put("parm0", param0);
        System.out.println("参数=" + JSON.toJSONString(param));
        try {
            logger.info("TMYB下单接口发送的信息:{}",JSON.toJSONString(param));
            String s = HttpClientUtils.invokeGetHttpWithMap(url, param, "UTF-8", 5000);
            logger.info("TMYB下单接口接收的信息:{}",s);
            Document document = DocumentHelper.parseText(s);
            Element root = document.getRootElement();
            Element result = root.element("result");
            if (result != null) {
                String success = result.elementText("success");
                if (StringUtils.equals(success, "true")) {
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
                            responseOrder.setResponseCode("00");
                            channelService.callBack(channelId, responseOrder);
                        }
                    }).start();
                    return new ProcessResult(ProcessResult.SUCCESS, "提交成功");
                }else {
                    return new ProcessResult(ProcessResult.UNKOWN, "提交可疑：");
                }
            } else {
                String msg = root.elementText("msg");
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
                        responseOrder.setResponseCode("01");
                        channelService.callBack(channelId, responseOrder);
                    }
                }).start();
                return new ProcessResult(ProcessResult.FAIL, msg);
            }
        } catch (Exception e) {
            logger.info("TMYB下单接口报错的信息:{}",e.getMessage());
            return new ProcessResult(ProcessResult.UNKOWN, "提交可疑：" + e.getMessage());
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

    @Test
    void test() {
        Channel channel = new Channel();
        channel.setConfigInfo("{method:\"alibaba.tmall.eps.oa.point.send\",appKey:\"27612269\",v:\"2.0\",sign_method:\"md5\",user_type:\"taobao\",corp_id:\"ding72b46c3bf1fcbe2e35c2f4657eb6378f\",isv_corp_id:\"ding72b46c3bf1fcbe2e35c2f4657eb6378f\",rechargeUrl:\"http://gw.api.taobao.com/router/rest\",appSecret: \"9e6fa86935813f9654bfaf6b8fd09681\"}");
        ChannelOrder channelOrder = new ChannelOrder();
        ProcessResult recharge = recharge(channel, channelOrder, new RechargeOrderBean());
    }

    @Test
    void test1() {
        String s = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><alibaba_tmall_eps_oa_point_send_response><result><data><corp_id>ding72b46c3bf1fcbe2e35c2f4657eb6378f</corp_id><isv_corp_id>ding72b46c3bf1fcbe2e35c2f4657eb6378f</isv_corp_id><task_id>438971</task_id></data><success>true</success></result><request_id>iwvv5be4o1c1</request_id></alibaba_tmall_eps_oa_point_send_response><!--top011184081134.na62-->";
        String s1 = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><error_response><code>25</code><msg>Invalid signature</msg><request_id>exz2eejn5y85</request_id></error_response><!--top011023105124.center.na61-->";
        Document document = null;
        try {
            document = DocumentHelper.parseText(s);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        Element root = document.getRootElement();
        Element result = root.element("result");
        if (result != null) {
            String success = result.elementText("success");
            System.out.println(success);
        } else {
            String msg = root.elementText("msg");
            System.out.println(msg);
        }
    }
}
