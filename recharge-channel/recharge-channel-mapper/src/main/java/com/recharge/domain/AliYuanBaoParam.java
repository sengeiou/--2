package com.recharge.domain;

import java.util.List;

public class AliYuanBaoParam {
	
	private String corp_id; //企业id
	private String isv_corp_id;//isv企业id
	private String task_name;//任务名称
	private String blessing;//祝福语
	private String unique_id;//幂等id
	private String task_id; //发放凭证
	private List<UserPointInfo> user_info_list;//用户信息
	
	public String getCorp_id() {
		return corp_id;
	}
	public void setCorp_id(String corp_id) {
		this.corp_id = corp_id;
	}
	public String getIsv_corp_id() {
		return isv_corp_id;
	}
	public void setIsv_corp_id(String isv_corp_id) {
		this.isv_corp_id = isv_corp_id;
	}
	public String getTask_name() {
		return task_name;
	}
	public void setTask_name(String task_name) {
		this.task_name = task_name;
	}
	public String getBlessing() {
		return blessing;
	}
	public void setBlessing(String blessing) {
		this.blessing = blessing;
	}
	public String getUnique_id() {
		return unique_id;
	}
	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}
	public List<UserPointInfo> getUser_info_list() {
		return user_info_list;
	}
	public void setUser_info_list(List<UserPointInfo> user_info_list) {
		this.user_info_list = user_info_list;
	}
	public String getTask_id() {
		return task_id;
	}
	public void setTask_id(String task_id) {
		this.task_id = task_id;
	}
	
	
}
