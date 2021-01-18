package com.recharge.bean;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by qi.cao on 2016/5/23.
 */
public class ProcessResult {
    //成功
    public static final String SUCCESS = "0000";
    //未知错误
    public static final String UNKOWN = "1000";
    //处理中
    public static final String PROCESSING="2000";
    //失败
    public static final String FAIL = "4000";

    public static final String NO_BALANCE = "4001";

    private String code ;

    private String msg;

    public ProcessResult(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess(){
        return StringUtils.equals(SUCCESS,code);
    }
}