package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.BuyCardInfo;
import com.recharge.domain.Channel;
import com.recharge.domain.PlatformCardInfo;
import com.recharge.domain.RechargeOrder;
import com.recharge.domain.yidian.YiDianCallBackOrderDetail;
import com.recharge.domain.yidian.YiDianCallBackTelephone;
import com.recharge.mapper.IBuyCardMapper;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
    private ChannelService channelService;
    @Autowired
    MerchantCardServiceImpl merchantCardServiceImpl;

    @Autowired
    private IRechargeOrderMapper rechargeOrderMapper;
    @Autowired
    private IBuyCardMapper iBuyCardMapper;
    private String channelId = "100130";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/callBack", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String callBack(HttpServletRequest request) {
        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String DESkey = configJSONObject.getString("DESkey");
        JSONObject jsonParam = this.getJSONParam(request);
        logger.info("易点生活提卡回调信息{}",jsonParam);
        int code = (int) jsonParam.get("code");
        String s = Integer.toString(code);
        ResponseOrder responseOrder = new ResponseOrder();
        if (StringUtils.equals("0", s)) {
            Object data = jsonParam.get("data");
            JSONObject jsonObject = JSONObject.parseObject(data.toString());
            String orderdetail = jsonObject.getString("orderdetail");
            String orderstatus = jsonObject.getString("orderstatus");
            String clientorderno = jsonObject.getString("clientorderno");
            RechargeOrder rechargeOrder = rechargeOrderMapper.selectByChannleOrderId(clientorderno);
            String productId = rechargeOrder.getProductId();
            String productName = rechargeOrder.getProductName();
            String merchantId = rechargeOrder.getMerchantId();
            responseOrder.setChannelOrderId(clientorderno);
            responseOrder.setResponseCode(orderstatus);
            JSONArray jsonArray = new JSONArray(JSON.parseArray(orderdetail));
            BuyCardInfo buyCardInfo = new BuyCardInfo();
            List<Map<String, String>> cardInfos = new ArrayList<>();
            List<YiDianCallBackOrderDetail> cardDTOS = null;
            cardDTOS = JSONObject.parseArray(jsonArray.toJSONString(), YiDianCallBackOrderDetail.class);
            for (YiDianCallBackOrderDetail cardDTO : cardDTOS) {
                Object codedetail = cardDTO.getCodedetail();
                String telephone = JSONObject.parseObject(JSONObject.toJSONString(codedetail)).getString(jsonObject.getString("telephone"));
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
                cardInfo.setCardNo(item.get(BuyCardInfo.KEY_CARD_PWD));
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
            merchantCardServiceImpl.insertByBatch(platformCardInfos,rechargeOrder.getOrderId());
            channelService.callBack(channelId,responseOrder);
        }else {
            Object data = jsonParam.get("data");
            JSONObject jsonObject = JSONObject.parseObject(data.toString());
            String orderstatus = jsonObject.getString("orderstatus");
            String clientorderno = jsonObject.getString("clientorderno");
            responseOrder.setChannelOrderId(clientorderno);
            responseOrder.setResponseCode(orderstatus);
            channelService.callBack(channelId,responseOrder);
        }
        return "ok";
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
