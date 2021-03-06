package com.bbyiya.model;

import java.io.Serializable;
import java.util.Date;

public class MBigcase implements Serializable{
	private static final long serialVersionUID = 1L;
	
    private Integer caseid;

    private String title;

    private Integer typeid;

    private Date createtime;

    private String imagedefault;

    private Integer startday;

    private Integer endday;

    private String summary;
    private Integer forday;

    public Integer getCaseid() {
        return caseid;
    }

    public void setCaseid(Integer caseid) {
        this.caseid = caseid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public Integer getTypeid() {
        return typeid;
    }

    public void setTypeid(Integer typeid) {
        this.typeid = typeid;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getImagedefault() {
        return imagedefault;
    }

    public void setImagedefault(String imagedefault) {
        this.imagedefault = imagedefault == null ? null : imagedefault.trim();
    }

    public Integer getStartday() {
        return startday;
    }

    public void setStartday(Integer startday) {
        this.startday = startday;
    }

    public Integer getEndday() {
        return endday;
    }

    public void setEndday(Integer endday) {
        this.endday = endday;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary == null ? null : summary.trim();
    }

	public Integer getForday() {
		return forday;
	}

	public void setForday(Integer forday) {
		this.forday = forday;
	}
    
    
}