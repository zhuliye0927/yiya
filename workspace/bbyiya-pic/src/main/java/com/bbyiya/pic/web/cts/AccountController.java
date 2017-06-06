package com.bbyiya.pic.web.cts;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.baseUtils.GenUtils;
import com.bbyiya.baseUtils.ValidateUtils;
import com.bbyiya.dao.UAccountsMapper;
import com.bbyiya.dao.UAdminMapper;
import com.bbyiya.dao.UAdminactionlogsMapper;
import com.bbyiya.dao.UCashlogsMapper;
import com.bbyiya.dao.UUsersMapper;
import com.bbyiya.enums.AdminActionType;
import com.bbyiya.enums.AmountType;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.enums.user.UserIdentityEnums;
import com.bbyiya.model.UAccounts;
import com.bbyiya.model.UAdmin;
import com.bbyiya.model.UAdminactionlogs;
import com.bbyiya.model.UCashlogs;
import com.bbyiya.model.UUsers;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.web.base.SSOController;

@Controller
@RequestMapping(value = "/cts/account")
public class AccountController  extends SSOController{

	
	@Autowired
	private UUsersMapper userMapper;
	@Autowired
	private UAccountsMapper accountMapper;
	@Autowired
	private UCashlogsMapper cashlogMapper;
	@Autowired
	private UAdminactionlogsMapper actLogMapper;
	@Autowired
	private UAdminMapper adminMapper;
	/**
	 * cts �û���ֵ
	 * @param branchuserid
	 * @param amount
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/chongzhi")
	public String chongzhi(long branchuserid,String  amount) throws Exception {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult user=super.getLoginUser();
		double amountPrice=ObjectUtil.parseDouble(amount);
		if(user!=null) {
			if(ValidateUtils.isIdentity(user.getIdentity(), UserIdentityEnums.cts_admin)){
				UUsers branch= userMapper.getUUsersByUserID(branchuserid);
				if(branch!=null&&ValidateUtils.isIdentity(branch.getIdentity(), UserIdentityEnums.branch)) {
					UAccounts accounts=accountMapper.selectByPrimaryKey(branchuserid);
					if(accounts!=null&&accounts.getAvailableamount()!=null&&accounts.getAvailableamount()>1000){
						if(amountPrice>10){
							rq.setStatu(ReturnStatus.SystemError);
							rq.setStatusreson("���û��˻����н��"+accounts.getAvailableamount());
							return JsonUtil.objectToJsonStr(rq);
						}
					}
					String payId=GenUtils.getOrderNo(9999l); 
					UCashlogs log=new UCashlogs();
					log.setAmount(amountPrice);
					log.setUserid(branchuserid);
					log.setPayid(payId);
					log.setUsetype(Integer.parseInt(AmountType.get.toString()));//��ֵ
					log.setCreatetime(new Date());
					cashlogMapper.insert(log);
					UCashlogs freeLog=new UCashlogs();
					freeLog.setAmount(amountPrice*2);
					freeLog.setUserid(branchuserid);
					freeLog.setPayid(payId);
					freeLog.setUsetype(Integer.parseInt(AmountType.free.toString()));//��ֵ
					freeLog.setCreatetime(new Date());
					cashlogMapper.insert(freeLog);
					//��ֵ ��� = ʵ�ʽ��*3 
					Double totalPriceTemp=amountPrice*3;
					if(accounts!=null){
						accounts.setAvailableamount(accounts.getAvailableamount()+totalPriceTemp);
						accountMapper.updateByPrimaryKeySelective(accounts);
					}else {
						accounts=new UAccounts();
						accounts.setUserid(branchuserid);
						accounts.setAvailableamount(totalPriceTemp);
						accountMapper.insert(accounts);
					}
					addActionLog(user.getUserId(),"[�˻���ֵ]�����ɹ�����ֵ��"+totalPriceTemp+"Ԫ����ֵ�û� userId:"+branchuserid);
					rq.setStatusreson("��ֵ�ɹ����˻����ý�"+accounts.getAvailableamount()+"Ԫ!"); 
					rq.setStatu(ReturnStatus.Success);
				}else {
					rq.setStatu(ReturnStatus.SystemError);
					rq.setStatusreson("���û�����Ӱ¥���ݣ�"); 
				}
			}else {
				rq.setStatu(ReturnStatus.SystemError);
				rq.setStatusreson("Ȩ�޲��㣡");
			}
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}
	
	public void addActionLog(Long userId,String msg){
		UAdminactionlogs log=new UAdminactionlogs();
		log.setUserid(userId);
		log.setContent(msg);
		UAdmin admin= adminMapper.selectByPrimaryKey(userId);
		if(admin!=null){
			log.setUsername(admin.getUsername());
		}
		log.setType(Integer.parseInt(AdminActionType.chongzhi.toString()));
		log.setCreatetime(new Date());
		actLogMapper.insert(log);
	}
}