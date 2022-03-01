package com.xxl.job.client.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName: ApiUtil
 * @Description:
 * @Author LXYuuuuu
 * @Date 2020/6/30 9:47
 */
public class XxlJobUtil {

	public static Logger logger = LoggerFactory.getLogger(XxlJobUtil.class);

	private static String cookie = "";

	/**
	 * 新增/编辑任务
	 * 
	 * @param url
	 * @param requestInfo
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static JSONObject addJob(String url, JSONObject requestInfo) throws HttpException, IOException {
		String path = "/jobinfo/add";
		return doPost(url, requestInfo, path);
	}

	public static JSONObject updateJob(String url, JSONObject requestInfo) throws HttpException, IOException {
		String path = "/jobinfo/update";
		return doPost(url, requestInfo, path);
	}

	/**
	 * 删除任务
	 * 
	 * @param url
	 * @param id
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static JSONObject removeJob(String url, int id) throws HttpException, IOException {
		String path = "/jobinfo/remove?id=" + id;
		return doGet(url, path);
	}

	/**
	 * 开始任务
	 * 
	 * @param url
	 * @param id
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static JSONObject startJob(String url, int id) throws HttpException, IOException {
		String path = "/jobinfo/start?id=" + id;
		return doGet(url, path);
	}

	/**
	 * 停止任务
	 * 
	 * @param url
	 * @param id
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	public static JSONObject stopJob(String url, int id) throws HttpException, IOException {
		String path = "/jobinfo/stop?id=" + id;
		return doGet(url, path);
	}

	public static JSONObject doGet(String url, String path) throws HttpException, IOException {
		String targetUrl = url + path;
		HttpClient httpClient = new HttpClient();
		HttpMethod get = new GetMethod(targetUrl);
		get.setRequestHeader("cookie", cookie);
		httpClient.executeMethod(get);
		JSONObject result = new JSONObject();
		result = getJsonObject(get, result);
		return result;
	}
	
	public static JSONObject doPost(String url, JSONObject requestInfo, String path) throws HttpException, IOException {
		String targetUrl = url + path;
		HttpClient httpClient = new HttpClient();
		PostMethod post = new PostMethod(targetUrl);
		post.setRequestHeader("cookie", cookie);
		post.setRequestHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		Set<String> keySet = requestInfo.keySet();
		for (String key : keySet) {
			String value = requestInfo.get(key).toString();
			post.addParameter(key, value);
		}
		httpClient.executeMethod(post);
		JSONObject result = new JSONObject();
		result = getJsonObject(post, result);
		return result;
	}

	private static JSONObject getJsonObject(HttpMethod get, JSONObject result) throws IOException {
		InputStream inputStream = get.getResponseBodyAsStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer stringBuffer = new StringBuffer();
		String str = "";
		while ((str = br.readLine()) != null) {
			stringBuffer.append(str);
		}
		if (get.getStatusCode() == 200) {
			/**
			 * 使用此方式会出现 Going to buffer response body of large or unknown size. Using
			 * getResponseBodyAsStream instead is recommended. 异常 String
			 * responseBodyAsString = get.getResponseBodyAsString(); result =
			 * JSONObject.parseObject(responseBodyAsString);
			 */
			result = JSONObject.parseObject(stringBuffer.toString());
		} else {
			try {
				result = JSONObject.parseObject(stringBuffer.toString());
			} catch (Exception e) {
				result.put("error", stringBuffer.toString());
			}
		}
		return result;
	}

	public static String login(String url, String userName, String password) throws HttpException, IOException {
		String path = "/login?userName=" + userName + "&password=" + password + "&ifRemember=on";
		String targetUrl = url + path;
		HttpClient httpClient = new HttpClient();
		HttpMethod get = new PostMethod(targetUrl);
		httpClient.executeMethod(get);
		if (get.getStatusCode() == 200) {
			Cookie[] cookies = httpClient.getState().getCookies();
			StringBuffer tmpcookies = new StringBuffer();
			for (Cookie c : cookies) {
				tmpcookies.append(c.toString() + ";");
			}
			cookie = tmpcookies.toString();
		} else {
			try {
				cookie = "";
			} catch (Exception e) {
				cookie = "";
			}
		}
		return cookie;
	}
}