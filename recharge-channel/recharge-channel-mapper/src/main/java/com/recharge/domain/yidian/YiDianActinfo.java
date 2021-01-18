package com.recharge.domain.yidian;

import java.util.List;

/**
 * @author Administrator
 * @create 2021/1/11 17:39
 */
public class YiDianActinfo {
    private String actid;
    private List<YiDianIssueinfo> issueinfo;

    public List<YiDianIssueinfo> getIssueinfo() {
        return issueinfo;
    }

    public void setIssueinfo(List<YiDianIssueinfo> issueinfo) {
        this.issueinfo = issueinfo;
    }

    public String getActid() {
        return actid;
    }

    public void setActid(String actid) {
        this.actid = actid;
    }



}
