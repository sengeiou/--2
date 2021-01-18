package com.recharge.service.recharge.iml.tika;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.recharge.common.utils.HttpClientUtils;
import com.recharge.domain.PlatformCardInfo;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**卡密加密和解密
 * @author Administrator
 * @create 2020/5/27 15:11
 */
@Service
public class CardEncrypt {static Logger logger = LoggerFactory.getLogger(CardEncrypt.class);
    static  final String url="http://security.yaajie.com:8012";

    static  final String key= DigestUtils.md5Hex("ow6!QF9Wr5wKgcMAvIfS1BHQNyoj5NP7");

    public  List<PlatformCardInfo> sendCardEncrypt(List<PlatformCardInfo> platformCardInfos){
        Map<String,Object> map=new HashMap<>();
        map.put("platformCardInfos",platformCardInfos);
        map.put("sign",key);
        String requestString = JSONObject.toJSONString(map);
        List<PlatformCardInfo> platformCardInfo=new ArrayList<>();
        try{
            logger.info("send recharge request params:{}", requestString);
            String responseBody = HttpClientUtils.invokePostString(url+"/enc/platformcard", new StringEntity(requestString,"utf-8"),"UTF-8",20000);
            logger.info("send recharge response :{}", responseBody);

            JSONArray result = JSONObject.parseObject(responseBody).getJSONArray("result");
            String errCode = JSONObject.parseObject(responseBody).getString("errCode");
            if (StringUtils.equals(errCode,"0")){
                platformCardInfo=JSONObject.parseArray(result.toJSONString(), PlatformCardInfo.class);;
            }else {
                logger.info("responseBody  cardPwd fail:{}", responseBody);
            }
        }catch (Exception e){
            logger.info("request  fail:{}", e);
        }
        return  platformCardInfo;
    }


    public  List<PlatformCardInfo> decodeCardEncrypt(List<PlatformCardInfo> platformCardInfos){
        Map<String,Object> map=new HashMap<>();
        map.put("platformCardInfos",platformCardInfos);
        map.put("sign",key);
        String requestString = JSONObject.toJSONString(map);
        List<PlatformCardInfo> platformCardInfo=new ArrayList<>();
        try{
            logger.info("send recharge request params:{}", requestString);
            String responseBody = HttpClientUtils.invokePostString(url+"/dec/platformcard", new StringEntity(requestString,"utf-8"),"UTF-8",20000);
            logger.info("send recharge response :{}", responseBody);
            JSONArray result = JSONObject.parseObject(responseBody).getJSONArray("result");
            String errCode = JSONObject.parseObject(responseBody).getString("errCode");
            if (StringUtils.equals(errCode,"0")){
                platformCardInfo = JSONObject.parseArray(result.toJSONString(), PlatformCardInfo.class);
            }else {
                logger.info("responseBody  cardPwd fail:{}", responseBody);
            }
        }catch (Exception e){
            logger.info("request  fail:{}", e);
        }
        return  platformCardInfo;
    }
}
