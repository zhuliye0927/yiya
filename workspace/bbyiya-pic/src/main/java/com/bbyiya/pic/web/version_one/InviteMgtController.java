package com.bbyiya.pic.web.version_one;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.dao.PMyproductsMapper;
import com.bbyiya.dao.PMyproducttempapplyMapper;
import com.bbyiya.dao.UUseraddressMapper;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.enums.pic.InviteStatus;
import com.bbyiya.model.PMyproducts;
import com.bbyiya.model.PMyproducttempapply;
import com.bbyiya.model.UUseraddress;
import com.bbyiya.pic.service.IPic_myProductService;
import com.bbyiya.service.IRegionService;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.web.base.SSOController;

@Controller
@RequestMapping(value = "/myProduct/invite")
public class InviteMgtController  extends SSOController {
	@Resource(name = "pic_myProductService")
	private IPic_myProductService myProductService;
	
	@Autowired
	private UUseraddressMapper addressMapper;
	@Autowired
	private PMyproducttempapplyMapper tempApplyMapper;
	@Autowired
	private PMyproductsMapper myproductsMapper;
	
	@Resource(name = "regionServiceImpl")
	private IRegionService regionService;
	/**
	 * 发送 协同编辑 邀请
	 * @param province
	 * @param city
	 * @param district
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/sendInvite")
	public String sendInvite(String phone,Long cartId) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			if(phone.equals(user.getMobilePhone())){
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("不能邀请自己协同编辑！");
				return JsonUtil.objectToJsonStr(rq);
			}
			rq=myProductService.sendInvite(user.getUserId(), phone, cartId);
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期，请重新登录");
		}
		return JsonUtil.objectToJsonStr(rq);
	}

	
	/**
	 * 处理我的邀请(已完成活动)
	 * @param phone
	 * @param cartId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/processInvite")
	public String processInvite(Long cartId,Integer status,String addressId) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			
			if(status!=null&&status==Integer.parseInt(InviteStatus.ok.toString())){
				//更新用户活动收获地址信息
				long userAddressId=ObjectUtil.parseLong(addressId);
				if(userAddressId>0){
					UUseraddress address=addressMapper.get_UUserAddressByKeyId(userAddressId);
					if(address==null){
						rq.setStatusreson("地址信息不存在！");
						return JsonUtil.objectToJsonStr(rq);
					}
					PMyproducttempapply apply= tempApplyMapper.getMyProducttempApplyByCartId(cartId);
					if(apply==null){
						PMyproducts myproducts= myproductsMapper.selectByPrimaryKey(cartId);
						if(myproducts!=null&&myproducts.getTempid()!=null){
							apply=tempApplyMapper.getMyProducttempApplyByUserId(myproducts.getTempid(), user.getUserId());
						}
					}
					if(apply!=null){
						apply.setReceiver(address.getReciver()); 
						apply.setMobilephone(address.getPhone());
						apply.setProvince(address.getProvince());
						apply.setCity(address.getCity());
						apply.setStreet(address.getStreetdetail());
						apply.setArea(address.getArea());
						apply.setAdress(regionService.getProvinceName(address.getProvince())+regionService.getCityName(address.getCity())+regionService.getAresName(address.getArea())+address.getStreetdetail());
						tempApplyMapper.updateByPrimaryKeySelective(apply);
					}
				}//收获地址信息（完）------------------
				
				rq=myProductService.processInvite(cartId,user.getUserId(), status);
			}else {
				//旧版的状态更新
				rq=myProductService.processInvite(user.getMobilePhone(),cartId, status);
			}
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期，请重新登录");
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
	/**
	 * 处理扫码页面的接受邀请
	 * @param phone 被邀请人手机号
	 * @param cartId 作品cartid
	 * @param vcode  验证码
	 * @param needVerfiCode  是否需要验证手机验证码
	 * @param version  二维码版本号 可为空
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/acceptScanQrCodeInvite")
	public String acceptScanQrCodeInvite(String phone,Long cartId,String vcode,Integer needVerfiCode,String version) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			PMyproducts myproduct=myProductService.getPMyproducts(cartId);
			if(myproduct==null){
				rq.setStatu(ReturnStatus.SystemError);
				rq.setStatusreson("不存在的作品");
				return JsonUtil.objectToJsonStr(rq);
			}
			//如果是模板作品
			if(myproduct.getIstemp()!=null&&myproduct.getIstemp().toString().equals("1")){
				rq=myProductService.acceptTempScanQrCodeInvite(user.getUserId(), phone, cartId,vcode,needVerfiCode);
			}else{
				rq=myProductService.acceptScanQrCodeInvite(user.getUserId(),phone,cartId,vcode,needVerfiCode,version);	
			}
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期，请重新登录");
		}
		return JsonUtil.objectToJsonStr(rq);
	}

	/**
	 * 获取用户提示信息
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/myUserInfoExp")
	public String myUserInfoExp() throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user= super.getLoginUser();
		if(user!=null){
			rq=myProductService.myUserInfoExp(user.getUserId(), user.getMobilePhone()); 
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("登录过期，请重新登录");
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
}
