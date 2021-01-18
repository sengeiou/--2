package com.recharge.mapper;

/**
 * Created by qi.cao on 2016/5/11.
 */
public interface ISequenceMapper {

    int selectNextval(String sequenceName);
}
