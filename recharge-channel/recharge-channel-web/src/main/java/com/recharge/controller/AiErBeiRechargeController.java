package com.recharge.controller;

import com.alibaba.fastjson.JSON;
import com.recharge.bean.ResponseOrder;
import com.recharge.domain.AiErBei;
import com.recharge.service.ChannelService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Administrator
 * @create 2020/12/15 17:20
 */
@Controller
@RequestMapping("/aiErBei")
public class AiErBeiRechargeController {

    @Autowired
    private ChannelService channelService;


    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping("/callBack")
    @ResponseBody
    public void callBack(@RequestBody(required = false) AiErBei aiErBei, HttpServletResponse res) {
        ResponseOrder responseOrder = new ResponseOrder();
        responseOrder.setChannelOrderId(aiErBei.getClientOrderId());
        responseOrder.setResponseCode(aiErBei.getStatus());
        if (StringUtils.isNotBlank(aiErBei.getSerial())) {
            responseOrder.setOutChannelOrderId(StringUtils.substring(aiErBei.getSerial(), 0, 90));
        }
        logger.info("AiErBeiController callback :" + JSON.toJSONString(responseOrder));
        channelService.callBack("100116", responseOrder);
        try {
            res.setContentType("text/html;charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.print("success");
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
