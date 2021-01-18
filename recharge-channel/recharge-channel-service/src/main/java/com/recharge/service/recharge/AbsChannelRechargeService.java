package com.recharge.service.recharge;

import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.domain.ProductRelation;
import com.recharge.mapper.IProductRelationMapper;
import com.recharge.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by qi.cao on 2016/5/20.
 */
public abstract class AbsChannelRechargeService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IProductRelationMapper iProductRelationMapper;

    @Autowired
    private ChannelService channelService;

    /**
     * 具体供货商充值逻辑
     *
     * @param channel
     * @param rechargeOrderBean
     */
    public abstract ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean);

    /**查询接口
     * @param channel
     * @param channelOrder
     * @return
     */
    public abstract ProcessResult query(Channel channel, ChannelOrder channelOrder);
    /**
     * 回调处理函数
     */
    public abstract ProcessResult parseResponse(ResponseOrder responseOrder);

    public ProductRelation queryChannelProductId(String productName,String channelId){
        return iProductRelationMapper.selectByName(productName,channelId);
    }

    public BigDecimal balanceQuery(Channel channel){
        return new BigDecimal("0");
    }

    /**
     　　* 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     　　* @param params 需要排序并参与字符拼接的参数组
     　　* @return 拼接后字符串
     　　* @throws UnsupportedEncodingException
     　　*/
     public String createLinkStringByGet(Map<String, String> params) throws UnsupportedEncodingException {
         List<String> keys = new ArrayList<String>(params.keySet());
         Collections.sort(keys);
         String prestr = "";
         for (int i = 0; i < keys.size(); i++) {
             String key = keys.get(i);
             String value = params.get(key);
             value = URLEncoder.encode(value, "UTF-8");
             if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                 prestr = prestr + key +  value;
             } else {
                 prestr = prestr + key + value;
             }
         }
         return prestr;
     }

     public void callBack(ResponseOrder responseOrder , String channelId){
         new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     Thread.sleep(10000);
                 } catch (InterruptedException e) {
                     logger.error("InterruptedException" ,e);
                 }

                 channelService.callBack(channelId,responseOrder);
             }
         }).start();
     }
}
