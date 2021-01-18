package com.recharge.mapper;

import com.recharge.domain.Channel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by qi.cao on 2016/5/17.
 */
public interface IChannelMapper {

    List<Channel> selectListStateOn(String groupId);

    Channel selectByChannelId(String channelId);

    int updateToken(@Param("access_token") String access_token,@Param("refresh_token") String refresh_token,@Param("channelId") String channelId);
}
