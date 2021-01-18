package com.recharge.service.recharge.iml.tika;

import com.recharge.domain.PlatformCardInfo;
import com.recharge.mapper.IPlatformCardInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * 卡库管理
 */
@Service
public class CardStockService {
	@Autowired
	private CardEncrypt cardEncrypt;

    @Autowired
    private IPlatformCardInfoMapper iPlatformCardInfoMapper;

    /**
     * 新的提卡查询接口
     * 1. 首先出私库,如果私库库存不足查询公库
     * @param orderId
     * @param productId
     * @param outNumber
     * @return
     */
    @Transactional
    public List<PlatformCardInfo> outNewCards(String orderId, String productId, Integer outNumber, String merchantId) {
    	//提走的卡
    	List<PlatformCardInfo> updatePlatformCards = new ArrayList<>();
    	try {
	    	PlatformCardInfo privateStockQuery = new PlatformCardInfo();
	    	privateStockQuery.setCustomerId(merchantId);
	    	privateStockQuery.setProductId(productId);
	    	privateStockQuery.setStatus(PlatformCardInfo.STATUS_UN_SALE);
	        //查询私库
	        List<PlatformCardInfo> privateStockCardInfos = iPlatformCardInfoMapper.queryStock(privateStockQuery,outNumber);
	        /**
	         * 如果私库足够直接返回出去
	         */
	        if(!privateStockCardInfos.isEmpty() && privateStockCardInfos.size()>=outNumber){
	        	return handleExtractResult(privateStockCardInfos, orderId);
	        }else{
	        	Integer privateStockNumber = 0 ;
	        	Integer publicStockNumber = 0;
	        	if(!privateStockCardInfos.isEmpty()){
	        		privateStockNumber = privateStockCardInfos.size(); //私库数量
	        	}
	        	PlatformCardInfo publicStockQuery = new PlatformCardInfo();
	        	publicStockQuery.setCustomerId(PlatformCardInfo.PUBLIC_STOCK);
	        	publicStockQuery.setProductId(productId);
	        	publicStockQuery.setStatus(PlatformCardInfo.STATUS_UN_SALE);
	            //查询公库
	            Integer publicStockNum = outNumber - privateStockNumber; 
	            List<PlatformCardInfo> publicStockCardInfos = iPlatformCardInfoMapper.queryStock(publicStockQuery,publicStockNum);
	            //公库库存为空直接返回
	            if(publicStockCardInfos.isEmpty()){ 
	            	return publicStockCardInfos;
	            }
	            //公有库存数量
	            publicStockNumber= publicStockCardInfos.size(); 
	            //私库为空，公有库存足够直接返回公库
	            if(privateStockCardInfos.isEmpty() && publicStockNumber>=outNumber){
	            	return handleExtractResult(publicStockCardInfos, orderId);
	            	
	            }else if((publicStockNumber+privateStockNumber)>=outNumber){ //私库+公库 >= 出库
	            	publicStockCardInfos.addAll(privateStockCardInfos);
	            	return handleExtractResult(publicStockCardInfos, orderId);
	            }else{
	            	return new ArrayList<>(); //公私库库存都不足
	            }
	            
	        }
    	} catch (Exception e) {
    		iPlatformCardInfoMapper.unLockCards(updatePlatformCards);
    		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); //回滚事务
    		return new ArrayList<>();
		}
    }
    /**
     * 处理最终的提卡结果
     * @return
     */
    public List<PlatformCardInfo> handleExtractResult(List<PlatformCardInfo> extractCards,String orderId){
        //锁公库的卡
        int count =  iPlatformCardInfoMapper.lockCards(extractCards);
        if(count==0){
            return new ArrayList<>();
        }
        //解密数据
        extractCards = cardEncrypt.decodeCardEncrypt(extractCards);
        if(extractCards.isEmpty()){
            return new ArrayList<>();
        }
        //更新提卡订单号
        iPlatformCardInfoMapper.batchUpdateExtractCardOrder(extractCards, orderId, PlatformCardInfo.STATUS_UN_SALE, PlatformCardInfo.STATUS_SALE,0);
        return extractCards;
    }
}