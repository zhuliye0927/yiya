package com.bbyiya.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbyiya.dao.EErrorsMapper;
import com.bbyiya.dao.OOrderaddressMapper;
import com.bbyiya.dao.OOrderproductdetailsMapper;
import com.bbyiya.dao.OOrderproductsMapper;
import com.bbyiya.dao.OPayorderMapper;
import com.bbyiya.dao.OPayorderextMapper;
import com.bbyiya.dao.OUserordersMapper;
import com.bbyiya.dao.PProductstyleexpMapper;
import com.bbyiya.dao.PProductstylesMapper;
import com.bbyiya.dao.UAccountsMapper;
import com.bbyiya.dao.UBranchtransaccountsMapper;
import com.bbyiya.dao.UBranchtransamountlogMapper;
import com.bbyiya.dao.UCashlogsMapper;
import com.bbyiya.dao.UUseraddressMapper;
import com.bbyiya.dao.UUsersMapper;
import com.bbyiya.enums.AmountType;
import com.bbyiya.enums.OrderStatusEnum;
import com.bbyiya.enums.PayOrderTypeEnum;
import com.bbyiya.enums.PayTypeEnum;
import com.bbyiya.model.EErrors;
import com.bbyiya.model.OOrderproductdetails;
import com.bbyiya.model.OOrderproducts;
import com.bbyiya.model.OPayorder;
import com.bbyiya.model.OPayorderext;
import com.bbyiya.model.OUserorders;
import com.bbyiya.model.PProductstyleexp;
import com.bbyiya.model.PProductstyles;
import com.bbyiya.model.UAccounts;
import com.bbyiya.model.UBranchtransaccounts;
import com.bbyiya.model.UBranchtransamountlog;
import com.bbyiya.model.UCashlogs;
import com.bbyiya.model.UUsers;
import com.bbyiya.service.IBasePayService;
import com.bbyiya.utils.ObjectUtil;

@Service("basePayServiceImpl")
@Transactional(rollbackFor = { RuntimeException.class, Exception.class })
public class BasePayServiceImpl implements IBasePayService{
	
	// --------------------订单模块注解--------------------------------------
	@Autowired
	private OOrderaddressMapper orderaddressMapper;// 订单收货地址
	@Autowired
	private OOrderproductsMapper oproductMapper;// 订单产品
	@Autowired
	private OUserordersMapper userOrdersMapper;// 订单
	@Autowired
	private OPayorderMapper payOrderMapper;// 支付单
	@Autowired
	private OOrderproductdetailsMapper odetailMapper;// 产品图片集合
	
	@Autowired
	private OPayorderextMapper oextMapper;
	

	/*------------------------用户信息模块----------------------------------*/
	@Autowired
	private UAccountsMapper accountsMapper;//账户信息
	@Autowired
	private UCashlogsMapper cashlogsMapper;//账户流水
	@Autowired
	private UUseraddressMapper addressMapper;// 用户收货地址
	@Autowired
	private UUsersMapper usersMapper;
	
	/*-----------------------产品模块--------------------------------------------*/
	@Autowired
	private PProductstyleexpMapper styltExpMapper;
	@Autowired
	private PProductstylesMapper styleMapper;
	
	/*-- ---------------------错误日志记录-------------------------------------------*/
	@Autowired
	private EErrorsMapper logMapper;
	
	/*-----------------------------------------*/
	@Autowired
	private UBranchtransaccountsMapper transMapper;
	@Autowired
	private UBranchtransamountlogMapper transLogMapper;
	
	
	/**
	 * 订单支付成功 回写
	 */
	public boolean paySuccessProcess(String payId) {
		if (!ObjectUtil.isEmpty(payId)) {
			try {
				OPayorder payOrder = payOrderMapper.selectByPrimaryKey(payId);
				if (payOrder != null && payOrder.getStatus()!=null && payOrder.getStatus().intValue()==Integer.parseInt(OrderStatusEnum.noPay.toString())) {
					int orderType=payOrder.getOrdertype()==null?0:payOrder.getOrdertype();
					/*-------------------------代理商货款充值-----------------------------------------------------*/
					if(orderType==Integer.parseInt(PayOrderTypeEnum.chongzhi.toString())){
						UCashlogs log=new UCashlogs();
						log.setAmount(payOrder.getTotalprice());
						log.setUserid(payOrder.getUserid());
						log.setPayid(payId);
						log.setUsetype(Integer.parseInt(AmountType.get.toString()));//充值
						log.setCreatetime(new Date());
						cashlogsMapper.insert(log);
						UCashlogs freeLog=new UCashlogs();
						freeLog.setAmount(payOrder.getTotalprice()*2);
						freeLog.setUserid(payOrder.getUserid());
						freeLog.setPayid(payId);
						freeLog.setUsetype(Integer.parseInt(AmountType.free.toString()));//充值
						freeLog.setCreatetime(new Date());
						cashlogsMapper.insert(freeLog);
						//充值 金额 = 实际金额*3 
						Double totalPriceTemp=payOrder.getTotalprice()*3;
						UAccounts accounts=accountsMapper.selectByPrimaryKey(payOrder.getUserid());
						if(accounts!=null){
							accounts.setAvailableamount(accounts.getAvailableamount()+totalPriceTemp);
							accountsMapper.updateByPrimaryKeySelective(accounts);
						}else {
							accounts=new UAccounts();
							accounts.setUserid(payOrder.getUserid());
							accounts.setAvailableamount(totalPriceTemp);
							accountsMapper.insert(accounts);
						}
					}/*-----------------------------货款充值（完）-------------------------------------*/
					/*-----------------------------代理商邮费 充值------------------------------------------*/
					else if (orderType==Integer.parseInt(PayOrderTypeEnum.postage.toString())) {
						//供应商邮费充值
						UBranchtransamountlog translog=new UBranchtransamountlog();
						translog.setBranchuserid(payOrder.getUserid());
						translog.setPayid(payId);
						translog.setAmount(payOrder.getTotalprice());
						translog.setType(Integer.parseInt(AmountType.get.toString()));
						translog.setCreatetime(new Date());
						transLogMapper.insert(translog);
						
						//邮费账户金额更新
						UBranchtransaccounts transAccount= transMapper.selectByPrimaryKey(payOrder.getUserid());
						if(transAccount!=null){  
							double amount=transAccount.getAvailableamount()==null?0d:transAccount.getAvailableamount();
							transAccount.setAvailableamount(amount+payOrder.getTotalprice());
							transMapper.updateByPrimaryKey(transAccount);
						}else {
							transAccount=new UBranchtransaccounts();
							transAccount.setBranchuserid(payOrder.getUserid());
							transAccount.setAvailableamount(payOrder.getTotalprice());
							transMapper.insert(transAccount);
						}
					}/*-------------------------------------------------------------------*/
					
					/************************------------普通购物------------------********************************/
					else {//购物
						OUserorders userorders = userOrdersMapper.selectByPrimaryKey(payOrder.getUserorderid());
						if (userorders != null) {
							if (userorders.getStatus().intValue() == Integer.parseInt(OrderStatusEnum.noPay.toString())) {
								//订单作品详细
								List<OOrderproductdetails> detailsList= odetailMapper.findOrderProductDetailsByProductOrderId(userorders.getUserorderid());
								
								//更新订单状态
								if(detailsList!=null&&detailsList.size()>0){
									userorders.setStatus(Integer.parseInt(OrderStatusEnum.waitFoSend.toString()));
									userorders.setUploadtime(new Date()); 
								}else {
									userorders.setStatus(Integer.parseInt(OrderStatusEnum.payed.toString()));
								}
								userorders.setPaytype(Integer.parseInt(PayTypeEnum.weiXin.toString()));
								userorders.setPaytime(new Date()); 
								userOrdersMapper.updateByPrimaryKeySelective(userorders);
								//分提成
								addOrderExtend(payOrder);
							}
						}
					}
					//更新支付订单状态-----------------------
					payOrder.setPaytime(new Date());
					payOrder.setStatus(Integer.parseInt(OrderStatusEnum.payed.toString()));
					payOrder.setPaytype(Integer.parseInt(PayTypeEnum.weiXin.toString())); 
					payOrderMapper.updateByPrimaryKeySelective(payOrder);
					return true;
				}else {
					addlog("payId:"+payId+",方法paySuccessProcess。不在可支付的状态！");
				}
			} catch (Exception e) {
				addlog("payId:"+payId+",方法paySuccessProcess。"+e.getMessage());
			}
		}
		return false;
	}

	/**
	 * 上级订单
	 * @param payorder
	 */
	public void addOrderExtend(OPayorder payorder){
		try {
			UUsers user= usersMapper.selectByPrimaryKey(payorder.getUserid());
			if(user!=null&&user.getUpuserid()!=null&&user.getUpuserid()>0){
				OPayorderext ext=new OPayorderext();
				ext.setPayid(payorder.getPayid());
				ext.setUserorderid(payorder.getUserorderid());
				ext.setUserid(payorder.getUserid());
				ext.setUpuserid(user.getUpuserid());
				ext.setTotalprice(payorder.getTotalprice());
				ext.setCreatetime(new Date());
				oextMapper.insert(ext);
			}
		} catch (Exception e) {
			addlog("userOrderId:"+payorder.getUserorderid()+",方法addOrderExtend。"+e.getMessage());
		}
	}
	
	
	/**
	 * 订单完成后新增销量
	 */
	public void addProductExt(String userOrderId){
		try {
			OOrderproducts oproduct= oproductMapper.getOProductsByOrderId(userOrderId);
			if(oproduct!=null&&oproduct.getSalesuserid()!=null){
				PProductstyleexp styleExp= styltExpMapper.selectByPrimaryKey(oproduct.getSalesuserid());
				if(styleExp!=null){
					int count=styleExp.getSalecount()==null?0:styleExp.getSalecount();
					styleExp.setSalecount(count+oproduct.getCount()); 
				}else {
					PProductstyles style=styleMapper.selectByPrimaryKey(oproduct.getStyleid());
					if(style!=null){
						styleExp=new PProductstyleexp();
						styleExp.setStyleid(oproduct.getStyleid());
						styleExp.setProductid(style.getProductid());
						styleExp.setSalecount(oproduct.getCount());
						styltExpMapper.insert(styleExp);
					}
				}
			}
		} catch (Exception e) {
			addlog("userOrderId："+userOrderId+",方法addProductExt。"+e); 
		}
	}
	
	/**
	 * 插入错误Log
	 * 
	 * @param msg
	 */
	public void addlog(String msg) {
		try {
			EErrors errors = new EErrors();
			errors.setClassname(this.getClass().getName());
			errors.setMsg(msg);
			errors.setCreatetime(new Date()); 
			logMapper.insert(errors);
		} catch (Exception e) {
			
		}
	}

}
