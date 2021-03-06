package com.bbyiya.cts.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbyiya.cts.job.HeartbeatJob;
import com.bbyiya.cts.service.ITempAutoOrderSumbitService;
import com.bbyiya.dao.PMyproductsMapper;
import com.bbyiya.dao.PMyproducttempMapper;
import com.bbyiya.dao.PMyproducttempapplyMapper;
import com.bbyiya.dao.SysLogsMapper;
import com.bbyiya.dao.UAdminactionlogsMapper;
import com.bbyiya.enums.MyProductTempStatusEnum;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.model.OOrderproducts;
import com.bbyiya.model.PMyproducts;
import com.bbyiya.model.PMyproducttemp;
import com.bbyiya.model.PMyproducttempapply;
import com.bbyiya.model.SysLogs;
import com.bbyiya.service.pic.IBaseOrderMgtService;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.address.OrderaddressParam;
import com.bbyiya.vo.order.SubmitOrderProductParam;
import com.bbyiya.vo.order.UserOrderSubmitParam;

@Service("tempAutoOrderSumbitService")
@Transactional(rollbackFor = { RuntimeException.class, Exception.class })
public class TempAutoOrderSumbitServiceImpl implements ITempAutoOrderSumbitService{
	
	private Logger Log = Logger.getLogger(TempAutoOrderSumbitServiceImpl.class);
	
	@Autowired
	private PMyproductsMapper myproductMapper;
	
	@Autowired
	private PMyproducttempMapper tempMapper;
	
	@Autowired
	private PMyproducttempapplyMapper applyMapper;
	
	@Autowired
	private SysLogsMapper syslogMapper;

	
	@Resource(name = "baseOrderMgtServiceImpl")
	private IBaseOrderMgtService orderMgtService;
	
	public ReturnModel dotempAutoOrderSumbit(){
		ReturnModel rq=new ReturnModel();
		rq.setStatu(ReturnStatus.Success);
		//得到所有已开启的活动列表
		List<PMyproducttemp> templist=tempMapper.findAllAutoOrderTempByStatus(Integer.parseInt(MyProductTempStatusEnum.enable.toString()));
		if(templist!=null&&templist.size()>0){
			for (PMyproducttemp temp : templist) {
				//如果orderhours为空则默认为48小时
				if(temp.getOrderhours()==null) temp.setOrderhours(48);
				//得到活动下所有可下单的作品列表
				List<PMyproducts> productlist=myproductMapper.findCanOrderMyProducts(temp.getTempid(), temp.getOrderhours());
				for (PMyproducts myproduct : productlist) {
					//调用下单接口
					SubmitOrderProductParam productParam=new SubmitOrderProductParam();
					productParam.setProductId(myproduct.getProductid());
					Long styleId=temp.getStyleid();
					//如果为空，默认为竖板
					if(ObjectUtil.isEmpty(styleId)) styleId=myproduct.getProductid();
					productParam.setStyleId(styleId);
					productParam.setCount(1);
					productParam.setCartId(myproduct.getCartid());
					
					PMyproducttempapply tempapply=applyMapper.getMyProducttempApplyByCartId(myproduct.getCartid());
					OrderaddressParam addressParam=new OrderaddressParam();
					addressParam.setUserid(tempapply.getUserid());
					addressParam.setCity(tempapply.getCity());
					addressParam.setDistrict(tempapply.getArea());
					addressParam.setPhone(tempapply.getMobilephone());
					addressParam.setProvince(tempapply.getProvince());
					addressParam.setReciver(tempapply.getReceiver());
					addressParam.setStreetdetail(tempapply.getStreet());
					if (productParam != null&&addressParam!=null) {
						OOrderproducts product = new OOrderproducts();
						product.setProductid(productParam.getProductId());
						product.setStyleid(productParam.getStyleId());
						product.setCount(productParam.getCount());
						
						// 下单参数
						UserOrderSubmitParam param = new UserOrderSubmitParam();
						
						param.setUserId(myproduct.getUserid());
						param.setRemark("系统自动下单");
						
						if (productParam.getCartId() != null && productParam.getCartId() > 0) {
							param.setCartId(productParam.getCartId());
						}
						//为影楼订单
						param.setOrderType(1);
						if(productParam.getPostModelId()!=null){
							param.setPostModelId(productParam.getPostModelId()); 
						} 
						param.setOrderproducts(product);
						if(addressParam.getCity()==null){
							rq.setStatu(ReturnStatus.ParamError);
							rq.setStatusreson("地址参数有误：city为空");
							return rq;
						}
						if(addressParam.getProvince()==null){
							rq.setStatu(ReturnStatus.ParamError);
							rq.setStatusreson("地址参数有误：province为空");
							return rq;
						}
						if(addressParam.getDistrict()==null){
							rq.setStatu(ReturnStatus.ParamError);
							rq.setStatusreson("地址参数有误：district为空");
							return rq;
						}
						if(addressParam.getStreetdetail()==null){
							rq.setStatu(ReturnStatus.ParamError);
							rq.setStatusreson("地址参数有误：streetdetail为空");
							return rq;
						}
						if(addressParam.getPhone()==null){
							rq.setStatu(ReturnStatus.ParamError);
							rq.setStatusreson("参数有误,手机号为空");
							return rq;
						}
						if(!ObjectUtil.isEmpty(addressParam.getPhone())&&!ObjectUtil.isMobile(addressParam.getPhone())){
							rq.setStatu(ReturnStatus.ParamError_2);
							rq.setStatusreson("手机号格式不对！");
							return rq;
						}
						if(addressParam.getReciver()==null){
							rq.setStatu(ReturnStatus.ParamError);
							rq.setStatusreson("参数有误,联系人为空");
							return rq;
						}
						param.setAddressparam(addressParam);
						rq = orderMgtService.submitOrder_IBS(param);
					} else {
						rq.setStatu(ReturnStatus.ParamError);
						rq.setStatusreson("参数有误");
					}
					if(!rq.getStatu().equals(ReturnStatus.Success))//未通过参数验证
					{
						addSysLog("作品"+productParam.getCartId()+"自动下单失败！原因："+rq.getStatusreson(),"dotempAutoOrderSumbit","下单失败");
						Log.error("作品"+productParam.getCartId()+"自动下单失败！原因："+rq.getStatusreson());
					}else{
						Log.info("作品"+productParam.getCartId()+"自动下单成功！");
						addSysLog("作品"+productParam.getCartId()+"自动下单成功！","dotempAutoOrderSumbit","下单成功");
					}
					
				}//end For 1
				
			}//end For 2
			
		}
		
		return rq;
	}
	
	public void addSysLog(String msg,String jobid,String jobname){
		SysLogs log=new SysLogs();
		log.setContent(msg);
		log.setJobid(jobid);
		log.setJobname(jobname);
		log.setCreatetime(new Date());
		syslogMapper.insert(log);
	}
}
