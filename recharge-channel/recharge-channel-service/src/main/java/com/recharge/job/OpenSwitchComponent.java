/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.recharge.job;

import org.springframework.stereotype.Service;

@Service
public class OpenSwitchComponent {

    //回调开关
    private boolean isOpenRecharge = false;


    private boolean endRecharge = false;


    public boolean isOpenRecharge() {
        return isOpenRecharge;
    }

    public void setOpenRecharge(boolean openRecharge) {
        isOpenRecharge = openRecharge;
    }

    public boolean isEndRecharge() {
        return endRecharge;
    }

    public void setEndRecharge(boolean endRecharge) {
        this.endRecharge = endRecharge;
    }
}
