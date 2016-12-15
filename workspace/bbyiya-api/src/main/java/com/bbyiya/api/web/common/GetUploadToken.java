package com.bbyiya.api.web.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.upload.FileUploadUtils_qiniu;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.web.base.SSOController;

@Controller
@RequestMapping(value = "/upload")
public class GetUploadToken  extends SSOController {
	
	@ResponseBody
	@RequestMapping(value = "/getUploadToken")
	public String loginAjax(String phone, String pwd) throws Exception {
		ReturnModel rq = new ReturnModel();
		if(super.getLoginUser()!=null){
			String token=FileUploadUtils_qiniu.getUpToken();
			rq.setStatu(ReturnStatus.Success);
			Map<String, Object> map=new HashMap<String, Object>();
			map.put("upToken", token);
			rq.setBasemodle(map);
		}else {
			rq.setStatu(ReturnStatus.LoginError);
			rq.setStatusreson("��¼����");
		}
		return JsonUtil.objectToJsonStr(rq);
	}
}
