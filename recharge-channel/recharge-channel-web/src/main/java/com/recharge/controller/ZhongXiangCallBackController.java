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

import java.util.Arrays;

/**
 * Created by qi.cao on 2016/5/23.
 */
@Controller
@RequestMapping("/zhongxiang")
public class ZhongXiangCallBackController {

    @Autowired
    private ChannelService channelService;

    private String channelId = "100002";

    private Logger logger = LoggerFactory.getLogger(getClass());

/*    public static void main(String[] args) {
        String[] a = new String[]{"cp_code","cp_tran_no","prod_code","prod_num","trana_no","tran_price","tran_state","time"};
        Arrays.sort(a);
        System.out.println(JSON.toJSONString(a));
    }*/

    @RequestMapping("/callback")
    @ResponseBody
    public String callBack(@RequestParam("cp_code")String cp_code,
                           @RequestParam("cp_tran_no")String cp_tran_no,
                           @RequestParam("prod_code")String prod_code,
                           @RequestParam("prod_num")String prod_num,
                           @RequestParam("trana_no")String tran_no,
                           @RequestParam("tran_price")String tran_price,
                           @RequestParam("tran_state")String tran_state,
                           @RequestParam("time")String time,
                           @RequestParam("sign")String sign){
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
        responseOrder.setResponseCode(tran_state);
        responseOrder.setChannelOrderId(cp_tran_no);
        responseOrder.setOutChannelOrderId(tran_no);
        Channel channel = channelService.queryChannelInfo(channelId);
        JSONObject configJSONObject = JSONObject.parseObject(channel.getConfigInfo());

        String sourceString  = cp_code+"|"+cp_tran_no+"|"+prod_code+"|"+prod_num+"|"+time+"|"+tran_price+"|"+tran_state+"|"+tran_no;
        logger.info("加密原串是："+sourceString);
        String signOne =DigestUtils.md5Hex(sourceString+"|"+configJSONObject.getString("md5_key"));
        if (!StringUtils.equals(sign, DigestUtils.md5Hex(signOne+"|"+configJSONObject.getString("md5_key")))){
            return "fail";
        }

        channelService.callBack(channelId,responseOrder);
        return "success";
    }
}
