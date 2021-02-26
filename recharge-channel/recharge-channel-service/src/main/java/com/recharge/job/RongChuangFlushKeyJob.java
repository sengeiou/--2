package com.recharge.job;

import com.recharge.domain.Channel;
import com.recharge.mapper.IChannelMapper;
import com.recharge.service.recharge.iml.hf.RongXiangRechargeServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Administrator
 * @create 2021/2/26 21:01
 */
public class RongChuangFlushKeyJob {
    @Autowired
    private RongXiangRechargeServiceImpl rongXiangRechargeService;
    @Autowired
    private IChannelMapper iChannelMapper;
    private String channelId="100139";


    @Scheduled(cron = "${RongChuangFlushKeyCron}")
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
