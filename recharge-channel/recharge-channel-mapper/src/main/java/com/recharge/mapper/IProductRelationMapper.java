package com.recharge.mapper;

import com.recharge.domain.ProductRelation;
import org.apache.ibatis.annotations.Param;

/**
 * Created by qi.cao on 2017/4/27.
 */
public interface IProductRelationMapper {

    ProductRelation selectByName (@Param("productName") String productName, @Param("channelId") String channelId);
}
