package com.recharge.mapper;

import com.recharge.domain.BuyCardInfo;
import org.apache.ibatis.annotations.Param;

/**
 * @author qi.cao
 */
public interface IBuyCardMapper {

    int insertOne(@Param("buyCardInfo") BuyCardInfo buyCardInfo);

    BuyCardInfo selectOne(@Param("orderId") String orderId, @Param("merchantId") String merchantId);

    int update(@Param("buyCardInfo") BuyCardInfo buyCardInfo);
}
