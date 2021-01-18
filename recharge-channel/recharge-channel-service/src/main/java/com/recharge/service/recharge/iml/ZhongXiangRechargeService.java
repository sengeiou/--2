package com.recharge.service.recharge.iml;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.recharge.bean.ProcessResult;
import com.recharge.bean.ResponseOrder;
import com.recharge.center.bean.RechargeOrderBean;
import com.recharge.center.bean.ZhifubaoRechargeInfoBean;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.Channel;
import com.recharge.domain.ChannelOrder;
import com.recharge.mapper.IChannelOrderMapper;
import com.recharge.mapper.ISequenceMapper;
import com.recharge.service.ChannelService;
import com.recharge.service.recharge.AbsChannelRechargeService;
import com.recharge.utils.ConstantsUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qi.cao on 2016/5/20.
 */
@Service
public class ZhongXiangRechargeService extends AbsChannelRechargeService {

    @Autowired
    private ISequenceMapper iSequenceMapper;

    @Autowired
    private IChannelOrderMapper iChannelOrderMapper;

    @Autowired
    private ChannelService channelService;

    private Map<String,String> codeMap = new HashMap<>();

    private String channelId = "100002";

    private Logger logger = LoggerFactory.getLogger(getClass());

    public ZhongXiangRechargeService() {
        codeMap.put("RECEIVE_ACCOUNT_ERROR","账户不存在或姓名不正确");
        codeMap.put("DETAIL_CONSULT_CHECK_ERROR","支付宝内部错误");
        codeMap.put("ACCOUNT_NOT_MATCH","文件信息和账户信息不匹配或付款账户非正常状态");
        codeMap.put("USER_NOT_EXIST","用户不存在");
        codeMap.put("USER_BLOCKED","付款账户余额支付功能关闭");
        codeMap.put("batchFeeIllegal","总金额只能为货币类型");
        codeMap.put("batchFeesIsNull","总金额为空");
        codeMap.put("detailDataIsNull","单笔数据集为空");
        codeMap.put("ERROR_SELF_CERTIFY_LEVEL_LIMIT","您暂时无法使用此功能，请立即补全您的认证信息");
        codeMap.put("TOTAL_NUMS_LIMIT","超过最大笔数限制");
        codeMap.put("SELLER_NOT_CERTIFY","付款方没有通过实名认证");
        codeMap.put("UPLOAD_FAIL","上传失败");
        codeMap.put("emailIsNull","付款人 Email 为空");
        codeMap.put("baccountNameIsNull","账户名称为空");
        codeMap.put("TRANS_NO_NOT_UNIQUE","商户提交的该批次已存在");
        codeMap.put("PAY_EMAIL_NAME_NOT_MATCH","付款人 email 账号与姓名不匹配");
        codeMap.put("batchNoIsNull","批次号为空");
        codeMap.put("MAX_SINGLE_FEE","单笔最大金额超出限制");
        codeMap.put("NOT_ENOUGH_AMOUNT","付款账号余额不足");
        codeMap.put("BATCH_MONEY_ADD_NOT_MATCH_AMOUNT","批次累加金额和付款总额不匹配");
        codeMap.put("MAX_SINGLE_FEE_FOR_CERT","都是认证商家并超过限额");
        codeMap.put("partnerORPayEmailError","合作伙伴 Email 和付款人 Email 不一致");
        codeMap.put("BATCH_NO_ERROR","文件批次号错误");
        codeMap.put("BATCH_COUNT_ADD_NOT_MATCH_AMOUNT","批次累加笔数和付款笔数不匹配");
        codeMap.put("batchNumsIsNull","付款总笔数为空");
        codeMap.put("BATCH_ILLEGAL","批次信息非法");
        codeMap.put("TOTAL_COUNT_NOT_MATCH","总笔数与明细汇总笔数不一致");
        codeMap.put("TOTAL_AMOUNT_NOT_MATCH","总金额与明细汇总金额不一致");
        codeMap.put("DAILY_QUOTA_LIMIT_EXCEED","日限额超限");
        codeMap.put("PAYER_ACCOUNT_IS_RELEASED","付款人登录账号存在多个重复账户，无法确认唯一");
        codeMap.put("PAYEE_ACCOUNT_IS_RELEASED","收款人登录账号存在多个重复账户，无法确认唯一");
        codeMap.put("PERMIT_NON_BANK_LIMIT_PAYEE_L0_FORBIDDEN","根据监管部门的要求，对方未完善身份信息，无法收款");
        codeMap.put("PERMIT_PAYER_L1_FORBIDDEN","根据监管部门的要求，当前余额支付额度仅剩 XX 元，请尽快完善身份信息提升额度");
        codeMap.put("PERMIT_PAYER_L2_L3_FORBIDDEN","根据监管部门的要求，您今日余额支付额度仅剩 XX元，今年余额支付额度仅剩 XX 元");
        codeMap.put("PERMIT_CHECK_PERM_AML_DATA_INCOMPLETE","由于您的资料不全，付款受限");
        codeMap.put("PERMIT_CHECK_PERM_AML_CERT_EXPIRED","由于您的证件过期，付款受限");
        codeMap.put("PERMIT_CHECK_PERM_AML_CERT_MISS_4_TRANS_LIMIT","您的账户收付款额度超限");
        codeMap.put("PERMIT_CHECK_PERM_AML_CERT_MISS_4_ACC_LIMIT","为了保证您的资金安全，请尽快完成信息补全");
        codeMap.put("PERMIT_CHECK_PERM_IDENTITY_THEFT","您的账户存在身份冒用风险，请进行身份核实解除限制");
        codeMap.put("PERMIT_CHECK_PERM_LIMITED_BY_SUPERIOR","根据监管部门的要求，请补全您的身份信息解除限制");
        codeMap.put("PERMIT_CHECK_PERM_ACCOUNT_NOT_EXIST","根据监管部门的要求，请补全您的身份信息，开立余额账户");
        codeMap.put("PERMIT_CHECK_PERM_INSUFFICIENT_ACC_LEVEL","根据监管部门的要求，请完善身份信息解除限制");
    }


    public ProcessResult recharge(Channel channel,ChannelOrder channelOrder, RechargeOrderBean rechargeOrderBean) {
        /**
         *configinfo {partner:"B500015",md5_key:"5909f514c8c34518df7bb995ad52f26f",cp_code:"15161133001",prod_code:"B30001",rechargeUrl:"http://interface.chongqilai.com/b2b/doRecharge.shtml"}
        */
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());

        ZhifubaoRechargeInfoBean zhifubaoRechargeInfoBean = (ZhifubaoRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(ZhifubaoRechargeInfoBean.class);

        String requestUrl = configJSONObject.getString("rechargeUrl");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("client_ip", configJSONObject.getString("client_ip"));    //	客户端IP
        requestMap.put("cp_code", configJSONObject.getString("cp_code"));    //商户编码
        requestMap.put("cp_tran_no", channelOrder.getChannelOrderId());//商户订单号，确保平台唯一性 字母、数字或字线数字组合
        requestMap.put("partner", configJSONObject.getString("partner"));
        requestMap.put("income_account", zhifubaoRechargeInfoBean.getIncomeAccount());    //	充值账户
        requestMap.put("income_remark", zhifubaoRechargeInfoBean.getIncomeRemark());    //	充值描述
        requestMap.put("income_user_name", zhifubaoRechargeInfoBean.getIncomeUserName());    //	充值账户姓名
        requestMap.put("notify_url", configJSONObject.getString("notify_url"));    //	异步通知地址
        requestMap.put("prod_code", configJSONObject.getString("prod_code"));    //商品编码
        requestMap.put("prod_num", zhifubaoRechargeInfoBean.getNum());    //	商品数量
        requestMap.put("time", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));    //商户订单提交时间,精确到秒格式：yyyyMMddHHmmss

        String sourceString = requestMap.get("client_ip")+"|"+requestMap.get("cp_code")+"|"+requestMap.get("cp_tran_no")
                +"|"+requestMap.get("income_account")+"|"+requestMap.get("income_remark")+"|"+requestMap.get("income_user_name")
                +"|"+requestMap.get("notify_url")
                +"|"+requestMap.get("partner")+"|"+requestMap.get("prod_code")+"|"+requestMap.get("prod_num")+"|"+requestMap.get("time")
                +"|"+configJSONObject.getString("md5_key");
        logger.info("加密原串:{}",sourceString);
        String signOne = DigestUtils.md5Hex(sourceString) ;
        String signTwo = DigestUtils.md5Hex(signOne+"|"+configJSONObject.getString("md5_key"));
        requestMap.put("sign", signTwo);//签名(详情见签名规则)

        try {
            logger.info("{},发送充值的参数:{}",rechargeOrderBean.getOrderId(),JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, requestMap, configJSONObject.getString("encoding"));

            logger.info("{},收到充值的响应:{}",rechargeOrderBean.getOrderId(),responseBody);
            JSONObject jsonObject = JSON.parseObject(responseBody);



            /*tran_state=3时，描述失败原因*/
            String errorMsg = jsonObject.getString("error_msg");
            /*
            * 000000成功、222222处理中
                999999失败、555555订单存在
            * */
            String retCode = jsonObject.getString("ret_code");
            String retMsg = jsonObject.getString("ret_msg");
            if (StringUtils.equals("000000",retCode)
                    ||StringUtils.equals("222222",retCode)){
                                /*订单状态
                0-生成订单、1-处理中、
                2-交易成功、3-交易失败
                */
                String tranState = jsonObject.getString("tran_state");
                if (StringUtils.equals("0",tranState)||
                        StringUtils.equals("1",tranState)){
                    return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
                }else if (StringUtils.equals("3",tranState)){
                    return new ProcessResult(ProcessResult.FAIL,codeMap.get(errorMsg));
                }else{
                    return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
                }
            }else if (StringUtils.equals("999999",retCode)){
                return new ProcessResult(ProcessResult.FAIL,retMsg);
            }else{
                return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
            }
            /*1、当ret_code=000000时，下面的字段有值
            2、当ret_code=999999或222222时，下面的字段没有值
            3、当ret_code=555555时，商户提交的订单号已经存在的，cp_tran_no、tran_no、tran_state这三个有值
            */
//            String retMsg = jsonObject.getString("ret_msg");
        } catch (ConnectTimeoutException connectException){
            logger.error("{}发送异常",rechargeOrderBean.getOrderId(),connectException);
            return new ProcessResult(ProcessResult.FAIL,"提交失败");
        }catch (Exception e) {
            logger.error("{}发送异常",rechargeOrderBean.getOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
        }
    }


    @Override
    public ProcessResult query(Channel channel, ChannelOrder channelOrder) {
        /**
         *configinfo {partner:"B500015",md5_key:"5909f514c8c34518df7bb995ad52f26f",cp_code:"15161133001",prod_code:"B30001",requestUrl:"http://interface.chongqilai.com/b2b/doRecharge.shtml"}
         */
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String requestUrl = configJSONObject.getString("queryUrl");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("cp_code", configJSONObject.getString("cp_code"));    //商户编码
        requestMap.put("cp_tran_no", channelOrder.getChannelOrderId());//商户订单号，确保平台唯一性 字母、数字或字线数字组合
        requestMap.put("partner", configJSONObject.getString("partner"));
        String sourceString = requestMap.get("cp_code")+"|"+requestMap.get("cp_tran_no")
                +"|"+requestMap.get("partner")
                +"|"+configJSONObject.getString("md5_key");
        logger.info("加密原串:{}",sourceString);
        String signOne = DigestUtils.md5Hex(sourceString) ;
        String signTwo = DigestUtils.md5Hex(signOne+"|"+configJSONObject.getString("md5_key"));
        requestMap.put("sign", signTwo);//签名(详情见签名规则)

        try {
            logger.info("{},发送查询的参数:{}",channelOrder.getChannelOrderId(),JSON.toJSONString(requestMap));
            String responseBody = HttpClientUtils.invokePostHttp(requestUrl, requestMap, configJSONObject.getString("encoding"));

            logger.info("{},收到查询的响应:{}",channelOrder.getChannelOrderId(),responseBody);
            JSONObject jsonObject = JSON.parseObject(responseBody);


            String outChannelId = jsonObject.getString("tran_no");
            channelOrder.setOutChannelOrderId(outChannelId);
            /*tran_state=3时，描述失败原因*/
            String errorMsg = jsonObject.getString("error_msg");
            /*
            * 000000成功、222222处理中
                999999失败、555555订单存在
            * */
            String retCode = jsonObject.getString("ret_code");
            if (StringUtils.equals("000000",retCode)){
                                /*订单状态
                0-生成订单、1-处理中、
                2-交易成功、3-交易失败
                */
                String tranState = jsonObject.getString("tran_state");
                if (StringUtils.equals("0",tranState)||
                        StringUtils.equals("1",tranState)){
                    return new ProcessResult(ProcessResult.SUCCESS,"提交成功");
                }else if (StringUtils.equals("3",tranState)){
                    return new ProcessResult(ProcessResult.FAIL,codeMap.get(errorMsg.substring(errorMsg.indexOf("---")==-1?0:errorMsg.indexOf("---")+3))==null
                            ? "支付宝内部错误" :codeMap.get(errorMsg.substring(errorMsg.indexOf("---")==-1?0:errorMsg.indexOf("---")+3)));
                }else{
                    return new ProcessResult(ProcessResult.UNKOWN,"未知返回码");
                }
            }else if (StringUtils.equals("222222",retCode)){
                return new ProcessResult(ProcessResult.PROCESSING,"处理中");
            }else if (StringUtils.equals("999999",retCode)){
                return new ProcessResult(ProcessResult.FAIL,"失败失败");
            }else{
                return new ProcessResult(ProcessResult.UNKOWN,"提交可疑");
            }
            /*1、当ret_code=000000时，下面的字段有值
            2、当ret_code=999999或222222时，下面的字段没有值
            3、当ret_code=555555时，商户提交的订单号已经存在的，cp_tran_no、tran_no、tran_state这三个有值
            */
//            String retMsg = jsonObject.getString("ret_msg");


        } catch (Exception e) {
            logger.error("{}发送失败",channelOrder.getChannelOrderId(),e);
            return new ProcessResult(ProcessResult.UNKOWN,"提交成功");
        }
    }

    public ProcessResult parseResponse(ResponseOrder responseOrder) {
        /*0-生成订单、1-处理中、
        2-交易成功、3-交易失败
                */
        String tranState = responseOrder.getResponseCode();
        if (StringUtils.equals("2",tranState)){
            return new ProcessResult(ProcessResult.SUCCESS,"交易成功");
        }else if (StringUtils.equals("3",tranState)){
            ChannelOrder channelOrder = iChannelOrderMapper.selectByChannelOrderId(responseOrder.getChannelOrderId());
            Channel channel = channelService.queryChannelInfo(channelId);
            ProcessResult queryResult = query(channel,channelOrder);
            responseOrder.setResponseMsg(queryResult.getMsg());
            return new ProcessResult(ProcessResult.FAIL, queryResult.getMsg());
        }else{
            return new ProcessResult(ProcessResult.UNKOWN,"未知返回码");
        }
    }

    @Override
    public BigDecimal balanceQuery(Channel channel) {
        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
        String balanceQueryUrl = configJSONObject.getString("balanceQueryUrl");

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("userName", configJSONObject.getString("cp_code"));    //商户编码
        requestMap.put("partner", configJSONObject.getString("partner"));
        String sourceString = requestMap.get("partner")
                +"|"+requestMap.get("userName")
                +"|"+configJSONObject.getString("md5_key");
        logger.info("加密原串:{}",sourceString);
        String signOne = DigestUtils.md5Hex(sourceString) ;
        String signTwo = DigestUtils.md5Hex(signOne+"|"+configJSONObject.getString("md5_key"));
        requestMap.put("sign",signTwo);
        logger.info("{},requestParam:{}",channel.getChannelId(),JSON.toJSONString(requestMap));
        try {
            String responseBody = HttpClientUtils.invokePostHttp(balanceQueryUrl, requestMap, configJSONObject.getString("encoding"));
            logger.info("{},responseBody:{}",channel.getChannelId(),responseBody);
            JSONObject jsonObject = JSON.parseObject(responseBody);
            return new BigDecimal(jsonObject.getString("useableBalance"));
        } catch (Exception e) {
            logger.warn("balance warn ",e);
        }
        return new BigDecimal("9999999999");
    }
}
