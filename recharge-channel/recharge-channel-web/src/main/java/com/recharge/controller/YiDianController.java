package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.*;
import com.recharge.domain.yidian.YiDianCallBackOrderDetail;
import com.recharge.domain.yidian.YiDianCallBackTelephone;
import com.recharge.mapper.IBuyCardMapper;
import com.recharge.mapper.IChannelMapper;
import com.recharge.mapper.IChannelOrderMapper;
import com.recharge.mapper.IRechargeOrderMapper;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.iml.MerchantCardServiceImpl;
import com.recharge.utils.YiDianDESUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2021/1/11 19:45
 */
@Controller
@RequestMapping("/yiDian")
public class YiDianController {
    @Autowired
    MerchantCardServiceImpl merchantCardServiceImpl;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private IRechargeOrderMapper rechargeOrderMapper;
    @Autowired
    private IChannelOrderMapper channelOrderMapper;
    
    private String channelId = "100130";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/callBack", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public void callBack(HttpServletRequest request, HttpServletResponse res) {
        logger.info("易点提卡接口回调接收到的信息{}", request);
        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String DESkey = configJSONObject.getString("DESkey");
        JSONObject jsonParam = this.getJSONParam(request);
        logger.info("易点生活提卡回调信息{}", jsonParam);
        int code = (int) jsonParam.get("code");
        String s = Integer.toString(code);
        ResponseOrder responseOrder = new ResponseOrder();
        String orderstatus;
        String clientorderno;
        if (StringUtils.equals("0", s)) {
            Object data = jsonParam.get("data");
            JSONObject jsonObject = JSONObject.parseObject(data.toString());
            String orderdetail = jsonObject.getString("orderdetail");
            orderstatus = jsonObject.getString("orderstatus");
            clientorderno = jsonObject.getString("clientorderno");

            String nowdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Date now = null;
            try {
                now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(nowdate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Calendar c = Calendar.getInstance();
            c.setTime(now);
            int day1 = c.get(Calendar.DATE);
            c.set(Calendar.DATE, day1 - 3);
            String start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
            ChannelOrder channelOrder = channelOrderMapper.selectByChannelOrderIdByTime(clientorderno,start);
            if(channelOrder!=null){
            	RechargeOrder rechargeOrder = rechargeOrderMapper.selectByChannleOrderIdOnrecent(channelOrder.getOrderId(), start);
	            if (!(rechargeOrder.getRechargeState() == 4 || rechargeOrder.getRechargeState() == 5)) {
	                String productId = rechargeOrder.getProductId();
	                String productName = rechargeOrder.getProductName();
	                String merchantId = rechargeOrder.getMerchantId();
	                JSONArray jsonArray = new JSONArray(JSON.parseArray(orderdetail));
	                List<Map<String, String>> cardInfos = new ArrayList<>();
	                List<YiDianCallBackOrderDetail> cardDTOS = null;
	                cardDTOS = JSONObject.parseArray(jsonArray.toJSONString(), YiDianCallBackOrderDetail.class);
	                for (YiDianCallBackOrderDetail cardDTO : cardDTOS) {
	                    Object codedetail = cardDTO.getCodedetail();
	                    String telephone = JSONObject.parseObject(JSONObject.toJSONString(codedetail)).getString(configJSONObject.getString("telephone"));
	                    JSONArray telephoneArray = new JSONArray(JSON.parseArray(telephone));
	                    List<YiDianCallBackTelephone> cards = null;
	                    cards = JSONObject.parseArray(telephoneArray.toJSONString(), YiDianCallBackTelephone.class);
	                    for (YiDianCallBackTelephone card : cards) {
	                        Map<String, String> infosMap = new HashMap<>();
	                        infosMap.put(BuyCardInfo.KEY_CARD_NO, YiDianDESUtils.decrypt(card.getBarcode(), DESkey));
	                        infosMap.put(BuyCardInfo.KEY_CARD_PWD, YiDianDESUtils.decrypt(card.getBarpwd(), DESkey));
	                        infosMap.put(BuyCardInfo.KEY_CARD_EXP_TIME, card.getDuedate());
	                        cardInfos.add(infosMap);
	                    }
	                }
	                List<PlatformCardInfo> platformCardInfos = cardInfos.stream().map(item -> {
	                    PlatformCardInfo cardInfo = new PlatformCardInfo();
	                    cardInfo.setCardNo(item.get(BuyCardInfo.KEY_CARD_NO));
	                    cardInfo.setCardPwd(item.get(BuyCardInfo.KEY_CARD_PWD));
	                    cardInfo.setCustomerId(merchantId);
	                    cardInfo.setOrderId(rechargeOrder.getOrderId());
	                    cardInfo.setProductId(productId);
	                    cardInfo.setProductName(productName);
	                    cardInfo.setSupId(channel.getChannelId());
	                    Date endDate = null;
	                    try {
	                        endDate = DateUtils.parseDate(item.get(BuyCardInfo.KEY_CARD_EXP_TIME), "yyyy-MM-dd hh:mm:ss");
	                    } catch (Exception e) {
	                    }
	                    cardInfo.setExpireTime(endDate);
	                    return cardInfo;
	                }).collect(Collectors.toList());
	                merchantCardServiceImpl.insertByBatch(platformCardInfos, rechargeOrder.getOrderId());
	            }
            }else{
            	logger.info("渠道订单号{}不存在..."+clientorderno);
            	return ;
            }
        } else {
            Object data = jsonParam.get("data");
            JSONObject jsonObject = JSONObject.parseObject(data.toString());
            orderstatus = jsonObject.getString("orderstatus");
            clientorderno = jsonObject.getString("clientorderno");
        }
        //更新订单信息
        responseOrder.setChannelOrderId(clientorderno);
        responseOrder.setResponseCode(orderstatus);
        channelService.callBack(channelId, responseOrder);
        try {
            res.setContentType("text/html;charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.print("ok");
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJSONParam(HttpServletRequest request) {
        JSONObject jsonParam = null;
        try {
            // 获取输入流
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

            // 数据写入Stringbuilder
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = streamReader.readLine()) != null) {
                sb.append(line);
            }
            jsonParam = JSONObject.parseObject(sb.toString());
            System.out.println(jsonParam.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonParam;
    }

}
