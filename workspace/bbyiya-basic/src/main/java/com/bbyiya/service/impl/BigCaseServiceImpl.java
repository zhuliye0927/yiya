package com.bbyiya.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbyiya.dao.MBigcaseMapper;
import com.bbyiya.dao.MBigcaseclasstagMapper;
import com.bbyiya.dao.MBigcasecollectMapper;
import com.bbyiya.dao.MBigcasetagMapper;
import com.bbyiya.dao.MBigcaseuserimgsMapper;
import com.bbyiya.dao.UChildreninfoMapper;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.model.MBigcase;
import com.bbyiya.model.MBigcaseclasstag;
import com.bbyiya.model.MBigcasecollect;
import com.bbyiya.model.MBigcasetag;
import com.bbyiya.model.UChildreninfo;
import com.bbyiya.service.IBigCaseService;
import com.bbyiya.utils.ConfigUtil;
import com.bbyiya.utils.DateUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.utils.RedisUtil;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.bigcase.BigcaseResult;
import com.bbyiya.vo.bigcase.BigcaseTagResult;
import com.bbyiya.vo.bigcase.BigcasesummaryResult;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("bigCaseService")
@Transactional(rollbackFor = { RuntimeException.class, Exception.class })
public class BigCaseServiceImpl implements IBigCaseService {
	@Autowired
	private UChildreninfoMapper childMapper;
	@Autowired
	private MBigcaseMapper bigcaseMapper;
	@Autowired
	private MBigcaseclasstagMapper tagMapper;
	@Autowired
	private MBigcasetagMapper bigCaseMapper;

	@Autowired
	private MBigcaseuserimgsMapper imgMapper;
	
	@Autowired
	private MBigcasecollectMapper collectMapper;
	
	public PageInfo<MBigcase> find_MBigcasePage(Long userId, int pageIndex, int pageSize) {
		UChildreninfo child = childMapper.selectByPrimaryKey(userId);
		if (child != null) {
			try {
				int day = DateUtil.daysBetween(child.getBirthday(), new Date());
				BigCaseTime timeParam = getStartAndEndDay(day);
				if (timeParam != null) {
					PageHelper.startPage(pageIndex, pageSize);
					List<MBigcase> relist = bigcaseMapper.findMBigcaseList(timeParam.getStart(), timeParam.getEnd());
					PageInfo<MBigcase> pageInfo = new PageInfo<MBigcase>(relist);
					return pageInfo;
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	
	public List<MBigcase> find_MBigcaselist(LoginSuccessResult user, boolean isCurrent, int timeId) {
		BigCaseTime timeParam = null;
		if (isCurrent) {
			if (user.getBabyInfo() != null) {
				try {
					int day = DateUtil.daysBetween(user.getBabyInfo().getBirthday(), new Date());
					timeParam = getStartAndEndDay(day);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		} else if (timeId > 0) {
			timeParam = getStartAndEndDayById(timeId);
		}
		if (timeParam != null) {
			String key = "bigcase_start_" + timeParam.getStart() + "end_" + timeParam.getEnd();
			List<MBigcase> list = (List<MBigcase>) RedisUtil.getObject(key);
			if (list != null && list.size() > 0) {
				return list;
			} else {
				list = bigcaseMapper.findMBigcaseList(timeParam.getStart(), timeParam.getEnd());
				if (list != null && list.size() > 0) {
					RedisUtil.setObject(key, list, 3600);
					return list;
				}
			}
		}
		return null;
	}

	

	public PageInfo<BigcaseResult> find_MBigcaseResultPage(LoginSuccessResult user, int pageIndex, int pageSize) {
		if (user.getBabyInfo() != null) {
			try {
				int day = DateUtil.daysBetween(user.getBabyInfo().getBirthday(), new Date());
				BigCaseTime timeParam = getStartAndEndDay(day);
				if (timeParam != null) {
				
					PageHelper.startPage(pageIndex, pageSize);
					List<BigcaseResult> relist = bigcaseMapper.findMBigcaseResultList(timeParam.getStart(), day);
					PageInfo<BigcaseResult> pageInfo = new PageInfo<BigcaseResult>(relist);
					for (BigcaseResult mo : pageInfo.getList()) {
						mo.setTaglist(getBigcaseTagResultlist(mo.getCaseId()));
					}
					return pageInfo;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	
	public ReturnModel getBigcaseDetails(LoginSuccessResult user, Integer caseId) {
		ReturnModel rqModel=new ReturnModel();
		BigcaseResult caseResult=bigcaseMapper.getMBigcaseResultByCaseId(caseId);
		if(caseResult!=null){
			caseResult.setTaglist(getBigcaseTagResultlist(caseId)); 
			rqModel.setStatu(ReturnStatus.Success);
			rqModel.setBasemodle(caseResult); 
		}else {
			rqModel.setStatu(ReturnStatus.SystemError);
			rqModel.setStatusreson("找不到相应的事件");
		} 
		return rqModel;
	}

	public ReturnModel addCollection(Long userid, Integer caseId){
		ReturnModel rqModel=new ReturnModel();
		List<MBigcasecollect> coList=collectMapper.findMBigCaseCollect(userid);
		if(coList!=null&&coList.size()>0){
			for (MBigcasecollect cc : coList) {
				if(cc.getCaseid().intValue()==caseId){
					rqModel.setStatu(ReturnStatus.ParamError);
					rqModel.setStatusreson("您已经收藏了该条记录！");
					return rqModel;
				}
			}
		}
		MBigcase bigcase= bigcaseMapper.selectByPrimaryKey(caseId);
		if(bigcase!=null){
			MBigcasecollect model=new MBigcasecollect();
			model.setCaseid(caseId);
			model.setUserid(userid);
			model.setCreatetime(new Date());
			try {
				collectMapper.insert(model);
				rqModel.setStatu(ReturnStatus.Success);
				rqModel.setStatusreson("收藏成功！");
			} catch (Exception e) {
				// TODO: handle exception
				rqModel.setStatu(ReturnStatus.SystemError);
				rqModel.setStatusreson("收藏失败");
			}
		}else {
			rqModel.setStatu(ReturnStatus.ParamError_1);
			rqModel.setStatusreson("不存在的大事件");
		}
		return rqModel;
	}
	
	public List<BigcaseResult> find_MBigcasecollectlist(Long userId){
		List<BigcaseResult> results=new ArrayList<BigcaseResult>();
		List<MBigcasecollect> list=collectMapper.findMBigCaseCollect(userId);
		if(list!=null&&list.size()>0){
			for (MBigcasecollect mod : list) {
				BigcaseResult caseMod= bigcaseMapper.getMBigcaseResultByCaseId(mod.getCaseid());
				caseMod.setTaglist(getBigcaseTagResultlist(mod.getCaseid())); 
				results.add(caseMod);
			}
		}
		return results;
	}
	
	public BigcasesummaryResult getBigcasesummaryResult(int timeId) {
		// TODO
		return null;
	}

	
	public List<BigcaseTagResult> getBigcaseTagResultlist(Integer caseId) {
		String key = "caseTag_id_" + caseId;
		List<BigcaseTagResult> list = (List<BigcaseTagResult>) RedisUtil.getObject(key);
		if (list != null && list.size() > 0) {
			return list;
		}
		List<MBigcaseclasstag> classtags = tagMapper.findTagsByClassId(caseId);
		List<MBigcasetag> tags = bigCaseMapper.findBigCaseTagByCaseId(caseId);
		if (tags != null && tags.size() > 0) {
			List<BigcaseTagResult> tagResults = new ArrayList<BigcaseTagResult>();
			for (MBigcasetag tag : tags) {
				BigcaseTagResult model = new BigcaseTagResult();
				model.setCaseId(caseId);
				model.setTagId(tag.getId());
				model.setTagContent(tag.getTagcontent());
				for (MBigcaseclasstag ctag : classtags) {
					if (tag.getTagid().intValue() == ctag.getTagid().intValue()) {
						model.setTagName(ctag.getValue());
					}
				}
				tagResults.add(model);
			}
			if (tagResults != null && tagResults.size() > 0) {
				//TODO 缓存信息
				// RedisUtil.setObject(key, tagResults,36000);
			}
			return tagResults;
		}
		return null;
	}

	
	public BigCaseTime getStartAndEndDay(int theday) {
		BigCaseTime result = new BigCaseTime();
		List<Map<String, String>> timelist = ConfigUtil.getMaplist("timeIntervals");
		for (Map<String, String> map : timelist) {
			int start = ObjectUtil.parseInt(map.get("start"));
			int end = ObjectUtil.parseInt(map.get("end"));
			if (theday >= start && theday <= end) {
				result.setStart(start);
				result.setEnd(end);
				result.setTimeId(ObjectUtil.parseInt(map.get("id")));
				return result;
			}
		}
		return null;
	}

	public BigCaseTime getStartAndEndDayById(int timeId) {
		BigCaseTime result = new BigCaseTime();
		List<Map<String, String>> timelist = ConfigUtil.getMaplist("timeIntervals");
		for (Map<String, String> map : timelist) {
			int start = ObjectUtil.parseInt(map.get("start"));
			int end = ObjectUtil.parseInt(map.get("end"));
			int id = ObjectUtil.parseInt(map.get("id"));
			if (id == timeId) {
				result.setStart(start);
				result.setEnd(end);
				return result;
			}
		}
		return null;
	}

	/**
	 * 
	 * @author Administrator
	 *
	 */
	public class BigCaseTime {
		private int start;
		private int end;
		private int timeId;

		public int getStart() {
			return start;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public int getEnd() {
			return end;
		}

		public void setEnd(int end) {
			this.end = end;
		}

		public int getTimeId() {
			return timeId;
		}

		public void setTimeId(int timeId) {
			this.timeId = timeId;
		}

	}
}
