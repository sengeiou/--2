package com.recharge.job;

import com.recharge.center.IOrderExportService;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.center.bean.RechargeStateBean;
import com.recharge.domain.Channel;
import com.recharge.domain.Maintenance;
import com.recharge.mapper.IChannelMapper;
import com.recharge.mapper.IMaintenanceMapper;
import com.recharge.mapper.LockMapper;
import com.recharge.service.ChannelService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by qi.cao on 2016/5/16.
 */
@Service
public class GetOrderJob {

    @Autowired
    private IOrderExportService iOrderExportService;

    @Autowired
    private IChannelMapper iChannelMapper;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private IMaintenanceMapper iMaintenanceMapper;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LockMapper lockMapper;

    @Value("${groupId}")
    private String groupId;

    @Value("${user}")
    private String user;

    @Value("${getOrderOpen}")
    private int getOrderOpen;

    @Autowired
    private OpenSwitchComponent openSwitchComponent;

    @Scheduled(cron = "${getOrderJobCron}")
    public void init(){

        if(!openSwitchComponent.isOpenRecharge())return;
        openSwitchComponent.setEndRecharge(false);
        logger.info("get order job start");
        List<Channel> channelList = iChannelMapper.selectListStateOn(groupId);
        for (Channel channel : channelList) {
            try {
                List<RechargeOrderBean> rechargeOrderBeanList = iOrderExportService.getToRechargeOrder(channel.getChannelId(), channel.getGetNum());


                Maintenance maintenance = iMaintenanceMapper.selectById(channel.getChannelId());
                if (maintenance != null && maintenance.getStatus() == Maintenance.STATUS_ON) {

                    for (RechargeOrderBean rechargeOrderBean : rechargeOrderBeanList) {
                        logger.info("orderId:{} maintenance. channelId:{}", rechargeOrderBean.getOrderId(), channel.getChannelId());
                        rechargeOrderBean.setRechargeState(RechargeStateBean.UNKOWN);
                        iOrderExportService.orderToUnknown(rechargeOrderBean);
                    }

                    continue;
                }

                if (CollectionUtils.isNotEmpty(rechargeOrderBeanList)) {
                    channelService.rechargeBatch(channel, rechargeOrderBeanList);
                }
            } catch (Exception e) {
                logger.error("supId:{}取单发生错误", channel.getChannelId(), e);
            }
        }
        openSwitchComponent.setEndRecharge(true);
    }

//    @Scheduled(cron="${getOrderJobCron}")
//    public void getOrder(){
//        List<Channel> channelList =iChannelMapper.selectListStateOn(groupId);
//        for (Channel channel : channelList){
//            try {
//                List<RechargeOrderBean> rechargeOrderBeanList = iOrderExportService.getToRechargeOrder(channel.getChannelId(), channel.getGetNum());
//                if (CollectionUtils.isNotEmpty(rechargeOrderBeanList)){
//                    channelService.rechargeBatch(channel ,rechargeOrderBeanList);
//                }
//            } catch (Exception e) {
//                logger.error("supId:{}取单发生错误",channel.getChannelId(),e);
//            }
//        }
//    }
}
