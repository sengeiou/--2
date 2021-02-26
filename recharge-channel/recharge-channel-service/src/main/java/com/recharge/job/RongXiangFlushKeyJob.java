package com.recharge.job;

import com.alibaba.fastjson.JSON;
import com.recharge.domain.Channel;
import com.recharge.mapper.IChannelMapper;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.iml.hf.RongChuangRechargeServiceImpl;
import com.recharge.service.recharge.iml.hf.RongXiangRechargeServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @author Administrator
 * @create 2021/1/21 15:27
 */
public class RongXiangFlushKeyJob {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RongXiangRechargeServiceImpl rongXiangRechargeService;
    @Autowired
    private IChannelMapper iChannelMapper;
    private String channelId="100140";

    @Scheduled(cron = "${RongXiangFlushKeyCron}")
    public void flush() {
        logger.info("荣享开始刷新token");
        Channel channel = iChannelMapper.selectByChannelId(channelId);
        String s = rongXiangRechargeService.signIn(channel);
        if (!(StringUtils.equals("fail",s))){
            logger.info("荣享刷新token成功:{}", s);
            iChannelMapper.updateToken("",s,channelId);
        }else {
            logger.info("荣享开始第二次刷新token");
            String s1 = rongXiangRechargeService.signIn(channel);
            if (!(StringUtils.equals("fail",s1))){
                logger.info("荣享第二次刷新token成功:{}", s1);
                iChannelMapper.updateToken("",s,channelId);
            }else {
            }
        }
    }
}
