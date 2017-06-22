package com.bbyiya.pic.web.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.dao.PMyproductactivitycodeMapper;
import com.bbyiya.dao.PMyproducttempMapper;
import com.bbyiya.dao.PProductstylesMapper;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.enums.pic.ActivityCodeStatusEnum;
import com.bbyiya.model.PMyproductactivitycode;
import com.bbyiya.model.PMyproducttemp;
import com.bbyiya.model.PProductstyles;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.web.base.SSOController;

@Controller
@RequestMapping(value = "/myProduct/yiye")
public class YiyeCodeController extends SSOController {
	@Autowired
	private PMyproductactivitycodeMapper codeMapper;
	@Autowired
	private PMyproducttempMapper tempMapper;

	@Autowired
	private PProductstylesMapper styleMapper;
	/**
	 * �����֤
	 * 
	 * @param commentJson
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/codecheck")
	public String access(String codenum,int tempId) throws Exception {
		ReturnModel rq = new ReturnModel();
		rq.setStatu(ReturnStatus.ParamError);
		LoginSuccessResult user = super.getLoginUser();
		if (user != null) {
			if (!ObjectUtil.isEmpty(codenum)) {
				PMyproductactivitycode codeMod = codeMapper.selectByPrimaryKey(codenum);
				if (codeMod != null) {
					if (codeMod.getStatus() != null && codeMod.getStatus().intValue() == Integer.parseInt(ActivityCodeStatusEnum.used.toString())) {
						rq.setStatu(ReturnStatus.ParamError);
						rq.setStatusreson("������˼�����Ļ���Ѿ�ʹ�ù���");
						return JsonUtil.objectToJsonStr(rq);
					} else if (codeMod.getStatus() == null || codeMod.getStatus().intValue() == Integer.parseInt(ActivityCodeStatusEnum.notuse.toString())) {
						PMyproducttemp temp= tempMapper.selectByPrimaryKey(codeMod.getTempid());
						if(temp!=null&&temp.getTempid().intValue()==tempId){
							rq.setStatu(ReturnStatus.Success);
							Map<String, Object> map=new HashMap<String, Object>();
							if(temp.getStyleid()!=null){
								PProductstyles style= styleMapper.selectByPrimaryKey(temp.getStyleid());
								if(style!=null){
									map.put("price", style.getPrice());
									map.put("property", style.getPropertystr());
									rq.setBasemodle(map); 
								}
							}
							
						}else {
							rq.setStatu(ReturnStatus.ParamError);
							rq.setStatusreson("������˼�����Ļ�벻֧���ڱ��ʹ�ã���PS:���ֻ����ָ�����ʹ�ã���");
						}
					} else {
						rq.setStatusreson("������˼�����Ļ��ʧЧ��");
						return JsonUtil.objectToJsonStr(rq);
					}
				}else {
					rq.setStatusreson("���ź���������Ļ�벻��ȷ��δ�ܻ�û�ʸ�");
				}
			} else {
				rq.setStatu(ReturnStatus.ParamError);
				rq.setStatusreson("�������룡");
			}
		}
		return JsonUtil.objectToJsonStr(rq);
	}

}