package com.bbyiya.model;

import java.util.Date;

public class UWeiusers {
    private Long userid;

    private String mobilephone;

    private String name;

    private Integer status;

    private String certificateimg;

    private Date createtime;

    private String remark;

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getMobilephone() {
        return mobilephone;
    }

    public void setMobilephone(String mobilephone) {
        this.mobilephone = mobilephone == null ? null : mobilephone.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCertificateimg() {
        return certificateimg;
    }

    public void setCertificateimg(String certificateimg) {
        this.certificateimg = certificateimg == null ? null : certificateimg.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }
}