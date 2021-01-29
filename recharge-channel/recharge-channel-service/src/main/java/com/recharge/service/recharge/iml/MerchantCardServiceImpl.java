

package com.recharge.service.recharge.iml;

import com.recharge.common.utils.DesUtil;
import com.recharge.domain.MerchantBuyCardPo;
import com.recharge.domain.PlatformCardInfo;
import com.recharge.mapper.MerchantBuyCardMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * 批量插入已购买卡密信息
     *
     * @param platformCardInfos
     * @return
     */
    public int insertByBatch(List<PlatformCardInfo> platformCardInfos,String orderId,String merchantId) {
        String newPsd = password + merchantId;
        String pass = getCustomerPassword(newPsd,32);
        logger.info("插卡使用的password+客户id ={},截取32位={}",newPsd,pass);
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
            po.setMerchantId(merchantId);
            po.setExpireTime(item.getExpireTime());
            return po;
        }).collect(Collectors.toList());

        int count = merchantBuyCardMapper.insertByBatch(list);
        return count;
    }

    public static String getCustomerPassword(String md5key,Integer ln){
        String key = StringUtils.left(md5key,ln);
        return StringUtils.rightPad(key,ln,"0");
    }

    @Test
    void test(){
        String newPsd = "giLuT2D19XtWGt4r" + "M13903";
        String pass = getCustomerPassword(newPsd,32);
        String no = DesUtil.encrypt("3100580205727331474", pass);
        String pwd = DesUtil.encrypt("5WH3UPFUNWEFUT3V", pass);
        System.out.println("z");
    }
}
