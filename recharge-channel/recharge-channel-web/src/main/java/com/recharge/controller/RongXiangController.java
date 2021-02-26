package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.AiErBei;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.RongXiang;
import com.recharge.mapper.IChannelMapper;
import com.recharge.mapper.IChannelOrderMapper;
import com.recharge.service.ChannelService;
import com.recharge.utils.RongXiangDESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Administrator
 * @create 2021/1/21 14:05
 */
@Controller
@RequestMapping("/rongXiang")
public class RongXiangController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ChannelService channelService;
    @Autowired
    private IChannelMapper iChannelMapper;

    @Autowired
    private IChannelOrderMapper iChannelOrderMapper;

    private String channelId="100140";

    @RequestMapping("/callBack")
    @ResponseBody
    public String callBack(@RequestBody(required = false) RongXiang rongXiang) {
        Channel channel = iChannelMapper.selectByChannelId(channelId);
        String md5key = channel.getRemark2();
//        String md5key = "33463E774F316B74";
        String data = rongXiang.getData();
        String decrypt = RongXiangDESUtil.decrypt(data, md5key);
        JSONObject jsonObject = JSONObject.parseObject(decrypt.trim());
        String channelOrderIdMapping = jsonObject.getString("OrderNo");
        String retCode = jsonObject.getString("RetCode");
        ChannelOrder channelOrder = iChannelOrderMapper.selectByChannelOrderIdMapping(channelOrderIdMapping);
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
        responseOrder.setResponseCode(retCode);
        channelService.callBack(channelId, responseOrder);
        return "ok";
    }
}
