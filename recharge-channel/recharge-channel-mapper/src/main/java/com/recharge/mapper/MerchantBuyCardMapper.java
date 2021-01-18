package com.recharge.mapper;

import com.recharge.domain.MerchantBuyCardPo;
import com.recharge.domain.condition.MerchantBuyCardCondition;

import java.util.List;

public interface MerchantBuyCardMapper {

    /**
     * 插入已购买卡密信息
     * @param merchantBuyCardPo
     * @return
     */
    int insert(MerchantBuyCardPo merchantBuyCardPo);


     /**
     * 批量插入已购买卡密信息
     * @param list
     * @return
     */
    int insertByBatch(List<MerchantBuyCardPo> list);


    /**
     * 查询客户已购买列表
     * @param condition
     * @return
     */
    List<MerchantBuyCardPo> selectList(MerchantBuyCardCondition condition);

}
