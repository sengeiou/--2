package com.recharge.service.recharge.iml.tika;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.ExtractCardRechargeInfoBean;
import com.recharge.center.bean.HuaFeiRechargeInfoBean;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.domain.*;
import com.recharge.mapper.IChannelMapper;
import com.recharge.mapper.IProductSupRelationMapper;
import com.recharge.mapper.IRechargeOrderMapper;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.service.recharge.iml.MerchantCardServiceImpl;
import com.recharge.utils.RSAUtil;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TmallPurchaseCardBuyRequest;
import com.taobao.api.request.TmallPurchaseCardFetchRequest;
import com.taobao.api.response.TmallPurchaseCardBuyResponse;
import com.taobao.api.response.TmallPurchaseCardFetchResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @create 2021/1/12 14:50
 */
@Service
public class YaJieServiceImpl extends AbsChannelRechargeService {
    @Autowired
    protected IRechargeOrderMapper iRechargeOrderMapper;
    @Autowired
    MerchantCardServiceImpl merchantCardServiceImpl;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private JingDongCardServiceImpl jingDongCardService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private IChannelMapper iChannelMapper;

    @Autowired
    private NaiXueServiceImpl naiXueService;
    @Autowired
    private IProductSupRelationMapper iProductSupRelationMapper;
    @Autowired
    private CardStockService cardStockService;

    @Resource(name = "configMap")
    private Map<String, String> configMap;

    @Value("${tianmao.tk.rsa.privateKey}")
    private String privateKey;

    @Override
    public ProcessResult recharge(Channel channel, ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        ExtractCardRechargeInfoBean extractCardRechargeInfoBean = (ExtractCardRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(ExtractCardRechargeInfoBean.class);
        logger.info("卡库充值参数={},RechargeOrderBean={}",JSON.toJSONString(extractCardRechargeInfoBean),JSON.toJSONString(rechargeOrderBean));
        //平台卡密
        List<PlatformCardInfo> platformCardInfos = cardStockService.outNewCards(channelOrder.getOrderId(), channelOrder.getProductId(), extractCardRechargeInfoBean.getBuyNumber(), rechargeOrderBean.getMerchantId());
        if (CollectionUtils.isEmpty(platformCardInfos)) {
            //如果库存不足
            if (rechargeOrderBean.getProductName().contains("天猫超市享淘卡")) {
                logger.info("卡库无天猫超时享淘卡,开始天猫享淘卡提卡....");
                List<Map<String, String>> cards = TianMaoXTKbuy(rechargeOrderBean.getProductName(), channelOrder.getOrderId(), extractCardRechargeInfoBean.getBuyNumber().toString());
                if (!cards.isEmpty()) {
                    insertBuyCard(cards, rechargeOrderBean);
                    ProductSupRelation productSupRelation = iProductSupRelationMapper.selectCost(rechargeOrderBean.getProductId(), channel.getChannelId(), rechargeOrderBean.getLevel());
                    if (!(productSupRelation == null)) {
                        BigDecimal cost = productSupRelation.getCost();
                        BigDecimal allCost = cost.multiply(new BigDecimal(extractCardRechargeInfoBean.getBuyNumber())).setScale(2, BigDecimal.ROUND_HALF_UP);
                        iRechargeOrderMapper.updateBuyCardInfo(rechargeOrderBean.getOrderId(), channel.getChannelId(), channel.getChannelName(), rechargeOrderBean.getSalePrice(), rechargeOrderBean.getRechargeInfo(), allCost);
                        rechargeOrderBean.setSupId(channelOrder.getChannelId());
                        rechargeOrderBean.setSupName(channel.getChannelName());
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                logger.info("InterruptedException", e);
                            }
                            ResponseOrder responseOrder = new ResponseOrder();
                            responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                            responseOrder.setResponseCode("00");
                            channelService.callBack(channel.getChannelId(), responseOrder);
                        }
                    }).start();
                    return new ProcessResult(ProcessResult.SUCCESS, "订单成功");
                }
            }
            if (rechargeOrderBean.getProductName().contains("奈雪")) {
                logger.info("卡库无奈雪卡,去奈雪官方提卡.......");
                List<Map<String, String>> cards = naiXueService.NaiXueBuyCode(channelOrder.getOrderId(), extractCardRechargeInfoBean.getBuyNumber().toString(), rechargeOrderBean.getProductName());
                if (!cards.isEmpty()) {

                    ProductSupRelation productSupRelation = iProductSupRelationMapper.selectCost(rechargeOrderBean.getProductId(), channel.getChannelId(), rechargeOrderBean.getLevel());
                    if (!(productSupRelation == null)) {
                        BigDecimal cost = productSupRelation.getCost();
                        BigDecimal allCost = cost.multiply(new BigDecimal(extractCardRechargeInfoBean.getBuyNumber())).setScale(2, BigDecimal.ROUND_HALF_UP);
                        iRechargeOrderMapper.updateBuyCardInfo(rechargeOrderBean.getOrderId(), channel.getChannelId(), channel.getChannelName(), rechargeOrderBean.getSalePrice(), rechargeOrderBean.getRechargeInfo(), allCost);
                        rechargeOrderBean.setSupId(channelOrder.getChannelId());
                        rechargeOrderBean.setSupName(channel.getChannelName());
                    }
                    insertBuyCard(cards, rechargeOrderBean);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                logger.error("InterruptedException", e);
                            }
                            ResponseOrder responseOrder = new ResponseOrder();
                            responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                            responseOrder.setResponseCode("00");
                            channelService.callBack(channel.getChannelId(), responseOrder);
                        }
                    }).start();
                    return new ProcessResult(ProcessResult.SUCCESS, "订单成功");
                }
            }
            if (rechargeOrderBean.getProductName().contains("京东")) {
                logger.info("卡库无京东卡,开始京东官方提卡.......");
                List<Map<String, String>> cards = jingDongCardService.recharge(channelOrder.getOrderId(), extractCardRechargeInfoBean.getBuyNumber().toString(), rechargeOrderBean.getProductName());
                if (!cards.isEmpty()) {
                    if (StringUtils.equals(cards.get(0).get(BuyCardInfo.KEY_CARD_PWD), "ToKen校验失败")) {
                        cards = jingDongCardService.recharge(channelOrder.getOrderId(), extractCardRechargeInfoBean.getBuyNumber().toString(), rechargeOrderBean.getProductName());
                        if (!cards.isEmpty() && StringUtils.equals(cards.get(0).get(BuyCardInfo.KEY_CARD_PWD), "ToKen校验失败")) {
                            //尝试两次订单失败

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(1500);
                                    } catch (InterruptedException e) {
                                        logger.error("InterruptedException", e);
                                    }
                                    ResponseOrder responseOrder = new ResponseOrder();
                                    responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                                    responseOrder.setResponseCode("01");
                                    channelService.callBack(channel.getChannelId(), responseOrder);
                                }
                            }).start();

                            return new ProcessResult(ProcessResult.FAIL, "订单失败");
                        }
                    }
                }
                //查询订单是否存在
                String jdOrderId = jingDongCardService.queryJDOrderId(channelOrder.getOrderId());
                if ("404".equals(jdOrderId)) {


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                logger.error("InterruptedException", e);
                            }
                            ResponseOrder responseOrder = new ResponseOrder();
                            responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                            responseOrder.setResponseCode("01");
                            channelService.callBack(channel.getChannelId(), responseOrder);
                        }
                    }).start();

                    return new ProcessResult(ProcessResult.FAIL, "订单失败");
                }
                ProductSupRelation productSupRelation = iProductSupRelationMapper.selectCost(rechargeOrderBean.getProductId(), channel.getChannelId(), rechargeOrderBean.getLevel());
                if (!(productSupRelation == null)) {
                    BigDecimal cost = productSupRelation.getCost();
                    BigDecimal allCost = cost.multiply(new BigDecimal(extractCardRechargeInfoBean.getBuyNumber())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    iRechargeOrderMapper.updateBuyCardInfo(rechargeOrderBean.getOrderId(), channel.getChannelId(), channel.getChannelName(), rechargeOrderBean.getSalePrice(), rechargeOrderBean.getRechargeInfo(), allCost);
                }

                return new ProcessResult(ProcessResult.SUCCESS, "订单成功");
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        logger.error("InterruptedException", e);
                    }
                    ResponseOrder responseOrder = new ResponseOrder();
                    responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                    responseOrder.setResponseCode("01");
                    channelService.callBack(channel.getChannelId(), responseOrder);
                }
            }).start();

            return new ProcessResult(ProcessResult.FAIL, "订单失败");
        } else {
            PlatformCardInfo platformCardInfo = platformCardInfos.get(0);
            BigDecimal cost = platformCardInfo.getCost();
            BigDecimal allCost = cost.multiply(new BigDecimal(extractCardRechargeInfoBean.getBuyNumber())).setScale(2, BigDecimal.ROUND_HALF_UP);
            rechargeOrderBean.setSupId(platformCardInfo.getSupId());
            rechargeOrderBean.setSupName(platformCardInfo.getSupName());
            rechargeOrderBean.setCost(allCost);
            int i = iRechargeOrderMapper.updateBuyCardInfo(rechargeOrderBean.getOrderId(), platformCardInfo.getSupId(), platformCardInfo.getSupName(), rechargeOrderBean.getSalePrice(), rechargeOrderBean.getRechargeInfo(), allCost);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        logger.error("InterruptedException", e);
                    }
                    ResponseOrder responseOrder = new ResponseOrder();
                    responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                    responseOrder.setResponseCode("00");
                    channelService.callBack(channel.getChannelId(), responseOrder);
                }
            }).start();

            merchantCardServiceImpl.insertByBatch(platformCardInfos,rechargeOrderBean.getOrderId());
            return new ProcessResult(ProcessResult.SUCCESS, "订单成功");
        }

    }

    private void insertBuyCard(List<Map<String, String>> cards, RechargeOrderBean rechargeOrderBean) {
        if (CollectionUtils.isEmpty(cards)) return;
        List<PlatformCardInfo> list = cards.stream().map(item -> {
            PlatformCardInfo cardInfo = new PlatformCardInfo();
            cardInfo.setCardNo(item.get(BuyCardInfo.KEY_CARD_PWD));
            cardInfo.setCardPwd(item.get(BuyCardInfo.KEY_CARD_PWD));
            cardInfo.setCustomerId(rechargeOrderBean.getMerchantId());
            cardInfo.setOrderId(rechargeOrderBean.getOrderId());
            cardInfo.setProductId(rechargeOrderBean.getProductId());
            cardInfo.setProductName(rechargeOrderBean.getProductName());
            cardInfo.setSupId(rechargeOrderBean.getSupId());
            String time = item.get(BuyCardInfo.KEY_CARD_EXP_TIME);
            if (StringUtils.isNotEmpty(time)) {
                Date endDate = null;
                try {
                    endDate = DateUtils.parseDate(time, "yyyy-MM-dd hh:mm:ss");
                } catch (Exception e) {
                }

                try {
                    endDate = DateUtils.parseDate(time, "yyyy-MM-dd");
                } catch (Exception e) {
                }
                cardInfo.setExpireTime(endDate);
            }

            return cardInfo;
        }).collect(Collectors.toList());
        merchantCardServiceImpl.insertByBatch(list,rechargeOrderBean.getOrderId());
    }

    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        if (channelOrder.getProductName().contains("京东")) {//如果是京东卡需要真的去查询
            String jdOrderId = jingDongCardService.queryJDOrderId(channelOrder.getOrderId());
            if ("404".equals(jdOrderId)) {
                logger.error("京东订单查询无此订单数据，{}", channelOrder);
                return new ProcessResult(ProcessResult.UNKOWN, "订单不存在");
            }

            List<Map<String, String>> list = jingDongCardService.queryCardPWD(jdOrderId);
            if(CollectionUtils.isEmpty(list)){
            	return new ProcessResult(ProcessResult.PROCESSING, "处理中");
            }
            RechargeOrder rechargeOrder = iRechargeOrderMapper.selectByOrderId(channelOrder.getOrderId());

            RechargeOrderBean rechargeOrderBean = new RechargeOrderBean();
            BeanUtils.copyProperties(rechargeOrder, rechargeOrderBean);
            insertBuyCard(list, rechargeOrderBean);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        logger.error("InterruptedException", e);
                    }
                    ResponseOrder responseOrder = new ResponseOrder();
                    responseOrder.setChannelOrderId(channelOrder.getChannelOrderId());
                    responseOrder.setResponseCode("00");
                    channelService.callBack(channel.getChannelId(), responseOrder);
                }
            }).start();
            return new ProcessResult(ProcessResult.SUCCESS, "卡密查询成功");
        }
        return new ProcessResult(ProcessResult.PROCESSING, "处理中");
    }

    @Override
    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        if (StringUtils.equals("00", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.SUCCESS, "充值成功");
        } else if (StringUtils.equals("01", responseOrder.getResponseCode())) {
            return new ProcessResult(ProcessResult.FAIL, "充值失败");
        } else {
            return new ProcessResult(ProcessResult.UNKOWN, "结果可疑");
        }
    }


    /**
     * 天猫享淘卡买卡提卡
     *
     * @return
     */
    public List<Map<String, String>> TianMaoXTKbuy(String productName, String rechargeOrderId, String buyNumber) {
        //获取渠道信息
        Channel channel = iChannelMapper.selectByChannelId(configMap.get("xtk_channel"));
        if (channel == null) {
            logger.info("渠道未配置....无法提卡");
            return new ArrayList<Map<String, String>>();
        }
        //读取配置文件，获取对应的信息
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String url = configJSONObject.getString("rechargeUrl");
        String appKey = configJSONObject.getString("appKey");
        String appSecret = configJSONObject.getString("appSecret");
//        根据产品名称获取产品面值  天猫享淘卡100元
        //提取产品中面值数字
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(productName);
        String num = m.replaceAll(" ").trim();
        Long amt = Long.valueOf(num) * 100;
//        创建淘宝客户端连接对象
        TaobaoClient buy_client = new DefaultTaobaoClient(url, appKey, appSecret);
        //创建天猫超市享淘卡参数对象
        TmallPurchaseCardBuyRequest buy_req = new TmallPurchaseCardBuyRequest();
        TmallPurchaseCardBuyRequest.CardBuyRequest cardBuyRequest = new TmallPurchaseCardBuyRequest.CardBuyRequest();
        //设置外部订单ID(必填)
        cardBuyRequest.setOuterOrderId(rechargeOrderId); //2
        //设置卡类型(1: 猫超卡，必填)
        cardBuyRequest.setCardType(1L);
        //设置面值,单位分(必填)
        cardBuyRequest.setParValue(Long.valueOf(amt));
        //设置购卡数量，不能大于10000(必填)
        cardBuyRequest.setAmount(Long.valueOf(buyNumber));
        //将cardBuyRequest写入到TmallPurchaseCardBuyRequest中
        buy_req.setCardBuyReq(cardBuyRequest);
        TmallPurchaseCardBuyResponse buy_rsp = null;
        try {
            logger.info("天猫享淘卡购买" + "==>请求信息==>" + JSON.toJSONString(buy_req));
            buy_rsp = buy_client.execute(buy_req);
            logger.info("天猫享淘卡购买" + "==>响应信息==>" + JSON.toJSONString(buy_rsp));
        } catch (ApiException e) {
            logger.error("invoke TianMaoXTK client error {}", e.getErrMsg());
        }
        if (buy_rsp.isSuccess() && buy_rsp.getResult().getSuccess()) {
//            提交成功之后进行提卡
            TaobaoClient fetch_client = new DefaultTaobaoClient(url, appKey, appSecret);
            TmallPurchaseCardFetchRequest fetch_req = new TmallPurchaseCardFetchRequest();
            TmallPurchaseCardFetchRequest.CardFetchRequest cardFetchRequest = new TmallPurchaseCardFetchRequest.CardFetchRequest();
            cardFetchRequest.setOuterOrderId(rechargeOrderId);
            fetch_req.setCardFetchReq(cardFetchRequest);
            List<TianMaoXTK> cardDTOS = null;
            TmallPurchaseCardFetchResponse fetch_rsp = null;
            List<Map<String, String>> cardInfos = new ArrayList<>();
            try {
                logger.info("天猫享淘卡提卡" + "==>请求信息==>" + JSON.toJSONString(fetch_req));
                fetch_rsp = fetch_client.execute(fetch_req);
                logger.info("天猫享淘卡提卡" + "==>响应信息==>" + JSON.toJSONString(fetch_rsp));
                String responseJson = JSON.toJSONString(fetch_rsp);
                String data = JSONObject.parseObject(responseJson).getString("result");
                String response1 = JSONObject.parseObject(data).getString("response");
                String cards = JSONObject.parseObject(response1).getString("cards");
                JSONArray jsonArray = new JSONArray(JSON.parseArray(cards));
                cardDTOS = JSONObject.parseArray(jsonArray.toJSONString(), TianMaoXTK.class);
                //卡密解密
                for (TianMaoXTK cardDTO : cardDTOS) {
                    String cardPass = cardDTO.getCardPass();
                    cardDTO.setCardPass(RSAUtil.decrypt(cardPass, privateKey));
                    Map<String, String> infosMap = new HashMap<>();
                    infosMap.put(BuyCardInfo.KEY_CARD_NO, cardDTO.getCardNo());
                    infosMap.put(BuyCardInfo.KEY_CARD_PWD, cardDTO.getCardPass());
                    infosMap.put(BuyCardInfo.KEY_CARD_EXP_TIME, new SimpleDateFormat("yyyy-MM-dd").format(cardDTO.getExpiredDate()));
                    cardInfos.add(infosMap);
                }
            } catch (Exception e) {
                logger.error("提卡失败{}", e.getMessage());
            }
            return cardInfos;
        } else {
            //提交失败的话
            logger.info("购卡失败");
            return new ArrayList<>();
        }
    }


}

