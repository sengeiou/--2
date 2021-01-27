package com.recharge.mapper;

import com.recharge.domain.ChannelOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by qi.cao on 2016/5/17.
 */
public interface IChannelOrderMapper {

    int insertOrder(ChannelOrder channelOrder);

    /**
     * 更新订单状态。
     * @param channelOrder
     * @return
     */
    int updateOrderState(@Param("channelOrder") ChannelOrder channelOrder , @Param("oldStateArray") String[] oldStateArray);

    ChannelOrder selectByChannelOrderId(String channelOrderId);

    ChannelOrder selectByOutChannelOrderId(String outChannelOrderId);

    int updateOutChannelId(ChannelOrder channelOrder);

    List<ChannelOrder> selectByChannelIdStatus(@Param("channelIds") String[] channelIds, @Param("status") String status);

    int updateQueryCount(ChannelOrder channelOrder);

    ChannelOrder selectByChannelOrderIdOnRecent(@Param("channelOrderId")String channelOrderId,@Param("start")String start);

}
