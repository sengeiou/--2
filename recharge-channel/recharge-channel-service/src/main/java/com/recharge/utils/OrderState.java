package com.recharge.utils;

/**
 * Created by qi.cao on 2016/5/17.
 */
public interface OrderState {

    /**
     * 没有发送给渠道
     */
    String UN_SEND = "1";

    String SENDED = "2";

    /**
     * 失败
     */
    String FAIL = "3";

    /**
     * 未知
     */
    String UNKOWN="4";

    /**
     * 成功
     */
    String SUCCESS = "5";
}
