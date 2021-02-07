package com.recharge.domain;

public class UserPointInfo {
	private Long point	;//Number	true	100	发放元宝数
	private String empl_id;	///String	false	2046252024901736	员工id
	private String name	;//String	false	张三	员工姓名
	private String phone_number;//	String	false	186****2893	员工电话号码
	private String tb_account;//	String	false	tgl_nba	员工淘宝账号
	private String user_type;//
	public Long getPoint() {
		return point;
	}
	public void setPoint(Long point) {
		this.point = point;
	}
	public String getEmpl_id() {
		return empl_id;
	}
	public void setEmpl_id(String empl_id) {
		this.empl_id = empl_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone_number() {
		return phone_number;
	}
	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}
	public String getTb_account() {
		return tb_account;
	}
	public void setTb_account(String tb_account) {
		this.tb_account = tb_account;
	}
	public String getUser_type() {
		return user_type;
	}
	public void setUser_type(String user_type) {
		this.user_type = user_type;
	}

	
}
