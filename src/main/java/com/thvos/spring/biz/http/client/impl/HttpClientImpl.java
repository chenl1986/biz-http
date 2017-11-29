package com.thvos.spring.biz.http.client.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.thvos.spring.biz.base.exception.SysException;
import com.thvos.spring.biz.base.packet.BizPacket;
import com.thvos.spring.biz.base.util.BT;
import com.thvos.spring.biz.base.util.RuiPacket;
import com.thvos.spring.biz.http.client.HttpClient;

@Service
public class HttpClientImpl implements HttpClient {

	private @Value("${http.connect.max}") int maxConPerHost = 300;

	private @Value("${http.connect.timeout}") int conTimeOutMs = 30000;

	private @Value("${http.socket.timeout}") int soTimeOutMs = 10000;

	private @Value("${http.cookies.ignore}") boolean ignore = false;

	private MultiThreadedHttpConnectionManager connectionManager;

	private org.apache.commons.httpclient.HttpClient client = null;

	@Override
	public void afterPropertiesSet() throws Exception {
		connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = connectionManager.getParams();
		params.setDefaultMaxConnectionsPerHost(maxConPerHost);
		params.setConnectionTimeout(conTimeOutMs);
		params.setSoTimeout(soTimeOutMs);
		HttpClientParams clientParams = new HttpClientParams();
		if(ignore) {
			clientParams.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		}
		client = new org.apache.commons.httpclient.HttpClient(clientParams, connectionManager);
	}

	@Override
	public BizPacket get(String url) throws SysException {
		return get(url, new RuiPacket<String, Object>());
	}

	@Override
	public BizPacket get(String url, RuiPacket<String, Object> packet) throws SysException {
		GetMethod getmethod = new GetMethod(url + (-1 == url.indexOf("?") ? "?" : "&") + encodeParameters(packet));
		return httpRequest(getmethod);
	}

	@Override
	public BizPacket delete(String url, RuiPacket<String, Object> packet) throws SysException {
		DeleteMethod deleteMethod = new DeleteMethod(url + (-1 == url.indexOf("?") ? "?" : "&") + encodeParameters(packet));
		return httpRequest(deleteMethod);
	}

	@Override
	public BizPacket post(String url, RuiPacket<String, Object> packet) throws SysException {
		PostMethod postMethod = new PostMethod(url);
		for(packet.first(); packet.existed(); packet.next()) {
			postMethod.addParameter(packet.getField(), BT.STRING.parse(packet.getValue()));
		}
		HttpMethodParams param = postMethod.getParams();
		param.setContentCharset("UTF-8");
		return httpRequest(postMethod);
	}
	
	@Override
	public BizPacket post(String url,String xmlParam) throws SysException{
		PostMethod postMethod = new PostMethod(url);
		try {
			StringRequestEntity entity = new StringRequestEntity(xmlParam,"text/xml", "UTF-8");
			postMethod.setRequestEntity(entity);
			HttpMethodParams param = postMethod.getParams();
			param.setContentCharset("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return httpRequest(postMethod);
	}

	public BizPacket httpRequest(HttpMethod method) throws SysException {
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		BizPacket packet = new BizPacket();
		try {
			client.executeMethod(method);
			packet.setCode(method.getStatusCode());
			packet.setData(method.getResponseBodyAsString());
			return packet;
		} catch (IOException e) {
			throw new SysException("Http client request error! status[#0]", e, packet.getCode());
		} finally {
			method.releaseConnection();
		}
	}

	private String encodeParameters(RuiPacket<String, Object> packet) {
		StringBuffer buf = new StringBuffer();
		for(packet.first(); packet.existed(); packet.next()) {
			try {
				buf.append("&").append(URLEncoder.encode(packet.getField(), "UTF-8"))
					.append("=").append(URLEncoder.encode(BT.STRING.parse(packet.getValue()), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
			}
		}
		return buf.length() > 0 ? buf.substring(1) : buf.toString();
	}
}