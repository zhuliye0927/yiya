package com.bbyiya.pic.web;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.service.IUserLoginService;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.OtherLoginParam;
import com.bbyiya.web.base.SSOController;
import com.bbyiyia.pic.service.IPic_UserMgtService;

@Controller
@RequestMapping(value = "/login")
public class LoginController  extends SSOController {
	/**
	 * ��½��ע�� service
	 */
	@Resource(name = "pic_userMgtService")
	private IPic_UserMgtService loginService; 
	
	/**
	 * ��������¼
	 * @param headImg
	 * @param loginType
	 * @param nickName
	 * @param openId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/otherLogin")
	public String otherLogin(String headImg, @RequestParam(required = false, defaultValue = "2")int loginType, String nickName, String openId) throws Exception {
		headImg=ObjectUtil.urlDecoder_decode(headImg, "");
		nickName=ObjectUtil.urlDecoder_decode(nickName, "");
		openId=ObjectUtil.urlDecoder_decode(openId, "");
		if(!ObjectUtil.validSqlStr(headImg)||!ObjectUtil.validSqlStr(nickName)||!ObjectUtil.validSqlStr(openId)){
			ReturnModel rqModel=new ReturnModel();
			rqModel.setStatu(ReturnStatus.ParamError_2);
			rqModel.setStatusreson("�����зǷ��ַ�");
			return JsonUtil.objectToJsonStr(rqModel);
		}
		OtherLoginParam param = new OtherLoginParam();
		param.setOpenId(openId);
		param.setLoginType(loginType);
		param.setNickName(nickName);
		param.setHeadImg(headImg);
		return JsonUtil.objectToJsonStr(loginService.otherLogin(param));
	}
}