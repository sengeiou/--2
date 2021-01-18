package com.recharge.job;

import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.mapper.IChannelMapper;
import com.recharge.mapper.IChannelOrderMapper;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.utils.ConstantsUtils;
import com.recharge.utils.OrderState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by qi.cao on 2017/6/21.
 */
@Service
public class QueryResultJob {

    @Autowired
    private IChannelOrderMapper iChannelOrderMapper;

    @Autowired
    private IChannelMapper iChannelMapper;

    @Resource(name = "channelMap")
    private Map<String, AbsChannelRechargeService> rechargeServiceMap;

    @Autowired
    private ChannelService channelService;

    @Resource(name = "taskMap")
    private Map<String, ThreadPoolTaskExecutor> taskExecutorMap;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${groupId}")
    private String groupId;

    @Value("${queryResultOpen}")
    private int queryResultOpen;

    @Scheduled(cron = "${queryOrderJobCron}")
    public void init() {
        try {
            if(queryResultOpen != 1)return;
            logger.info("query result job start.");
            List<Channel> channelList = iChannelMapper.selectListStateOn(groupId);
            for (Channel channel : channelList) {
                String[] channelIds = new String[]{channel.getChannelId()};
                List<ChannelOrder> channelOrders = iChannelOrderMapper.selectByChannelIdStatus(channelIds, OrderState.SENDED);

                if (CollectionUtils.isNotEmpty(channelOrders)) {
                    if (CollectionUtils.isNotEmpty(channelOrders)) {
                        for (ChannelOrder channelOrder : channelOrders) {
                            logger.info("orderid:{} query result.", channelOrder.getOrderId());
                            AbsChannelRechargeService absChannelRechargeService = rechargeServiceMap.get(channelOrder.getChannelId());

                            ProcessResult processResult = absChannelRechargeService.query(channel, channelOrder);

                            logger.info(JSONObject.toJSONString(processResult));
                            if (processResult.isSuccess()) {
                                channelOrder.setState(OrderState.SUCCESS);
                            } else if (StringUtils.equals(processResult.getCode(), ProcessResult.PROCESSING)) {
                                logger.info("order processing :{}", channelOrder.getOrderId());
                                iChannelOrderMapper.updateQueryCount(channelOrder);
                                continue;
                            } else if (StringUtils.equals(processResult.getCode(), ProcessResult.FAIL)) {
                                channelOrder.setState(OrderState.FAIL);
                            } else {
                                channelOrder.setState(OrderState.UNKOWN);
                            }
                            try {
                                channelService.resultProcess(channelOrder, ConstantsUtils.TYPE_QUERY);
                            } catch (Exception e) {
                                logger.error("resultProcess error" , e);
                            }
                            iChannelOrderMapper.updateQueryCount(channelOrder);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("query task error" , e);
        }
    }
}
