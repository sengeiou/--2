//package test;
//
//import com.taobao.api.DefaultTaobaoClient;
//import com.taobao.api.TaobaoClient;
//
//public class yuanbaoTest {
//
//	public static void main(String[] args) {
//		TaobaoClient client = new DefaultTaobaoClient(url, appkey, secret);
//		AlibabaTmallEpsOaPointSendRequest req = new AlibabaTmallEpsOaPointSendRequest();
//		TopCorpRequest obj1 = new TopCorpRequest();
//		obj1.setCorpId("ding1d182ae58b9ff8a1ee0f45d8e4f7c288");
//		List<UserPointInfo> list3 = new ArrayList<UserPointInfo>();
//		UserPointInfo obj4 = new UserPointInfo();
//		list3.add(obj4);
//		obj4.setPoint(100L);
//		obj4.setEmplId("2046252024901736");
//		obj4.setName("张三");
//		obj4.setPhoneNumber("186****2893");
//		obj4.setTbAccount("tgl_nba");
//		obj4.setUserType("taobao|dingding");
//		obj1.setUserInfoList(list3);
//		obj1.setIsvCorpId("ding1d182ae58b9ff8a1ee0f45d8e4f7c288");
//		obj1.setTaskName("新春福利");
//		obj1.setBlessing("新年快乐");
//		obj1.setUniqueId("M37XYENH9K349NX9Q04AJDVB93PTCT8C");
//		req.setParm0(obj1);
//		AlibabaTmallEpsOaPointSendResponse rsp = client.execute(req);
//		System.out.println(rsp.getBody());
//	}
//}
