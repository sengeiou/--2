package com.recharge.job;

import com.recharge.domain.Channel;
import com.recharge.mapper.IChannelMapper;
import com.recharge.service.recharge.iml.hf.RongChuangRechargeServiceImpl;
import com.recharge.service.recharge.iml.hf.RongXiangRechargeServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Administrator
 * @create 2021/2/26 21:01
 */
public class RongChuangFlushKeyJob {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RongChuangRechargeServiceImpl rongChuangRechargeService;
    @Autowired
    private IChannelMapper iChannelMapper;
    private String channelId="100139";


    @Scheduled(cron = "${RongChuangFlushKeyCron}")
    public void flush() {
        logger.info("荣创开始刷新token");
        Channel channel = iChannelMapper.selectByChannelId(channelId);
        String s = rongChuangRechargeService.signIn(channel);
        if (!(StringUtils.equals("fail",s))){
            logger.info("荣创刷新token成功:{}", s);
            iChannelMapper.updateToken("",s,channelId);
        }else {
            logger.info("荣创开始第二次刷新token:{}", s);
            String s1 = rongChuangRechargeService.signIn(channel);
            if (!(StringUtils.equals("fail",s1))){
                logger.info("荣创第二次刷新token成功:{}", s1);
                iChannelMapper.updateToken("",s,channelId);
            }else {
            }
        }
    }
}
