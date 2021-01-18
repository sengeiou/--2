package com.recharge.job;

import com.alibaba.fastjson.JSONObject;
import com.recharge.common.utils.SmsService;
import com.recharge.domain.BalanceListenerConfig;
import com.recharge.domain.Channel;
import com.recharge.mapper.IBalanceListenerConfigMapper;
import com.recharge.mapper.IChannelMapper;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by qi.cao on 2017/6/21.
 */
@Service
public class BalanceListenerJob {

    @Resource(name = "channelMap")
    private Map<String, AbsChannelRechargeService> rechargeServiceMap;

    @Autowired
    private IBalanceListenerConfigMapper iBalanceListenerConfigMapper;

    @Autowired
    private IChannelMapper iChannelMapper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${warnPhones}")
    private String warnPhones;

    @Value("${signName}")
    private String signName;

    @Value("${groupId}")
    private String groupId;

    @Value("${balanceOpen}")
    private int balanceOpen;

    @Scheduled(cron = "${balanceListenerJob.cron}")
    public void init() {
        try {
            if(balanceOpen != 1){
                return;
            }
            logger.info("query balance job start.");
            List<BalanceListenerConfig> balanceListenerConfigs = iBalanceListenerConfigMapper.queryAll();

            for (BalanceListenerConfig balanceListenerConfig : balanceListenerConfigs){
                Channel channel = iChannelMapper.selectByChannelId(balanceListenerConfig.getChannelId());
                BigDecimal balance = rechargeServiceMap.get(balanceListenerConfig.getChannelId()).balanceQuery(channel);
                if (balance.compareTo(balanceListenerConfig.getWarnAmt()) <=0){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("channelId" , channel.getChannelName()+"-"+channel.getChannelId());
                    jsonObject.put("balance" , balance);
                    SmsService.sendSms(warnPhones ,signName,jsonObject.toJSONString(),"SMS_133969145");
                    iBalanceListenerConfigMapper.updateTime(channel.getChannelId());
                }
            }
        } catch (Exception e) {
            logger.error("query balance job error.", e);
        }
    }
}
