package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.*;
import com.recharge.mapper.IChannelMapper;
import com.recharge.mapper.IChannelOrderSupRelationMapper;
import com.recharge.service.ChannelService;
import com.recharge.utils.RongXiangDESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Administrator
 * @create 2021/1/21 14:05
 */
@Controller
@RequestMapping("/wuYouGou")
public class WuYouGouController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ChannelService channelService;
    @Autowired
    private IChannelMapper iChannelMapper;

    @Autowired
    private IChannelOrderSupRelationMapper IChannelOrderSupRelationMapper;

    private String channelId="100140";

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(@RequestBody(required = false) RongXiang rongXiang) {
        logger.info("无忧购 callback解密前 :{}", JSON.toJSONString(rongXiang));
        Channel channel = iChannelMapper.selectByChannelId(channelId);
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String ChannelID = configJSONObject.getString("ChannelID");
        String User = configJSONObject.getString("User");
        String BusiType = "0202";
        String md5key = channel.getRemark2();
        String data = rongXiang.getData();
        String decrypt = RongXiangDESUtil.decrypt(data, md5key);
        JSONObject jsonObject = JSONObject.parseObject(decrypt.trim());
        logger.info("无忧购 callback解密后 :{}", JSON.toJSONString(jsonObject));
        String channelOrderIdMapping = jsonObject.getString("OrderNo");
        String retCode = jsonObject.getString("RetCode");
        ChannelOrderSupRelation channelOrderSupRelation = IChannelOrderSupRelationMapper.selectBySupOrderId(channelOrderIdMapping);
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(channelOrderSupRelation.getChannelOrderId());
        responseOrder.setResponseCode(retCode);
        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, String> Date = new LinkedHashMap<>();
        Date.put("OrderNo",channelOrderIdMapping);
        String dataString = JSONObject.toJSONString(Date);
        String dataDES = RongXiangDESUtil.encryptToDES(dataString, md5key);
        map.put("ChannelID", ChannelID);
        map.put("User", User);
        map.put("BusiType", BusiType);
        map.put("Data", dataDES);
        channelService.callBack(channelId, responseOrder);
        return JSONObject.toJSONString(map);
    }
}
