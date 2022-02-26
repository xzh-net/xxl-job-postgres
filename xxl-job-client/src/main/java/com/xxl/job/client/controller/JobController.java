package com.xxl.job.client.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.xxl.job.client.common.model.CommonResult;
import com.xxl.job.client.utils.CronUtils;
import com.xxl.job.client.utils.XxlJobUtil;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 发送邮件
 * 
 * @author Administrator
 *
 */
@Api(tags = "作业客户端")
@RestController
public class JobController {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

	@Value("${xxl.job.admin.addresses}")
	private String adminAddresses;
	@Value("${xxl.job.executor.appname}")
	private String executorAppname;

	/**
	 * 登陆
	 *
	 * @param userName
	 * @param password
	 * @return
	 * @throws IOException
	 */
	@ApiOperation("登录")
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public CommonResult login(String userName, String password) {
		try {
			String cookie = XxlJobUtil.login(adminAddresses, userName, password);
			if (StrUtil.isNotBlank(cookie)) {
				return CommonResult.success("成功");
			} else {
				throw new Exception("调用xxl-job-admin-login接口失败！");
			}
		} catch (Exception e) {
			return CommonResult.success("失败" + e.getMessage());
		}
	}

	@ApiOperation("添加jobInfo并启动")
	@RequestMapping(value = "/saveXxl", method = RequestMethod.GET)
	public CommonResult saveXxl() {
		try {
			JSONObject requestInfo = new JSONObject();
			/* 基础配置 */
			requestInfo.put("jobGroup", 1);// 执行器主键ID
			requestInfo.put("jobDesc", "订单自动取消");// 任务描述
			requestInfo.put("author", "xzh");// 负责人
			requestInfo.put("alarmEmail", "xzh@hotmail.com");// 报警邮件

			/* 调度配置 */
			requestInfo.put("scheduleType", "CRON");// 调度类型 固定:FIX_RATE 表达式：CRON 无：NONE
			long etime1 = System.currentTimeMillis() + 1 * 60 * 1000;// // 任务执行CRON表达式 :延时函数，单位毫秒，这里是延时了1分钟
			// requestInfo.put("jobCron", CronUtils.getCron(new Date(etime1)));
			requestInfo.put("scheduleConf", "0/10 * * * * ?");

			/* 任务配置 */
			requestInfo.put("glueType", "BEAN");// 运行模式
			requestInfo.put("executorHandler", "httpJobHandler"); // 执行器务Handler名称
			requestInfo.put("executorParam",
					"{\"url\": \"http://192.168.3.200/getUser\",\"method\": \"GET\",\"param\": {\"v\":\"1.0\"}}");//任务参数

			/* 高级配置 */
			requestInfo.put("executorRouteStrategy", "FIRST");// 路由策略
			requestInfo.put("childJobId", "");// 子任务ID
			requestInfo.put("misfireStrategy", "DO_NOTHING");// 调度过期策略 忽略：DO_NOTHING  立即执行一次：FIRE_ONCE_NOW
			requestInfo.put("executorBlockStrategy", "SERIAL_EXECUTION");// 阻塞处理策略 单机穿行：SERIAL_EXECUTION
			requestInfo.put("executorTimeout", 0);// 任务超时时间，单位秒
			requestInfo.put("executorFailRetryCount", 1);// 失败重试次数

			// 其他
			requestInfo.put("triggerStatus", 0);// 调度状态：0-停止，1-运行 ，1-启动
			requestInfo.put("triggerLastTime", 0);// 上次调度时间
			requestInfo.put("triggerNextTime", 0);// 下次调度时间

			JSONObject response = XxlJobUtil.addJob(adminAddresses, requestInfo);
			if (response.containsKey("code") && 200 == (Integer) response.get("code")) {
				// 修改任务参数 把id放入
				// 执行器主键ID
				requestInfo.put("executorParam", "JobId=" + response.get("content") + ";测试202006300943");
				requestInfo.put("id", Integer.valueOf(response.get("content").toString()));
				JSONObject responseUpdate = XxlJobUtil.updateJob(adminAddresses, requestInfo);
				if (responseUpdate.containsKey("code") && 200 == (Integer) responseUpdate.get("code")) {
					// 加入任务成功之后直接启动
					JSONObject responseStart = XxlJobUtil.startJob(adminAddresses,
							Integer.valueOf(response.get("content").toString()));
					if (responseStart.containsKey("code") && 200 == (Integer) responseStart.get("code")) {
						return CommonResult.success("成功");
					} else {
						return CommonResult.failed("调用xxl-job-admin-start接口失败！");
					}
				} else {
					return CommonResult.failed("调用xxl-job-admin-update接口失败！");
				}
			} else {
				return CommonResult.failed(response.toString());
			}
		} catch (Exception e) {
			return CommonResult.failed("失败" + e.getMessage());
		}
	}

}