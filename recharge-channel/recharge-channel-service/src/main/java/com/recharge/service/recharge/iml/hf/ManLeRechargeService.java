package com.recharge.service.recharge.iml.hf;

import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.service.recharge.AbsChannelRechargeService;
import org.springframework.stereotype.Service;

@Service
public class ManLeRechargeService extends AbsChannelRechargeService {
    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        return null;
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        return null;
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        return null;
    }
}
