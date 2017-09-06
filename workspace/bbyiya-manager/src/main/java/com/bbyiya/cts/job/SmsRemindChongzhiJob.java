package com.bbyiya.cts.job;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import javax.annotation.Resource;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import com.bbyiya.cts.service.ISmsRemindUserService;
import com.bbyiya.utils.ConfigUtil;
import com.bbyiya.utils.ObjectUtil;

public class SmsRemindChongzhiJob extends QuartzJobBean {
	
	private Logger Log = Logger.getLogger(SmsRemindChongzhiJob.class);
	@Resource(name = "smsRemindUserService")
	private ISmsRemindUserService remindService;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		this.JobRun();
	}
	
	/**
	 * 公用job一天执行一次
	 */
	private void JobRun(){
		try {
			List<Map<String, String>> joblist=ConfigUtil.getMaplist("jobs");
			//加一个开关
			if(joblist!=null&&joblist.size()>0){
				for (Map<String, String> job : joblist) {
					if(ObjectUtil.parseInt(job.get("seton"))==1&&job.get("id").equalsIgnoreCase("smsReminUserToChongzhi")){
						remindService.smsReminUserToChongzhi();
						Log.info("smsReminUserToChongzhi短信通知充值任务执行完成！");
					}
				}	
				
			}
			
		} catch (Exception e) {
			Log.error(e.toString());
			Log.error("dotempAutoOrderSumbit方法执行出错！"+e.getMessage());
			e.printStackTrace();
		}
			

	}
	
	

	
}
