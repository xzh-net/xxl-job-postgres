package com.xxl.job.client.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.xxl.job.client.common.model.CommonResult;
import com.xxl.job.client.utils.XxlJobUtil;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 发送邮件
 * 
 * @author Administrator
 *
 */
@Api(tags = "任务客户端")
@RestController
public class JobController {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

	@Value("${xxl.job.admin.addresses}")
	private String adminAddresses;

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
				return CommonResult.success(cookie);
			} else {
				return CommonResult.failed();
			}
		} catch (Exception e) {
			return CommonResult.failed("失败" + e.getMessage());
		}
	}

	@ApiOperation("添加任务")
	@PostMapping(value = "/saveJob")
	public CommonResult saveJob(
			@ApiParam(value = "执行器id") @RequestParam(value = "jobGroup", defaultValue = "1") Integer jobGroup,
			@ApiParam(value = "任务描述") @RequestParam(name = "jobDesc") String jobDesc,
			@ApiParam(value = "负责人") @RequestParam(name = "author", defaultValue = "admin") String author,
			@ApiParam(value = "报警邮件") @RequestParam(name = "alarmEmail", defaultValue = "xzh@163.com") String alarmEmail,
			@ApiParam(value = "调度类型FIX_RATE:固定,CRON:表达式,NONE:无") @RequestParam(name = "scheduleType", defaultValue = "CRON") String scheduleType,
			@ApiParam(value = "Cron表达式/固定速度") @RequestParam(name = "scheduleConf", defaultValue = "0/10 * * * * ?") String scheduleConf,
			@ApiParam(value = "运行模式") @RequestParam(name = "glueType", defaultValue = "BEAN") String glueType,
			@ApiParam(value = "执行器，任务Handler名称") @RequestParam(name = "executorHandler", defaultValue = "httpJobHandler") String executorHandler,
			@ApiParam(value = "执行器，任务参数") @RequestParam(name = "executorParam", defaultValue = "{\"url\": \"http://172.17.17.200/getUser\",\"method\": \"GET\",\"param\": {\"v\":\"1.0\"}}") String executorParam,
			@ApiParam(value = "执行器路由策略 FIRST:第一个;LAST:最后一个;ROUND:轮询;RANDOM:随机;CONSISTENT_HASH:一致性HASH") @RequestParam(name = "executorRouteStrategy", defaultValue = "FIRST") String executorRouteStrategy,
			@ApiParam(value = "子任务ID，多个逗号分隔") @RequestParam(name = "childJobId") String childJobId,
			@ApiParam(value = "调度过期策略 忽略：DO_NOTHING 立即执行一次：FIRE_ONCE_NOW") @RequestParam(name = "misfireStrategy", defaultValue = "DO_NOTHING") String misfireStrategy,
			@ApiParam(value = "阻塞处理策略 单机穿行：SERIAL_EXECUTION") @RequestParam(name = "executorBlockStrategy", defaultValue = "SERIAL_EXECUTION") String executorBlockStrategy,
			@ApiParam(value = "任务超时时间，单位秒") @RequestParam(value = "executorTimeout", defaultValue = "0") Integer executorTimeout,
			@ApiParam(value = "失败重试次数") @RequestParam(value = "executorFailRetryCount", defaultValue = "1") Integer executorFailRetryCount,
			@ApiParam(value = "调度状态：0-停止，1-运行") @RequestParam(value = "triggerStatus", defaultValue = "0") Integer triggerStatus) {
		try {
			JSONObject requestInfo = new JSONObject();
			/* 基础配置 */
			requestInfo.put("jobGroup", jobGroup);// 执行器主键ID
			requestInfo.put("jobDesc", jobDesc);// 任务描述
			requestInfo.put("author", author);// 负责人
			requestInfo.put("alarmEmail", alarmEmail);// 报警邮件
			/* 调度配置 */
			requestInfo.put("scheduleType", scheduleType);// 调度类型
			requestInfo.put("scheduleConf", scheduleConf);// cron表达式/固定速度
			/* 任务配置 */
			requestInfo.put("glueType", glueType);// 运行模式
			requestInfo.put("executorHandler", executorHandler); // 执行器，任务Handler名称
			requestInfo.put("executorParam", executorParam);// 任务参数
			/* 高级配置 */
			requestInfo.put("executorRouteStrategy", executorRouteStrategy);// 执行器路由策略
			requestInfo.put("childJobId", childJobId);// 子任务ID，多个逗号分隔
			requestInfo.put("misfireStrategy", misfireStrategy);// 调度过期策略 忽略：DO_NOTHING 立即执行一次：FIRE_ONCE_NOW
			requestInfo.put("executorBlockStrategy", executorBlockStrategy);// 阻塞处理策略 单机穿行：SERIAL_EXECUTION
			requestInfo.put("executorTimeout", executorTimeout);// 任务超时时间，单位秒
			requestInfo.put("executorFailRetryCount", executorFailRetryCount);// 失败重试次数
			// 其他
			requestInfo.put("triggerStatus", triggerStatus);// 调度状态：0-停止，1-运行
			requestInfo.put("triggerLastTime", 0);// 上次调度时间
			requestInfo.put("triggerNextTime", 0);// 下次调度时间

			JSONObject response = XxlJobUtil.addJob(adminAddresses, requestInfo);
			if (response.containsKey("code") && 200 == (Integer) response.get("code")) {
				return CommonResult.success(null);
			} else {
				return CommonResult.failed(response.toString());
			}
		} catch (Exception e) {
			return CommonResult.failed("失败" + e.getMessage());
		}
	}

	@ApiOperation("修改任务")
	@PostMapping(value = "/updateJob")
	public CommonResult updateJob(
			@ApiParam(value = "任务id") @RequestParam(value = "id") String id,
			@ApiParam(value = "执行器id") @RequestParam(value = "jobGroup", defaultValue = "1") Integer jobGroup,
			@ApiParam(value = "任务描述") @RequestParam(name = "jobDesc") String jobDesc,
			@ApiParam(value = "负责人") @RequestParam(name = "author", defaultValue = "admin") String author,
			@ApiParam(value = "报警邮件") @RequestParam(name = "alarmEmail", defaultValue = "xzh@163.com") String alarmEmail,
			@ApiParam(value = "调度类型FIX_RATE:固定,CRON:表达式,NONE:无") @RequestParam(name = "scheduleType", defaultValue = "CRON") String scheduleType,
			@ApiParam(value = "Cron表达式/固定速度") @RequestParam(name = "scheduleConf", defaultValue = "0/10 * * * * ?") String scheduleConf,
			@ApiParam(value = "运行模式") @RequestParam(name = "glueType", defaultValue = "BEAN") String glueType,
			@ApiParam(value = "执行器，任务Handler名称") @RequestParam(name = "executorHandler", defaultValue = "httpJobHandler") String executorHandler,
			@ApiParam(value = "执行器，任务参数") @RequestParam(name = "executorParam", defaultValue = "{\"url\": \"http://172.17.17.200/getUser\",\"method\": \"GET\",\"param\": {\"v\":\"1.0\"}}") String executorParam,
			@ApiParam(value = "执行器路由策略 FIRST:第一个;LAST:最后一个;ROUND:轮询;RANDOM:随机;CONSISTENT_HASH:一致性HASH") @RequestParam(name = "executorRouteStrategy", defaultValue = "FIRST") String executorRouteStrategy,
			@ApiParam(value = "子任务ID，多个逗号分隔") @RequestParam(name = "childJobId") String childJobId,
			@ApiParam(value = "调度过期策略 忽略：DO_NOTHING 立即执行一次：FIRE_ONCE_NOW") @RequestParam(name = "misfireStrategy", defaultValue = "DO_NOTHING") String misfireStrategy,
			@ApiParam(value = "阻塞处理策略 单机穿行：SERIAL_EXECUTION") @RequestParam(name = "executorBlockStrategy", defaultValue = "SERIAL_EXECUTION") String executorBlockStrategy,
			@ApiParam(value = "任务超时时间，单位秒") @RequestParam(value = "executorTimeout", defaultValue = "0") Integer executorTimeout,
			@ApiParam(value = "失败重试次数") @RequestParam(value = "executorFailRetryCount", defaultValue = "1") Integer executorFailRetryCount) {
		try {
			JSONObject requestInfo = new JSONObject();
			requestInfo.put("id", id);
			/* 基础配置 */
			requestInfo.put("jobGroup", jobGroup);// 执行器主键ID
			requestInfo.put("jobDesc", jobDesc);// 任务描述
			requestInfo.put("author", author);// 负责人
			requestInfo.put("alarmEmail", alarmEmail);// 报警邮件
			/* 调度配置 */
			requestInfo.put("scheduleType", scheduleType);// 调度类型
			requestInfo.put("scheduleConf", scheduleConf);// cron表达式/固定速度
			/* 任务配置 */
			requestInfo.put("glueType", glueType);// 运行模式
			requestInfo.put("executorHandler", executorHandler); // 执行器，任务Handler名称
			requestInfo.put("executorParam", executorParam);// 任务参数
			/* 高级配置 */
			requestInfo.put("executorRouteStrategy", executorRouteStrategy);// 执行器路由策略
			requestInfo.put("childJobId", childJobId);// 子任务ID，多个逗号分隔
			requestInfo.put("misfireStrategy", misfireStrategy);// 调度过期策略 忽略：DO_NOTHING 立即执行一次：FIRE_ONCE_NOW
			requestInfo.put("executorBlockStrategy", executorBlockStrategy);// 阻塞处理策略 单机穿行：SERIAL_EXECUTION
			requestInfo.put("executorTimeout", executorTimeout);// 任务超时时间，单位秒
			requestInfo.put("executorFailRetryCount", executorFailRetryCount);// 失败重试次数
			// 其他
			requestInfo.put("triggerLastTime", 0);// 上次调度时间
			requestInfo.put("triggerNextTime", 0);// 下次调度时间

			JSONObject response = XxlJobUtil.updateJob(adminAddresses, requestInfo);
			if (response.containsKey("code") && 200 == (Integer) response.get("code")) {
				return CommonResult.success(null);
			} else {
				return CommonResult.failed(response.toString());
			}
		} catch (Exception e) {
			return CommonResult.failed("失败" + e.getMessage());
		}
	}

	@ApiOperation("启动指定id任务")
	@PostMapping(value = "/startJob/{id}")
	public CommonResult startJob(@PathVariable("id") Integer id) {
		try {
			JSONObject response = XxlJobUtil.startJob(adminAddresses, id);
			if (response.containsKey("code") && 200 == (Integer) response.get("code")) {
				return CommonResult.success(null);
			} else {
				return CommonResult.failed();
			}

		} catch (Exception e) {
			return CommonResult.failed("失败" + e.getMessage());
		}
	}

	@ApiOperation("停止指定id任务")
	@PostMapping(value = "/stopJob/{id}")
	public CommonResult stopJob(@PathVariable("id") Integer id) {
		try {
			JSONObject response = XxlJobUtil.stopJob(adminAddresses, id);
			if (response.containsKey("code") && 200 == (Integer) response.get("code")) {
				return CommonResult.success(null);
			} else {
				return CommonResult.failed();
			}
		} catch (Exception e) {
			return CommonResult.failed("失败" + e.getMessage());
		}
	}

	@ApiOperation("删除指定id任务")
	@PostMapping(value = "/removeJob/{id}")
	public CommonResult deleteJob(@PathVariable("id") Integer id) {
		try {
			JSONObject response = XxlJobUtil.removeJob(adminAddresses, id);
			if (response.containsKey("code") && 200 == (Integer) response.get("code")) {
				return CommonResult.success(null);
			} else {
				return CommonResult.failed();
			}
		} catch (Exception e) {
			return CommonResult.failed("失败" + e.getMessage());
		}
	}

}