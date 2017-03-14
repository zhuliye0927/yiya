package com.bbyiya.pic.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbyiya.common.enums.SendMsgEnums;
import com.bbyiya.common.vo.ResultMsg;
import com.bbyiya.dao.UOtherloginMapper;
import com.bbyiya.dao.UUsersMapper;
import com.bbyiya.dao.UUsertesterwxMapper;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.enums.user.UserStatusEnum;
import com.bbyiya.model.UOtherlogin;
import com.bbyiya.model.UUsers;
import com.bbyiya.model.UUsertesterwx;
import com.bbyiya.pic.service.IPic_UserMgtService;
import com.bbyiya.service.IUserLoginService;
//import com.bbyiya.utils.ConfigUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.utils.RedisUtil;
import com.bbyiya.utils.SendSMSByMobile;
import com.bbyiya.utils.encrypt.MD5Encrypt;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.vo.user.OtherLoginParam;

@Service("pic_userMgtService")
@Transactional(rollbackFor = { RuntimeException.class, Exception.class })
public class Pic_UserMgtService implements IPic_UserMgtService {
	@Autowired
	private UOtherloginMapper otherloginMapper;
	@Autowired
	private UUsersMapper userDao;
	@Resource(name = "userLoginService")
	private IUserLoginService baseLoginService;

	@Autowired
	private UUsertesterwxMapper testerMapper;
	
	/**
	 * ��������½
	 * 
	 * @param param
	 */
	public ReturnModel otherLogin(OtherLoginParam param) {
		ReturnModel rq = new ReturnModel();
		try {
			if (param != null) {
				UOtherlogin others = otherloginMapper.get_UOtherlogin(param);
				if (others != null) {
					UUsers user = userDao.selectByPrimaryKey(others.getUserid());
					LoginSuccessResult loginSuccessResult = null;
					if (user != null) {
						loginSuccessResult = baseLoginService.loginSuccess(user);
						//�����˺�
						UUsertesterwx testerUsertesterwx= testerMapper.selectByPrimaryKey(user.getUserid());
						if(testerUsertesterwx!=null&&testerUsertesterwx.getStatus().intValue()==1){
							loginSuccessResult.setIsTester(1); 
						}
						
					} else {
						return otherRegiter(param);
					}
					rq.setStatu(ReturnStatus.Success);
					rq.setBasemodle(loginSuccessResult);
				} else {
					return otherRegiter(param);
				}
			} else {
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("��������Ϊ��");
			}
		} catch (Exception e) {
			rq.setStatu(ReturnStatus.SystemError);
			rq.setStatusreson("ע��ʧ�ܣ�");
			RedisUtil.setObject("register:", e); 
		}
		return rq;
	}

	/**
	 * �û�������ע��
	 * 
	 * @param param
	 * @return
	 */
	public ReturnModel otherRegiter(OtherLoginParam param) throws Exception {
		ReturnModel rq = new ReturnModel();
		rq.setStatu(ReturnStatus.ParamError);
		if (param != null) {
			if (ObjectUtil.isEmpty(param.getOpenId())) {
				rq.setStatusreson("openid����Ϊ��");
				return rq;
			}
			if (param.getLoginType() == null) {
				rq.setStatusreson("���Ͳ���Ϊ��");
				return rq;
			}
			UOtherlogin other = otherloginMapper.get_UOtherlogin(param);
			if (other == null) {
				UUsers userModel = new UUsers();
				userModel.setCreatetime(new Date());
				userModel.setStatus(Integer.parseInt(UserStatusEnum.noPwd.toString()));
				if(!ObjectUtil.isEmpty(param.getNickName())){
					userModel.setNickname(param.getNickName());
				}
				if(!ObjectUtil.isEmpty(param.getHeadImg())){
					userModel.setUserimg(param.getHeadImg());
				} 
				userDao.insertReturnKeyId(userModel);
				
				other = new UOtherlogin();
				other.setUserid(userModel.getUserid());
				other.setOpenid(param.getOpenId());
				other.setLogintype(param.getLoginType());
				other.setNickname(param.getNickName());
				other.setImage(param.getHeadImg());
				other.setStatus(Integer.parseInt(UserStatusEnum.noPwd.toString()));
				other.setCreatetime(new Date());
				otherloginMapper.insert(other);
				
				LoginSuccessResult result = baseLoginService.loginSuccess(userModel);
				
				rq.setStatu(ReturnStatus.Success);
				rq.setStatusreson("ע��ɹ�");
				rq.setBasemodle(result);
			} else if (other.getUserid() != null && other.getUserid() > 0) {
				UUsers userModel = userDao.getUUsersByUserID(other.getUserid());
				if (userModel != null) {
					LoginSuccessResult result = baseLoginService.loginSuccess(userModel);
					rq.setStatu(ReturnStatus.Success);
					rq.setStatusreson("��¼�ɹ�");
					rq.setBasemodle(result);
				}
			} else {
				//ע���û�
				UUsers userModel = new UUsers();
				userModel.setCreatetime(new Date());
				userModel.setStatus(Integer.parseInt(UserStatusEnum.noPwd.toString()));
				userDao.insertReturnKeyId(userModel);
				
				other.setUserid(userModel.getUserid());
				otherloginMapper.updateByPrimaryKeySelective(other);

				LoginSuccessResult result = baseLoginService.loginSuccess(userModel);
				rq.setStatu(ReturnStatus.Success);
				rq.setStatusreson("��¼�ɹ�");
				rq.setBasemodle(result);
			}

		} else {
			rq.setStatusreson("��������");
		}
		return rq;
	}
	
	
	public ReturnModel setPwd(Long userId,String pwd,String phone,String vcode){
		ReturnModel rq=new ReturnModel();
		if(ObjectUtil.isEmpty(pwd)){
			rq.setStatu(ReturnStatus.ParamError);
			rq.setStatusreson("���벻��Ϊ��");
			return rq;
		}
		ResultMsg msgResult= SendSMSByMobile.validateCode(phone, vcode, SendMsgEnums.register);
		if(msgResult.getStatus()==1) {
			UUsers userPhone=userDao.getUUsersByPhone(phone);
			if(userPhone!=null&&userPhone.getUserid().longValue()!=userId){
				rq.setStatu(ReturnStatus.SystemError);
				rq.setStatusreson("���ֻ����Ѿ��������û���");
				return rq;
			}
			UUsers user= userDao.getUUsersByUserID(userId);
			if(user!=null){
				user.setMobilephone(phone);
				user.setStatus(Integer.parseInt(UserStatusEnum.ok.toString())); 
				user.setPassword(MD5Encrypt.encrypt(pwd)); 
				userDao.updateByPrimaryKey(user);
				LoginSuccessResult result = baseLoginService.getLoginSuccessResult_Common(user);
				rq.setStatu(ReturnStatus.Success); 
				rq.setBasemodle(result); 
			}else {
				rq.setStatu(ReturnStatus.SystemError);
				rq.setStatusreson("ϵͳ����");
			}
		}else {
			rq.setStatu(ReturnStatus.ParamError);
			rq.setStatusreson(msgResult.getMsg()); 
		}
		return rq;
		
	}
}