package com.recharge.controller;

import com.recharge.domain.Channel;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/balance")
public class BalanceController {

    @Autowired
    private ChannelService channelService;

    @Resource(name = "channelMap")
    private Map<String, AbsChannelRechargeService> rechargeServiceMap;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/query")
    @ResponseBody
    public Map<String, Object> query(String channelId) {
        Map<String, Object> map = new HashMap<>();

        Channel channel = channelService.queryChannelInfo(channelId);
        if (channel == null) {
            map.put("code", 0);
            map.put("msg", "channel not exist");
            return map;
        }

        AbsChannelRechargeService absChannelRechargeService = rechargeServiceMap.get(channel.getChannelId());
        BigDecimal balance = absChannelRechargeService.balanceQuery(channel);

        logger.info("channelId :{}, balance :{}", channel.getChannelId(), balance);

        map.put("code", 200);
        map.put("msg", "success");
        map.put("channelName", channel.getChannelName());
        map.put("balance", balance);
        return map;
    }
}
