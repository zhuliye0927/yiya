package com.bbyiya.model;

import java.util.Date;

public class PMyproductchildinfo {
	
    private Long cartid;

    private Long userid;

    private Date birthday;

    private String nickname;

    private String relation;

    private Date createtime;
    
    private Integer isdue;
    
    private String birthdayStr;
    
    public Integer getIsdue() {
		return isdue;
	}

	public void setIsdue(Integer isdue) {
		this.isdue = isdue;
	}

	public Long getCartid() {
        return cartid;
    }

    public void setCartid(Long cartid) {
        this.cartid = cartid;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation == null ? null : relation.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

	public String getBirthdayStr() {
		return birthdayStr;
	}

	public void setBirthdayStr(String birthdayStr) {
		this.birthdayStr = birthdayStr;
	}
    
}