package com.recharge.mapper;

import com.recharge.domain.PlatformCardInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IPlatformCardInfoMapper {

    /**
     * 通过商品 供货商编号 查询出指定数量的记录
     * @param ids
     * @return
     */
    int updateOrderId(@Param("id") Integer ids, @Param("orderId") String orderId, @Param("oldStatus") String oldStatus, @Param("newStatus") String newStatus, @Param("isSelectedLock") Integer isSelectedLock);

    /**
     * 清空订单信息用于以后再次使用
     * @param id
     * @return
     */
    int emptyOrderId(@Param("id") Integer id);

    List<PlatformCardInfo> selectByOrderId(String orderId);

    List<String> selectStockIds(@Param("actId") String actId, @Param("number") Integer number);

    List<PlatformCardInfo> selectById(@Param("orderId") String orderId, @Param("merchantId") String merchantId);

    List<PlatformCardInfo> queryAll(@Param("platformCanrdInfo") PlatformCardInfo platformCardIfo);

    /**
     * 查询库存
     * @param platformCardIfo  查询条件
     * @param outNum 查询库存
     * @return
     */
    List<PlatformCardInfo> queryStock(@Param("platformCardInfo") PlatformCardInfo platformCardIfo, @Param("outNum") Integer outNum);

    /**
     * 查询公有库数量
     * @param platformCardInfo
     * @return
     */
    List<PlatformCardInfo> queryStokcNum(@Param("platformCardInfo") PlatformCardInfo platformCardInfo);


    int lock(@Param("cond") List<PlatformCardInfo> cond);

    void updateStatus(@Param("orderId") String orderId);

    /**
     * 批量锁卡
     * @param cardIds
     * @return
     */
    int lockCards(@Param("cards") List<PlatformCardInfo> cardIds);

    int unLockCards(@Param("cards") List<PlatformCardInfo> cardIds);

    int batchUpdateExtractCardOrder(@Param("cards") List<PlatformCardInfo> cardIds, @Param("orderId") String orderId, @Param("oldStatus") String oldStatus, @Param("newStatus") String newStatus, @Param("isSelectedLock") Integer isSelectedLock);
}
