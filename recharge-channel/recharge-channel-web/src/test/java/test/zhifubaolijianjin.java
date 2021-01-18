package test;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayMarketingCashvoucherTemplateCreateRequest;
import com.alipay.api.request.AlipayMarketingVoucherSendRequest;
import com.alipay.api.response.AlipayMarketingCashvoucherTemplateCreateResponse;
import com.alipay.api.response.AlipayMarketingVoucherSendResponse;

public class zhifubaolijianjin {

	public static void main(String[] args) throws AlipayApiException {
		try {
			createCodeTemplate();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("创建优惠券模板异常="+e.getMessage());
		}
		
//		try {
//			String abc = sendUser();
//			System.out.println("打印="+abc);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("发送优惠券模板异常="+e.getMessage());
//		}
	}
	
	
	public static String createCodeTemplate() throws Exception {
		String rechargeUrl = "https://openapi.alipay.com/gateway.do";//configJSONObject.getString("rechargeUrl");
        String appId = "2021002103618531";//configJSONObject.getString("appId");
        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCNkhLEY3YHebvmsz/HL1oiixldm+vzxVdlIk5JR4kKmrnzRQtDTAjsTGW+n4IKuSZ28tD4FBDQEth20Kjsui5ZKO5TDPcCnAcSJ+z281UMaBr3PBfPTsr0//rfuBnEET1Bcl/3dNOizZxARK3Xykj89CvBSoL6hDO+CXThoXXUP8Ui++a5biwiYrz8mz2aUOildzCKKosGPRnMvsvUh3rv9WMSDSy1pJw0dHOcKgHhB2hB+Sou3+Mc8d5LI6dIlxWWux47bQssrPR9u7n0yZIGFMn5hsD6Ic1Mx/4D3VqHMeaxsTdmzeqjfbMdrs1ZSljAJbGfsRvDB26mFhXUlhDtAgMBAAECggEAeBpXn5K9vEPTXDnvvpz2e93ELD0uZMWjiiIbhmGQ6pUvcim+D16/JzD9FEYgPdarwjfnfr8PMGb8i3gkB4k/7tYhsgWKvQT/nrCOgUcYfQkI73xkurQezNEp1YhLG6KAdDiDH91V+oUyrPdXAH4Vg7vmLvp2ue9UMkG1jgG7g881KUhCiabis4whEyVW4CD8G3+SaHWpi2HkjMEu1JYcVlBTwZH9ZSB9b9S2IQ0gkMA/Dkm9vECYGt0CpJJMOotF3E7jPK5eJ61d3juAX5Vkc8Bn8pmhaETYKRtrgcFl7foFK5WHwiSEYTZbX8M3BVrUCvKStOrVSQYLqMJTz6iLQQKBgQDzedJSCwrvPj4SrSmGb2+sTu/TuQtibCoGDgA5bJxk5GzMJgoQTToOTF6A6SjDR5pZNgI6NltdapzrWVNXpNbHGZ/oll/mSljWNZwr8gsAj4uWzs/6BUiBSGvI6kCG+BeeOdprJSol/exeqGxhFGWI9fiVI22/31RXMGmuli6k3QKBgQCU2lOF6SNJJWbzstZj/dE06dpD5KOyO1PpeBiC9GKOi3pASThdmK9G6O166+Z2Zo2I1KTH7lJvF2ztJ3gT8cCEO7FXun3CXRH8xdOGK0X+HfKsmm61hk9RzOXEAFpyO+ynHjO1XpO84o3eix5SIcWqarg2NmhEUfgEL8F2EL2TUQKBgA5s+lwoEVbJfk4G+n3fnmM8LhOCVD1ZoUfIjpTYbY/AxP13h4MdwEHWnobOyb6AQJHmlE00qUcoqPF80mZP+vXC++mA91+iqg7PEFi+N46p8qBV0pbCt2AJlXFLB9W3X1bdoKZWhl/H4rhdDEbYOYfa4wabGcaTieC8mpZgiZT1AoGAdQCwCmBZJhPKOuBl+K74FPmEYT2FFoHf0XJfAgCKv7UO4R0suxVbIQfSM37d5k9yDQ3ogGbqUPw+2KLflf2+77HkEis60t5JV0FApQO6vqZXrCivwOvTyV6fdFM6xPQpbqskxfmTczM5hKYhrTlyoCPGrIZTqvArXU8UaHN8MCECgYEA6Q7uYOqhWuvBDeA6oaOLVj4Dd6NFqbsGK2+BcyDXAdQU2T6GbXmE5YSTSSsacyiJv/J8I7LFF7FmEvmRTcC4nV5g3ZXyrsMs7nosiqeAeSpZKBfpl8YBjz7a3qZIcyDuH6NmO9zGj9Q+wrXsW0HtCvi28KxsRnxHk1TGZnK3Ifk=" ;//configJSONObject.getString("appPrivateKey");
        String certPath = "D:/channel-zhifubao-file/appCertPublicKey_2021002103618531.crt";//configJSONObject.getString("certPath");
        String alipayPublicCertPath = "D:/channel-zhifubao-file/alipayCertPublicKey_RSA2.crt";//configJSONObject.getString("alipayPublicCertPath");
        String rootCertPath = "D:/channel-zhifubao-file/alipayRootCert.crt";
        //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(rechargeUrl);
        certAlipayRequest.setAppId(appId);
        certAlipayRequest.setPrivateKey(appPrivateKey);
        certAlipayRequest.setFormat("json");
        certAlipayRequest.setCharset("GBK");
        certAlipayRequest.setSignType("RSA2");
        certAlipayRequest.setCertPath(certPath);
        certAlipayRequest.setAlipayPublicCertPath(alipayPublicCertPath);
        certAlipayRequest.setRootCertPath(rootCertPath);
        //证书请求
        AlipayClient alipayClient = new DefaultAlipayClient(certAlipayRequest);
        
        AlipayMarketingCashvoucherTemplateCreateRequest request = new AlipayMarketingCashvoucherTemplateCreateRequest();//创建API对应的request类
        request.setBizContent("{" +
        "\"voucher_type\":\"FIX_VOUCHER\"," +//券类型。暂时只支持"代金券"(FIX_VOUCHER)
        "\"voucher_use_scene\":\"ALIPAY_COMMON\"," +//券使用场景。 ALIPAY_COMMON
        "\"fund_account\":\"2514335050@qq.com\"," +//出资人登录账号。用于发券的资金会从该账号划拨到发券专用账户上。该账户必须为已实名认证的支付宝账户。
        "\"brand_name\":\"发券服务\"," +//创建券模板时录入的品牌信息，由商户自定义。
        "\"publish_start_time\":\"2020-11-11 00:00:01\"," +
        "\"publish_end_time\":\"2020-11-13 23:59:59\"," +
        "\"voucher_valid_period\":\"{\\\"type\\\":\\\"ABSOLUTE\\\",\\\"start\\\":\\\"2020-11-11 00:00:00\\\",\\\"end\\\":\\\"2020-11-13 23:59:59\\\"}\"," +
        "\"floor_amount\":5.0," +//最低额度。设置券使用门槛，只有订单金额大于等于最低额度时券才能使用。币种为人民币，单位为元。该数值不能小于0，小数点以后最多保留两位。
        "\"voucher_description\":\"[\\\"1、本券不可兑换现金，不可找零。\\\",\\\"2、每个用户最多可以领取1张。\\\",\\\"3、如果订单发生退款，优惠券无法退还。\\\"]\"," +
        "\"out_biz_no\":\"2020202020202\"," +//外部订单号，在商家系统保持唯一。
        "\"amount\":1," +//面额。每张代金券可以抵扣的金额。
        "\"voucher_quantity\":5," +
        //"\"redirect_uri\":\"https://www.yourdomain.com/alipay/pay/success\"," +
        //"\"notify_uri\":\"https://www.yourdomain.com/reieve/voucher/flux\"," +
        "\"rule_conf\":\"{\\\"bizProduct\\\":\\\"QUICK_MSECURITY_PAY,QR_CODE_OFFLINE,ALIPAY_F2F_PAYMENT,TAOBAO_FAST_PAY\\\"}\"," +////核销场景线下支付
        //"\"extension_info\":\"{\\\"useMode\\\":\\\"H5\\\",\\\"useModeData\\\":{\\\"url\\\":\\\"http://www.yourdomian.com/yourusepage.htm\\\",\\\"signType\\\":\\\"RSA2\\\"}}\"" +
        "}");//设置业务参数
        AlipayMarketingCashvoucherTemplateCreateResponse response = alipayClient.certificateExecute(request);
        System.out.println("返回response="+JSON.toJSONString(response));
        String responseStr = "";
        if(response.isSuccess()){
        	responseStr = "调用成功";
        } else {
        	responseStr = "调用失败";
        }// 根据response中的结果继续业务逻辑处理
        return responseStr;
	}
	
	/**
	 * 发券给用户
	 * @return
	 * @throws AlipayApiException 
	 */
	public static String sendUser() throws AlipayApiException{
		String rechargeUrl = "https://openapi.alipay.com/gateway.do";//configJSONObject.getString("rechargeUrl");
        String appId = "2021002103618531";//configJSONObject.getString("appId");
        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCNkhLEY3YHebvmsz/HL1oiixldm+vzxVdlIk5JR4kKmrnzRQtDTAjsTGW+n4IKuSZ28tD4FBDQEth20Kjsui5ZKO5TDPcCnAcSJ+z281UMaBr3PBfPTsr0//rfuBnEET1Bcl/3dNOizZxARK3Xykj89CvBSoL6hDO+CXThoXXUP8Ui++a5biwiYrz8mz2aUOildzCKKosGPRnMvsvUh3rv9WMSDSy1pJw0dHOcKgHhB2hB+Sou3+Mc8d5LI6dIlxWWux47bQssrPR9u7n0yZIGFMn5hsD6Ic1Mx/4D3VqHMeaxsTdmzeqjfbMdrs1ZSljAJbGfsRvDB26mFhXUlhDtAgMBAAECggEAeBpXn5K9vEPTXDnvvpz2e93ELD0uZMWjiiIbhmGQ6pUvcim+D16/JzD9FEYgPdarwjfnfr8PMGb8i3gkB4k/7tYhsgWKvQT/nrCOgUcYfQkI73xkurQezNEp1YhLG6KAdDiDH91V+oUyrPdXAH4Vg7vmLvp2ue9UMkG1jgG7g881KUhCiabis4whEyVW4CD8G3+SaHWpi2HkjMEu1JYcVlBTwZH9ZSB9b9S2IQ0gkMA/Dkm9vECYGt0CpJJMOotF3E7jPK5eJ61d3juAX5Vkc8Bn8pmhaETYKRtrgcFl7foFK5WHwiSEYTZbX8M3BVrUCvKStOrVSQYLqMJTz6iLQQKBgQDzedJSCwrvPj4SrSmGb2+sTu/TuQtibCoGDgA5bJxk5GzMJgoQTToOTF6A6SjDR5pZNgI6NltdapzrWVNXpNbHGZ/oll/mSljWNZwr8gsAj4uWzs/6BUiBSGvI6kCG+BeeOdprJSol/exeqGxhFGWI9fiVI22/31RXMGmuli6k3QKBgQCU2lOF6SNJJWbzstZj/dE06dpD5KOyO1PpeBiC9GKOi3pASThdmK9G6O166+Z2Zo2I1KTH7lJvF2ztJ3gT8cCEO7FXun3CXRH8xdOGK0X+HfKsmm61hk9RzOXEAFpyO+ynHjO1XpO84o3eix5SIcWqarg2NmhEUfgEL8F2EL2TUQKBgA5s+lwoEVbJfk4G+n3fnmM8LhOCVD1ZoUfIjpTYbY/AxP13h4MdwEHWnobOyb6AQJHmlE00qUcoqPF80mZP+vXC++mA91+iqg7PEFi+N46p8qBV0pbCt2AJlXFLB9W3X1bdoKZWhl/H4rhdDEbYOYfa4wabGcaTieC8mpZgiZT1AoGAdQCwCmBZJhPKOuBl+K74FPmEYT2FFoHf0XJfAgCKv7UO4R0suxVbIQfSM37d5k9yDQ3ogGbqUPw+2KLflf2+77HkEis60t5JV0FApQO6vqZXrCivwOvTyV6fdFM6xPQpbqskxfmTczM5hKYhrTlyoCPGrIZTqvArXU8UaHN8MCECgYEA6Q7uYOqhWuvBDeA6oaOLVj4Dd6NFqbsGK2+BcyDXAdQU2T6GbXmE5YSTSSsacyiJv/J8I7LFF7FmEvmRTcC4nV5g3ZXyrsMs7nosiqeAeSpZKBfpl8YBjz7a3qZIcyDuH6NmO9zGj9Q+wrXsW0HtCvi28KxsRnxHk1TGZnK3Ifk=" ;//configJSONObject.getString("appPrivateKey");
        String certPath = "D:/channel-zhifubao-file/appCertPublicKey_2021002103618531.crt";//configJSONObject.getString("certPath");
        String alipayPublicCertPath = "D:/channel-zhifubao-file/alipayCertPublicKey_RSA2.crt";//configJSONObject.getString("alipayPublicCertPath");
        String rootCertPath = "D:/channel-zhifubao-file/alipayRootCert.crt";
        //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        certAlipayRequest.setServerUrl(rechargeUrl);
        certAlipayRequest.setAppId(appId);
        certAlipayRequest.setPrivateKey(appPrivateKey);
        certAlipayRequest.setFormat("json");
        certAlipayRequest.setCharset("GBK");
        certAlipayRequest.setSignType("RSA2");
        certAlipayRequest.setCertPath(certPath);
        certAlipayRequest.setAlipayPublicCertPath(alipayPublicCertPath);
        certAlipayRequest.setRootCertPath(rootCertPath);
        //证书请求
        AlipayClient alipayClient = new DefaultAlipayClient(certAlipayRequest);
        
        AlipayMarketingVoucherSendRequest request = new AlipayMarketingVoucherSendRequest();
		String date = "puup"+System.currentTimeMillis();
		request.setBizContent("{" +
		"\"template_id\":\"20201111000730016699005FD33O\"," +//券模板ID
		"\"login_id\":\"17798529951\"," +// 支付宝登录ID，手机或邮箱。user_id, login_id, taobao_nick不能同时为空，优先级依次降低。
		//"\"taobao_nick\":\"yue54333\"," +// 淘宝昵称。
		//"\"user_id\":\"15715141438\"," +// 支付宝用户ID。
		"\"out_biz_no\":\""+date+"\"," +// 外部业务单号，保持商家系统中唯一。
		"\"memo\":\"支付宝通用券\"" +
		"}");//设置业务参数
		AlipayMarketingVoucherSendResponse response = alipayClient.certificateExecute(request);
		System.out.println("发送到用户返回值="+JSON.toJSONString(response));
		String result = "";
		if(response.isSuccess()){
			result="调用成功";
		} else {
			result="调用失败";
		}// 根据response中的结果继续业务逻辑处理
		return result;
	}
}
