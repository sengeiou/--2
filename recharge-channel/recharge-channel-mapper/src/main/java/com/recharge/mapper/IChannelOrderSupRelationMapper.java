package com.recharge.mapper;

import com.recharge.domain.ChannelOrderSupRelation;
import org.apache.ibatis.annotations.Param;

/**
 * @author Administrator
 * @create 2021/3/2 17:39
 */
public interface IChannelOrderSupRelationMapper {

    ChannelOrderSupRelation selectBySupOrderId(@Param("supOrderId") String supOrderId);

    int updateSupOrderId(@Param("ChannelOrderSupRelation") ChannelOrderSupRelation ChannelOrderSupRelation);

}
