package com.recharge.job;

import com.recharge.domain.Channel;
import com.recharge.mapper.IChannelMapper;
import com.recharge.service.recharge.iml.hf.WuYouGouRechargeServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Administrator
 * @create 2021/1/21 15:27
 */
public class WuYouGouFlushKeyJob {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WuYouGouRechargeServiceImpl wuYouGouRechargeService;
    @Autowired
    private IChannelMapper iChannelMapper;
    private String channelId="100140";

    @Scheduled(cron = "${RongXiangFlushKeyCron}")
    public void flush() {
        logger.info("无忧购开始刷新token");
        Channel channel = iChannelMapper.selectByChannelId(channelId);
        String s = wuYouGouRechargeService.signIn(channel);
        if (!(StringUtils.equals("fail",s))){
            logger.info("无忧购刷新token成功:{}", s);
            iChannelMapper.updateToken("",s,channelId);
        }else {
            logger.info("无忧购开始第二次刷新token");
            String s1 = wuYouGouRechargeService.signIn(channel);
            if (!(StringUtils.equals("fail",s1))){
                logger.info("无忧购第二次刷新token成功:{}", s1);
                iChannelMapper.updateToken("",s,channelId);
            }else {
            }
        }
    }
}
