package test;

import org.apache.commons.lang3.StringUtils;
import com.alipay.api.AlipayApiException;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;

public class ZhiFuBaoTest {

	public static void main(String[] args) {
//		ZhifubaoRechargeInfoBean zhifubaoRechargeInfoBean = (ZhifubaoRechargeInfoBean) rechargeOrderBean.getRechargeInfoObj(ZhifubaoRechargeInfoBean.class);
//        JSONObject configJSONObject = JSON.parseObject(channel.getConfigInfo());
		 //张建账户
//        String rechargeUrl = "https://openapi.alipay.com/gateway.do";//configJSONObject.getString("rechargeUrl");
//        String appId = "2021002103618531";//configJSONObject.getString("appId");
//        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCNkhLEY3YHebvmsz/HL1oiixldm+vzxVdlIk5JR4kKmrnzRQtDTAjsTGW+n4IKuSZ28tD4FBDQEth20Kjsui5ZKO5TDPcCnAcSJ+z281UMaBr3PBfPTsr0//rfuBnEET1Bcl/3dNOizZxARK3Xykj89CvBSoL6hDO+CXThoXXUP8Ui++a5biwiYrz8mz2aUOildzCKKosGPRnMvsvUh3rv9WMSDSy1pJw0dHOcKgHhB2hB+Sou3+Mc8d5LI6dIlxWWux47bQssrPR9u7n0yZIGFMn5hsD6Ic1Mx/4D3VqHMeaxsTdmzeqjfbMdrs1ZSljAJbGfsRvDB26mFhXUlhDtAgMBAAECggEAeBpXn5K9vEPTXDnvvpz2e93ELD0uZMWjiiIbhmGQ6pUvcim+D16/JzD9FEYgPdarwjfnfr8PMGb8i3gkB4k/7tYhsgWKvQT/nrCOgUcYfQkI73xkurQezNEp1YhLG6KAdDiDH91V+oUyrPdXAH4Vg7vmLvp2ue9UMkG1jgG7g881KUhCiabis4whEyVW4CD8G3+SaHWpi2HkjMEu1JYcVlBTwZH9ZSB9b9S2IQ0gkMA/Dkm9vECYGt0CpJJMOotF3E7jPK5eJ61d3juAX5Vkc8Bn8pmhaETYKRtrgcFl7foFK5WHwiSEYTZbX8M3BVrUCvKStOrVSQYLqMJTz6iLQQKBgQDzedJSCwrvPj4SrSmGb2+sTu/TuQtibCoGDgA5bJxk5GzMJgoQTToOTF6A6SjDR5pZNgI6NltdapzrWVNXpNbHGZ/oll/mSljWNZwr8gsAj4uWzs/6BUiBSGvI6kCG+BeeOdprJSol/exeqGxhFGWI9fiVI22/31RXMGmuli6k3QKBgQCU2lOF6SNJJWbzstZj/dE06dpD5KOyO1PpeBiC9GKOi3pASThdmK9G6O166+Z2Zo2I1KTH7lJvF2ztJ3gT8cCEO7FXun3CXRH8xdOGK0X+HfKsmm61hk9RzOXEAFpyO+ynHjO1XpO84o3eix5SIcWqarg2NmhEUfgEL8F2EL2TUQKBgA5s+lwoEVbJfk4G+n3fnmM8LhOCVD1ZoUfIjpTYbY/AxP13h4MdwEHWnobOyb6AQJHmlE00qUcoqPF80mZP+vXC++mA91+iqg7PEFi+N46p8qBV0pbCt2AJlXFLB9W3X1bdoKZWhl/H4rhdDEbYOYfa4wabGcaTieC8mpZgiZT1AoGAdQCwCmBZJhPKOuBl+K74FPmEYT2FFoHf0XJfAgCKv7UO4R0suxVbIQfSM37d5k9yDQ3ogGbqUPw+2KLflf2+77HkEis60t5JV0FApQO6vqZXrCivwOvTyV6fdFM6xPQpbqskxfmTczM5hKYhrTlyoCPGrIZTqvArXU8UaHN8MCECgYEA6Q7uYOqhWuvBDeA6oaOLVj4Dd6NFqbsGK2+BcyDXAdQU2T6GbXmE5YSTSSsacyiJv/J8I7LFF7FmEvmRTcC4nV5g3ZXyrsMs7nosiqeAeSpZKBfpl8YBjz7a3qZIcyDuH6NmO9zGj9Q+wrXsW0HtCvi28KxsRnxHk1TGZnK3Ifk=" ;//configJSONObject.getString("appPrivateKey");
//        String certPath = "D:/channel-zhifubao-file/appCertPublicKey_2021002103618531.crt";//configJSONObject.getString("certPath");
//        String alipayPublicCertPath = "D:/channel-zhifubao-file/alipayCertPublicKey_RSA2.crt";//configJSONObject.getString("alipayPublicCertPath");
//        String rootCertPath = "D:/channel-zhifubao-file/alipayRootCert.crt";//configJSONObject.getString("rootCertPath");
//        
        //老支付宝账号
//        String rechargeUrl = "https://openapi.alipay.com/gateway.do";//configJSONObject.getString("rechargeUrl");
//        String appId = "2021001162604085";//configJSONObject.getString("appId");
//        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCej5ZNvG9piMjWlajEtZSHc/1VPz/Gl440etb4GbcgBYSlqvHDYrFhDukeKMyBcCECFbr9v3S6GRDTiOczVr8mJCJBgBjCXJykk0mGp373JG+xUQWUVz3rYaDwVhaVr/hDrB6WeOdDdGZQ3zL6FiWiiD0nNVqpFTYckcmPzd/h07Guc3hRgMSEdpuuwZN/urAt88aYEa04mTLhNyzMaBpqYreeT6W1Fad/tQ9kaa6QdpBQ8hpxgaqq/5KvGOUhIMXF8YCI89VVsuznPEDw1yiNrSgUTP6Z8E5ejMjULRiIbkVTEnKfLtwKEbAJfBmqVo2yAkRL7viWzT9v+nWWeofzAgMBAAECggEAdpSDn5cw6wf0yyYZlSMG9VX1O2+VzntBSlYjtl/k5/m4ff0d2TVR2nhANuHkL1kgFmMR+PrZ3OMqBkj4Pxi801VqmPmnipOgQiQ0dJOHDGUfcshZF9s233Wf2p1HPqLfVBzAs1+v9G50pj6F7Snq82ZX9Z6dMp4+XZdZ2kXpDQUG3MNV9IIppG+LKJa8on7p67sgEI/HdgYXhgTzJ2A9B0ZwTRbJGF7qlFBel90C5FehX9cOLqj3a3Th+RbGaYLZIFWqoiURy7092IAFpKEq3ghXu6I9+gh+J9EmkoX3Mm3GDCcOwfsv9CZaxW5JbOd47UOdxrA1v5rgr/lapJ3aQQKBgQDuUKjQ+GKwNwsFh2N5XEqb3yeiVl4jgz3+/nrXyv2AQlo5wbOLG4xx+i7Gc7CkOsM0sPBFo15knK/ftEuvB9K1pP4/3WQThS9bNJWdxAc1VUr0v6trNlCTj7PMNVoXLh8NbLD8p3REyYz7V6C3mBdfU7p4iag9OazEYve321cbiQKBgQCqU9BynYp7JPHTPmcJsHeD10OWSpo8kAiVlH1QKBz2VpB26CsVRseEWrMgbYnpoWHYkSZsyqyMtpoB7GcMRRb5xxNUGftVqz8C7Y8Z0nT8Oq+ZacOvcmHdclvOKAutt3cvErWkzdQcnU/1AQkKOJnzclWx1viBvgZM549gyhL8mwKBgEp8o4dNW2tAiQ1dV9Yi5p75jOXMoPfwmXwOBk9Ufw+Lra/Hv1qHbcpiLCSTHG74fvMFKtRZT7Oa1c3SCKXYiB+GRoGnrRP82ORk9y6siqQ82ITPM8D65Qx6KfR16Ia+P65ufpM5s7mcVZhpty4rN4LwJKRwQkmAsWNgm5S31e4hAoGBAJBxx8KLa9cG+7WjpAoJhzFdqsmXtrlJhJ4lcSg6/GTEAHt2ZKMEhUrvCB2mB2EEJVBdt/Jm5nHmoTCkZLd5GAT3GKMn7Yc3VJaNE+BYJDGLK0O09+if3fLIeqaYO180yXQ4L6b1jr49goNKP3pViel0usu4fMK6felWLrdsZzGlAoGAJy7O+7dsFNH0W1UxhDETNFRc8elUUuI2nHNicRc63XSV8YcXdcG9qvE3Vg7Waydd2/qkSRaMuAMI82pU78NlDhgQLdnz4+nhnc+SH+cMKbcQ4B3e300eaMwfj4TnznekMWFBV1q28BcM3vS4wQ3vOFfMCoFB/VQcaumHJQwNlyw=" ;//configJSONObject.getString("appPrivateKey");
//        String certPath = "D:/channel-zhifubao-file/271/appCertPublicKey_2021001162604085.crt";//configJSONObject.getString("certPath");
//        String alipayPublicCertPath = "D:/channel-zhifubao-file/271/alipayCertPublicKey_RSA2.crt";//configJSONObject.getString("alipayPublicCertPath");
//        String rootCertPath = "D:/channel-zhifubao-file/271/alipayRootCert.crt";//configJSONObject.getString("rootCertPath");
        
        //2514 支付宝账号
        String rechargeUrl = "https://openapi.alipay.com/gateway.do";//configJSONObject.getString("rechargeUrl");
        String appId = "2021002102652383";//configJSONObject.getString("appId");
        String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCeNp1yg+kOrHLXN6zT98RN5ve5MtgYKxcfs5wgqbx0bUgGU/MTDJOGVHNyZJx+QC+vmis7tRLw81BLVyaa2PUztaYgFzhRpHtMAJckCwAgAoYx5HlrskFLW5yMD1GVIePNXDl/g3YJUT5lEZpWV4UK3U/xNvTi9XHNhGlU+HwTwQdEyXR7ZZUArV8d96AhyyrSNZFQ4TSfpudowxN1tfryT0WX4yo3Ql1ymMq7/r/pPrrsnMasA6JajlXHYzzdcv1j+7zIDeYuqS84Y3rGOYpQi+OSdXQLMovH+8PvX9jHcZB/51WiBom+OFetY/DkCg+Vx7aM+R09wKdJ2dJaCY0dAgMBAAECggEAI9NKI7wq8d+qwYEh6X2ZPeqm/ZC9zfsz6UHKgLruqY51WLl5uT6HyUsmOSj/JrdyFNlcINEH0PfHnsfFPhsSqvre+zVG03XjVXpsvwSeqqDnsjSCKhjbIC1GbZCqcJKT+tR/UJh8CfATqN8ri5GV1rf2ejBvk1TwyBDLqSn7Lp2Rxp//QpkjwV9PK+XrK+ccQxLGNeU3OvLzcCCSsJJzjZrobXsOxegmdZQ7RkdM6eEirh2uWn02aSIXhhBwA4BPQMhOskY8czOM/8XaTASktXmlzzuFfWdkr6EjGIWL1jPLU+77f/dXyLOdquG963Sll7BfvqxX41WY934ywMUVAQKBgQD5FWznk9eREUd5vHjFhrKOLDtb2m3/jTsAQp6Fl1TiepvGZONlpTG73rkNkM+30f6JHP7TljeUjkPubqKBQMXyfrA+BEBnXivH0nXLnzBCDAKNM5RvPceRrWQMjFzgzCkirBF1INwvmZZ/LSfNyrVWvVL0KBqV9DNc0+PuJRiNJQKBgQCim0BLuKBCrywzTG5PMGDshAtv9yZjTQlBw/Om0/CwInC8d4cT6DLrT8p4rLnyrfx+R2ZBm3Z5m9eSZtSAM6X07xnSfVW6i8Negiuc50vJ0XfkXOyHnZVyUsxjLMOv4MrRVYVKphoSYSht2UIyDUe7QTy9Ko956AaF/cWwVabKmQKBgQCEfLP3nXi8zc8R3oDyt8nDu507Jzz4//sREV5WOn9JE+RFq6YUi8CfaEFQCOWjpcvYzJZynuUjXGSfrzM6wuRHLsG5yvMiyEFLQROs4hh+GPtGddgPL3I2d17PGd3RKGb87T8/6OGVZAFb1f3lBVxdLciw26AAgOrNBluncQ3VDQKBgEbN5XRTY21GC7AWst3Gih3S7YwGiW+p5sL9SUY2eVKo3IAkZ+wSKsHuD5L2W03B1vVBr9QXlsIjSv4Tyqo8UyJRTuQp8T92hk7LzaPXz1eE33jPwarXwnByf08b+eXGs2ntnEnCBA5Gwiz68mkLAVYXdRRFyQ3jK5wCwacj1KnZAoGAMi+FhOTFGdddizzVhqqcuuT91chQOLKnA6aywgvSvj+XMSXOPdgLJjnnShEyUrCj0tw13tf4kDTMaJtG6dtd8/lAVj1r6gHlLou9WNPm/FwEB3RDeftJOkyhr3boVhWvW82+gbNn+gjKG5s9QZp/wNmpqxjdMNIUDO3EQBZl/8k=" ;//configJSONObject.getString("appPrivateKey");
        String certPath = "D:/channel-zhifubao-file/2514/appCertPublicKey_2021002102652383.crt";//configJSONObject.getString("certPath");
        String alipayPublicCertPath = "D:/channel-zhifubao-file/2514/alipayCertPublicKey_RSA2.crt";//configJSONObject.getString("alipayPublicCertPath");
        String  rootCertPath ="D:/channel-zhifubao-file/2514/alipayRootCert.crt";
        
        
        /**
         * {
				rechargeUrl: "https://openapi.alipay.com/gateway.do",
				rootCertPath: "/home/recharge/channel-file/cert/alipayRootCert.crt",
				alipayPublicCertPath: "/home/recharge/channel-file/cert/alipayCertPublicKey_RSA2.crt",
				certPath: "/home/recharge/channel-file/cert/appCertPublicKey_2021001162604085.crt",
				appId: "2021001162604085",
				appPrivateKey: "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCej5ZNvG9piMjWlajEtZSHc/1VPz/Gl440etb4GbcgBYSlqvHDYrFhDukeKMyBcCECFbr9v3S6GRDTiOczVr8mJCJBgBjCXJykk0mGp373JG+xUQWUVz3rYaDwVhaVr/hDrB6WeOdDdGZQ3zL6FiWiiD0nNVqpFTYckcmPzd/h07Guc3hRgMSEdpuuwZN/urAt88aYEa04mTLhNyzMaBpqYreeT6W1Fad/tQ9kaa6QdpBQ8hpxgaqq/5KvGOUhIMXF8YCI89VVsuznPEDw1yiNrSgUTP6Z8E5ejMjULRiIbkVTEnKfLtwKEbAJfBmqVo2yAkRL7viWzT9v+nWWeofzAgMBAAECggEAdpSDn5cw6wf0yyYZlSMG9VX1O2+VzntBSlYjtl/k5/m4ff0d2TVR2nhANuHkL1kgFmMR+PrZ3OMqBkj4Pxi801VqmPmnipOgQiQ0dJOHDGUfcshZF9s233Wf2p1HPqLfVBzAs1+v9G50pj6F7Snq82ZX9Z6dMp4+XZdZ2kXpDQUG3MNV9IIppG+LKJa8on7p67sgEI/HdgYXhgTzJ2A9B0ZwTRbJGF7qlFBel90C5FehX9cOLqj3a3Th+RbGaYLZIFWqoiURy7092IAFpKEq3ghXu6I9+gh+J9EmkoX3Mm3GDCcOwfsv9CZaxW5JbOd47UOdxrA1v5rgr/lapJ3aQQKBgQDuUKjQ+GKwNwsFh2N5XEqb3yeiVl4jgz3+/nrXyv2AQlo5wbOLG4xx+i7Gc7CkOsM0sPBFo15knK/ftEuvB9K1pP4/3WQThS9bNJWdxAc1VUr0v6trNlCTj7PMNVoXLh8NbLD8p3REyYz7V6C3mBdfU7p4iag9OazEYve321cbiQKBgQCqU9BynYp7JPHTPmcJsHeD10OWSpo8kAiVlH1QKBz2VpB26CsVRseEWrMgbYnpoWHYkSZsyqyMtpoB7GcMRRb5xxNUGftVqz8C7Y8Z0nT8Oq+ZacOvcmHdclvOKAutt3cvErWkzdQcnU/1AQkKOJnzclWx1viBvgZM549gyhL8mwKBgEp8o4dNW2tAiQ1dV9Yi5p75jOXMoPfwmXwOBk9Ufw+Lra/Hv1qHbcpiLCSTHG74fvMFKtRZT7Oa1c3SCKXYiB+GRoGnrRP82ORk9y6siqQ82ITPM8D65Qx6KfR16Ia+P65ufpM5s7mcVZhpty4rN4LwJKRwQkmAsWNgm5S31e4hAoGBAJBxx8KLa9cG+7WjpAoJhzFdqsmXtrlJhJ4lcSg6/GTEAHt2ZKMEhUrvCB2mB2EEJVBdt/Jm5nHmoTCkZLd5GAT3GKMn7Yc3VJaNE+BYJDGLK0O09+if3fLIeqaYO180yXQ4L6b1jr49goNKP3pViel0usu4fMK6felWLrdsZzGlAoGAJy7O+7dsFNH0W1UxhDETNFRc8elUUuI2nHNicRc63XSV8YcXdcG9qvE3Vg7Waydd2/qkSRaMuAMI82pU78NlDhgQLdnz4+nhnc+SH+cMKbcQ4B3e300eaMwfj4TnznekMWFBV1q28BcM3vS4wQ3vOFfMCoFB/VQcaumHJQwNlyw="
			}
         */
        
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
        try {
            DefaultAlipayClient defaultAlipayClient = new DefaultAlipayClient(certAlipayRequest);

            AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
            request.setBizContent("{" +
                    "\"out_biz_no\":\"" + "TEST251401" + "\"," +
                    "\"trans_amount\":\"" + "1" + "\"," +
                    "\"product_code\":\"TRANS_ACCOUNT_NO_PWD\"," +
                    "\"biz_scene\":\"DIRECT_TRANSFER\"," +
                    "\"payee_type\":\"ALIPAY_LOGONID\"," +
                    "\"payee_info\":{" +
                    "\"identity\":\"" + "15715141438" + "\"," +
                    "\"identity_type\":\"ALIPAY_LOGON_ID\"," +
                    "\"name\":\"" + "刘东" + "\"" +
                    "    }," +
                    "\"remark\":\"" + "" + "\"," +
                    "  }");
            try {
                AlipayFundTransUniTransferResponse response = defaultAlipayClient.certificateExecute(request);
                if (response.isSuccess()) {
                   System.out.println("提交成功");
                } else {
                    if (StringUtils.equals("付款方余额不足", response.getSubMsg())) {
                    	System.out.println("余额不足");
                    } else {
                        System.out.println(response.getSubMsg());
                    }
                }
            } catch (AlipayApiException e) {
                e.printStackTrace();
            	//logger.info("zhifubao invoke error", e);
                //return new ProcessResult(ProcessResult.FAIL, "zhifubao invoke error");
            }
        } catch (AlipayApiException e) {
            //logger.info("zhifubao invoke error", e);
            //return new ProcessResult(ProcessResult.FAIL, "zhifubao invoke error");
        	e.printStackTrace();
        }
	}
}
