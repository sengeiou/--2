

package com.recharge.service.recharge.iml;

import com.recharge.common.utils.DesUtil;
import com.recharge.domain.MerchantBuyCardPo;
import com.recharge.domain.PlatformCardInfo;
import com.recharge.mapper.MerchantBuyCardMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MerchantCardServiceImpl {

    @Autowired
    private MerchantBuyCardMapper merchantBuyCardMapper;

    @Value("${card.descKey}")
    private String password;

    /**
     * 批量插入已购买卡密信息
     *
     * @param platformCardInfos
     * @return
     */
    public int insertByBatch(List<PlatformCardInfo> platformCardInfos,String orderId) {

        String pass = getCustomerPassword(password + platformCardInfos.get(0).getCustomerId(),32);

        List<MerchantBuyCardPo> list = platformCardInfos.stream().map(item -> {
            MerchantBuyCardPo po = new MerchantBuyCardPo();
            po.setOrderId(orderId);
            if (StringUtils.isNotEmpty(item.getCardNo())) {
                po.setCardNo(DesUtil.encrypt(item.getCardNo(), pass));
            }
            if (StringUtils.isNotEmpty(item.getCardPwd())) {
                po.setCardPwd(DesUtil.encrypt(item.getCardPwd(), pass));
            }
            po.setProductId(item.getProductId());
            po.setProductName(item.getProductName());
            po.setSupId(item.getSupId());
            po.setMerchantId(item.getCustomerId());
            po.setExpireTime(item.getExpireTime());
            return po;
        }).collect(Collectors.toList());

        int count = merchantBuyCardMapper.insertByBatch(list);
        return count;
    }

    private String getCustomerPassword(String md5key,Integer ln){
        String key = StringUtils.left(md5key,ln);
        return StringUtils.rightPad(key,ln,"0");
    }
}
