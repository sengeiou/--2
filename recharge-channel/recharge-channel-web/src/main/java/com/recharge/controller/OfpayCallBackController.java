package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.Channel;
import com.recharge.service.ChannelService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by qi.cao on 2016/5/23.
 */
@Controller
@RequestMapping("/ofpay")
public class OfpayCallBackController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100003";

    private Logger logger = LoggerFactory.getLogger(getClass());

/*    public static void main(String[] args) {
        String[] a = new String[]{"cp_code","cp_tran_no","prod_code","prod_num","trana_no","tran_price","tran_state","time"};
        Arrays.sort(a);
        System.out.println(JSON.toJSONString(a));
    }*/

    @RequestMapping("/callback")
    @ResponseBody
    public String callBack(@RequestParam("ret_code")String retCode,
                           @RequestParam("sporder_id")String sporderId,
                           @RequestParam("ordersuccesstime")String ordersuccesstime,
                           @RequestParam("err_msg")String errMsg){
        /*
        * cp_code	string	是 商户编码
            cp_tran_no	string	是	商户订单号，确保平台唯一性字母、数字或字线数字组合
            prod_code	string	是	商品编码
            prod_num	int	是	商品数量
            tran_no	string	是	订单号
            tran_state	int	是	订单状态
            tran_price	double	是	订单金额
            time	string	是	众想订单完成时间,精确到秒
            格式：yyyyMMddHHmmss
            sign	string	是	签名(详情见签名规则)

        *
        * */
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setResponseCode(retCode);
        responseOrder.setChannelOrderId(sporderId);
        responseOrder.setResponseMsg(errMsg);
        logger.info("ofpay callback :{}", JSON.toJSONString(responseOrder));
        channelService.callBack(channelId,responseOrder);
        return "success";
    }
}
