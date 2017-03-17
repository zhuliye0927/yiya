package com.bbyiya.pic.vo.order.ibs;

import java.io.Serializable;

import com.bbyiya.model.OOrderaddress;

public class OrderVo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	//������
	private String userorderid;
	//�µ���
	private Long userid;

	private Integer status;

	private Long branchuserid;
	//
	private String paytime;
	
	private OOrderaddress address;

	private OrderProductVo orderProduct;
	
	public String getUserorderid() {
		return userorderid;
	}

	public void setUserorderid(String userorderid) {
		this.userorderid = userorderid;
	}

	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getBranchuserid() {
		return branchuserid;
	}

	public void setBranchuserid(Long branchuserid) {
		this.branchuserid = branchuserid;
	}

	

	public String getPaytime() {
		return paytime;
	}

	public void setPaytime(String paytime) {
		this.paytime = paytime;
	}

	public OOrderaddress getAddress() {
		return address;
	}

	public void setAddress(OOrderaddress address) {
		this.address = address;
	}

	public OrderProductVo getOrderProduct() {
		return orderProduct;
	}

	public void setOrderProduct(OrderProductVo orderProduct) {
		this.orderProduct = orderProduct;
	}


	
	
	
}
