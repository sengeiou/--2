/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.recharge.controller;

import com.recharge.job.OpenSwitchComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 内部使用
 */
@Controller
@RequestMapping("/rcg")
public class CommonController {

    @Autowired
    OpenSwitchComponent openSwitchComponent;


    @RequestMapping(value = "/closeRecharge")
    @ResponseBody
    public String closeRecharge(HttpServletRequest request) {

        if (!validAddr(request)) {
            return null;
        }
        openSwitchComponent.setOpenRecharge(false);
        return "closed";
    }

    @RequestMapping(value = "/openRecharge")
    @ResponseBody
    public String openRecharge(HttpServletRequest request) {
        if (!validAddr(request)) {
            return null;
        }
        openSwitchComponent.setOpenRecharge(true);
        return "opening";
    }

    @RequestMapping(value = "/isEndRecharge")
    @ResponseBody
    public String isEndRecharge(HttpServletRequest request) {
        if (!validAddr(request)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(openSwitchComponent.isEndRecharge() ? "recharge end;" : "recharge running;");
        return stringBuilder.toString();
    }


    private boolean validAddr(HttpServletRequest request) {
        String host = request.getLocalAddr();
        System.out.println(host);
        if ("0:0:0:0:0:0:0:1".equals(host) || "127.0.0.1".equals(host) || "localhost".equals(host)) {
            return true;
        }

        return false;

    }

}
