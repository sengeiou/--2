package com.recharge.job;

import com.recharge.domain.Channel;
import com.recharge.mapper.IChannelMapper;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.iml.hf.RongXiangRechargeServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @author Administrator
 * @create 2021/1/21 15:27
 */
public class RongXiangFlushKeyJob {
    @Autowired
    private RongXiangRechargeServiceImpl rongXiangRechargeService;
    @Autowired
    private IChannelMapper iChannelMapper;
    private String channelId="100139";

    @Scheduled(cron = "${RongXiangFlushKeyCron}")
    public void flush() {
        Channel channel = iChannelMapper.selectByChannelId(channelId);
        String s = rongXiangRechargeService.signIn(channel);
        if (!(StringUtils.equals("fail",s))){
            iChannelMapper.updateToken("",s,channelId);
        }else {
            String s1 = rongXiangRechargeService.signIn(channel);
            if (!(StringUtils.equals("fail",s))){
                iChannelMapper.updateToken("",s,channelId);
            }else {
            }
        }
    }
}
