package com.recharge.mapper;

import com.recharge.domain.RechargeOrder;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by qi.cao on 2016/4/21.
 */
public interface IRechargeOrderMapper {


    /**
     * 更新订单信息
     * @param orderIds
     * @param oldStateArray
     * @param newState
     * @return
     */
    int updateState(@Param("orderIds") String[] orderIds, @Param("supUid") String supUid, @Param("oldStateArray") int[] oldStateArray,
                    @Param("newState") int newState, @Param("rechargeFailReason") String rechargeFailReason);

    /**
     * 插入订单信息
     * @param rechargeOrder
     * @return
     */
    int insertOrder(RechargeOrder rechargeOrder);

    /**只更新初始化信息部分
     * 商品信息，以及rechargeInfo
     * @param rechargeOrder
     * @return
     */
    int updateOrderInitInfo(RechargeOrder rechargeOrder);

    /**
     * 更新订单供货商信息
     * @param supId
     * @param supName
     * @param supList
     * @return
     */
    int updateOrderSupInfo(@Param("orderId") String orderId, @Param("supId") String supId, @Param("supName") String supName, @Param("cost") BigDecimal cost, @Param("supList") String supList);

    /**
     * 更新超时时间
     * @param orderIds
     * @param timeOutTime
     * @return
     */
    int updateTimeOut(@Param("orderIds") String[] orderIds, @Param("timeOutTime") Date timeOutTime);

    /**
     * 更新订单支付信息
     * @return
     */
    int updateOrderPayInfo(@Param("orderId") String orderId, @Param("payId") String payId, @Param("payState") int payState);

    /**
     * 根据起始结束时间查询固定数量的订单
     * @param orderState
     * @param startTime
     * @param endTime
     * @param limitNo
     * @return
     */
    List<RechargeOrder> selectListByStateSupId(@Param("orderState") int orderState, @Param("supId") String supId,
                                               @Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("limitNo") int limitNo);

    /**
     * 根据状态查询指定数量的订单。
     * @param orderState
     * @param limitNo
     * @return
     */
    List<RechargeOrder> selectTimeOutListByState(@Param("orderState") int orderState, @Param("limitNo") int limitNo);

    /**
     * 更新充值信息
     * @param rechargeOrder
     * @return
     */
    int updateRechargeInfo(RechargeOrder rechargeOrder);

    /**
     * 通过订单号查询订单信息
     * @param orderId
     * @return
     */
    RechargeOrder selectByOrderId(String orderId);

    /**
     * 通过订单号查询订单信息
     * @param extOrderId
     * @return
     */
    RechargeOrder selectByExtOrderId(@Param("extOrderId") String extOrderId, @Param("merchantId") String merchantId);

    /**
     * 查询供货商监控信息
     *
     * sup_id,sup_name,recharge_state,"number"
     * @return
     */
    Map<String,String> selectSupMonitor();

    List<RechargeOrder> selectList(@Param("rechargeOrder") RechargeOrder rechargeOrder, @Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("curr") Integer curr, @Param("pageSize") Integer pageSize);

    int count(@Param("rechargeOrder") RechargeOrder rechargeOrder, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    BigDecimal totalSalePrice(@Param("rechargeOrder") RechargeOrder rechargeOrder, @Param("startTime") Date startTime, @Param("endTime") Date endTime);


    List<Map<String,Object>> totalOrderInfo(@Param("startTime") Date startTime);


    /**
     * 购买卡密时更新订单信息
     * @param orderId
     * @param supId
     * @param supName
     * @param salePrice
     * @param rechargeInfo
     * @return
     */
    int updateBuyCardInfo(@Param("orderId") String orderId, @Param("supId") String supId, @Param("supName") String supName,
                          @Param("salePrice") BigDecimal salePrice, @Param("rechargeInfo") String rechargeInfo, @Param("cost") BigDecimal cost);


    /**
     * rechargeorder 表批量加锁
     * @param ons
     * @return
     */
    int batchLockRechargeOrder(@Param("rechargeOrders") List<RechargeOrder> ons);

    /**
     * rechargeorder 表批量解锁
     * @param ons
     * @return
     */
    int batchUnLockRechargeOrder(@Param("rechargeOrders") List<RechargeOrder> ons);

    List<RechargeOrder> queryJDcard(@Param("count") int count, @Param("maxTime") Date maxDate);

    /**
     * 
     * 对比订单号
     * @param extendOrders
     * @return
     */
    List<RechargeOrder> compareOrderId(@Param("extendOrders") List<String> extendOrders);/**
     *
     * 查询指定的订单
     * @param orderIdList
     * @return
     */
    List<RechargeOrder> selectByOrderIdList(@Param("orderIdList") List<String> orderIdList, @Param("startTime") Date startTime);

    /**
     * 通过PS订单号查询订单信息
     * @param orderId
     * @return
     */
    RechargeOrder selectByChannleOrderId(@Param("rechargeId")String orderId);

    RechargeOrder selectByChannleOrderIdOnrecent(@Param("rechargeId")String orderId,@Param("start")String start);

}
