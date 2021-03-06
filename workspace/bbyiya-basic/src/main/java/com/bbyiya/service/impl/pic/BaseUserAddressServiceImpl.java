package com.bbyiya.service.impl.pic;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbyiya.baseUtils.ValidateUtils;
import com.bbyiya.dao.UBranchesMapper;
import com.bbyiya.dao.UBranchusersMapper;
import com.bbyiya.dao.UUseraddressMapper;
import com.bbyiya.dao.UUsersMapper;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.enums.user.UserIdentityEnums;
import com.bbyiya.model.UBranches;
import com.bbyiya.model.UBranchusers;
import com.bbyiya.model.UUseraddress;
import com.bbyiya.model.UUsers;
import com.bbyiya.service.IRegionService;
import com.bbyiya.service.pic.IBaseUserAddressService;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.UUserAddressResult;

@Service("baseUserAddressServiceImpl")
@Transactional(rollbackFor = { RuntimeException.class, Exception.class })
public class BaseUserAddressServiceImpl implements IBaseUserAddressService {
	@Autowired
	private UUseraddressMapper addressMapper;

	@Resource(name = "regionServiceImpl")
	private IRegionService regionService;
	
	@Autowired
	private UUsersMapper usersMapper;
	@Autowired
	private UBranchesMapper branchMapper;
	@Autowired
	private UBranchusersMapper branchusersMapper;

	/**
	 * edit/add user's addressInfo zy
	 * 
	 * @return
	 */
	public ReturnModel addOrEdit_UserAddress(UUseraddress address) {
		ReturnModel rq = new ReturnModel();
		if (address != null && address.getUserid() != null && address.getUserid() > 0) {
			if (!ObjectUtil.validSqlStr(address.getReciver())) {
				rq.setStatu(ReturnStatus.ParamError_2);
				rq.setStatusreson("收货人信息存在危险！");
				return rq;
			}
			if (!ObjectUtil.isEmpty(address.getPhone()) && !ObjectUtil.validSqlStr(address.getPhone())) {
				rq.setStatu(ReturnStatus.ParamError_2);
				rq.setStatusreson("手机号格式不对！");
				return rq;
			}
			if (!ObjectUtil.isEmpty(address.getStreetdetail()) && !ObjectUtil.validSqlStr(address.getStreetdetail())) {
				rq.setStatu(ReturnStatus.ParamError_2);
				rq.setStatusreson("街道详情存在危险");
				return rq;
			}
			if (!ObjectUtil.isEmpty(address.getPostcode()) && !ObjectUtil.validSqlStr(address.getPostcode())) {
				rq.setStatu(ReturnStatus.ParamError_2);
				rq.setStatusreson("邮政编码存在危险");
				return rq;
			}
			if (address.getAddrid() != null && address.getAddrid() > 0) {
				UUserAddressResult temp = addressMapper.get_UUserAddressResultByKeyId(address.getAddrid());
				if (temp != null) {
					if (temp.getUserid().longValue() != address.getUserid().longValue()) {
						rq.setStatu(ReturnStatus.ParamError_2);
						rq.setStatusreson("您没有权限修改别人的收货地址！");
						return rq;
					}
					addressMapper.updateByPrimaryKeySelective(address);
					if (!ObjectUtil.isEmpty(address.getReciver())) {
						temp.setReciver(address.getReciver());
					}
					if (!ObjectUtil.isEmpty(address.getPhone())) {
						temp.setPhone(address.getPhone());
					}
					if (!ObjectUtil.isEmpty(address.getProvince())) {
						temp.setProvince(address.getProvince());
					}
					if (!ObjectUtil.isEmpty(address.getCity())) {
						temp.setCity(address.getCity());
					}
					if (!ObjectUtil.isEmpty(address.getArea())) {
						temp.setArea(address.getArea());
					}
					if (!ObjectUtil.isEmpty(address.getStreetdetail())) {
						temp.setStreetdetail(address.getStreetdetail());
					}
					temp.setProvinceName(regionService.getProvinceName(temp.getProvince()));
					temp.setCityName(regionService.getCityName(temp.getCity()));
					temp.setAreaName(regionService.getAresName(temp.getArea()));
					rq.setBasemodle(temp);
					rq.setStatu(ReturnStatus.Success);
					return rq;
				} else {
					rq.setStatu(ReturnStatus.SystemError);
					rq.setStatusreson("不存在的用户收货地址！");
					return rq;
				}
			} else {
				List<UUserAddressResult> list = addressMapper.find_UUserAddressByUserId(address.getUserid());
				if (list == null || list.size() <= 0) {
					address.setIsdefault(1);
				}
				addressMapper.insertReturnId(address);
				rq.setStatu(ReturnStatus.Success);
				rq.setBasemodle(getUserAddressResult(address.getUserid(), address.getAddrid()));
			}
		} else {
			rq.setStatu(ReturnStatus.ParamError_1);
			rq.setStatusreson("参数有误");
		}
		return rq;
	}

	public ReturnModel addOrEdit_UserAddressReturnAddressId(UUseraddress address) {
		ReturnModel rq = new ReturnModel();
		rq.setStatu(ReturnStatus.ParamError_2);
		if (address != null && address.getUserid() != null && address.getUserid() > 0) {
			if(ObjectUtil.isEmpty(address.getReciver())){
				rq.setStatusreson("收货人信息不能为空！");
				return rq;
			}
			if (!ObjectUtil.validSqlStr(address.getReciver())) {
				rq.setStatusreson("收货人信息存在危险！");
				return rq;
			}
			if(ObjectUtil.isEmpty(address.getPhone())){
				rq.setStatusreson("联系方式不能为空！");
				return rq;
			}
			if (!ObjectUtil.isMobile(address.getPhone())) {
				rq.setStatu(ReturnStatus.ParamError_2);
				rq.setStatusreson("手机号格式不对！");
				return rq;
			}
			if (!ObjectUtil.isEmpty(address.getStreetdetail()) && !ObjectUtil.validSqlStr(address.getStreetdetail())) {
				rq.setStatu(ReturnStatus.ParamError_2);
				rq.setStatusreson("街道详情存在危险");
				return rq;
			}
			if (!ObjectUtil.isEmpty(address.getPostcode()) && !ObjectUtil.validSqlStr(address.getPostcode())) {
				rq.setStatu(ReturnStatus.ParamError_2);
				rq.setStatusreson("邮政编码存在危险");
				return rq;
			}
			if (address.getAddrid() != null && address.getAddrid() > 0) {
				UUserAddressResult temp = addressMapper.get_UUserAddressResultByKeyId(address.getAddrid());
				if (temp != null) {
					if (temp.getUserid().longValue() != address.getUserid().longValue()) {
						rq.setStatu(ReturnStatus.ParamError_2);
						rq.setStatusreson("您没有权限修改别人的收货地址！");
						return rq;
					}
					addressMapper.updateByPrimaryKeySelective(address);
					rq.setBasemodle(address.getAddrid());
					rq.setStatu(ReturnStatus.Success);
					return rq;
				} else {
					rq.setStatu(ReturnStatus.SystemError);
					rq.setStatusreson("不存在的用户收货地址！");
					return rq;
				}
			} else {
				List<UUserAddressResult> list = addressMapper.find_UUserAddressByUserId(address.getUserid());
				if (list != null && list.size() > 0) {
					address.setAddrid(list.get(0).getAddrid());  
					addressMapper.updateByPrimaryKeySelective(address);
				}else {
					address.setIsdefault(1);
					addressMapper.insertReturnId(address);
				}
				rq.setStatu(ReturnStatus.Success);
				rq.setBasemodle(address.getAddrid());
			}
		} else {
			rq.setStatu(ReturnStatus.ParamError_1);
			rq.setStatusreson("参数有误");
		}
		return rq;
	}

	public UUserAddressResult getUserAddressResult(Long userId, Long addressId) {
		UUserAddressResult userAddressResult = null;
		if (addressId == null || addressId <= 0) {
			userAddressResult = addressMapper.get_UUserAddressDefault(userId);
		} else {
			userAddressResult = addressMapper.get_UUserAddressResultByKeyId(addressId);
		}
		if (userAddressResult != null && userId.longValue() == userAddressResult.getUserid().longValue()) {
			userAddressResult.setProvinceName(regionService.getProvinceName(userAddressResult.getProvince()));
			userAddressResult.setCityName(regionService.getCityName(userAddressResult.getCity()));
			userAddressResult.setAreaName(regionService.getAresName(userAddressResult.getArea()));
			return userAddressResult;
		}else {
			return null;
		}
	}
	

	public UUserAddressResult getBranchAddressResult(Long userId){
		UUsers users = usersMapper.selectByPrimaryKey(userId);
		if (users != null) {
			UBranches branches = null;
			if (ValidateUtils.isIdentity(users.getIdentity(), UserIdentityEnums.branch)) {
				branches = branchMapper.selectByPrimaryKey(userId);
			} else if (ValidateUtils.isIdentity(users.getIdentity(), UserIdentityEnums.salesman)) {
				UBranchusers branchusers = branchusersMapper.selectByPrimaryKey(userId);
				if (branchusers != null && branchusers.getBranchuserid() != null) {
					branches = branchMapper.selectByPrimaryKey(branchusers.getBranchuserid());
				}
			}
			if (branches != null) {
				UUserAddressResult orderAddress = new UUserAddressResult();
				orderAddress.setUserid(branches.getBranchuserid());
				orderAddress.setPhone(branches.getPhone());
				orderAddress.setReciver(branches.getUsername());
				orderAddress.setCityName(regionService.getCityName(branches.getCity()));
				orderAddress.setProvinceName(regionService.getProvinceName(branches.getProvince()));
				orderAddress.setAreaName(regionService.getAresName(branches.getArea()));
				orderAddress.setStreetdetail(branches.getStreetdetail());
				return orderAddress;
			}
		}
		return null;
	}

}
