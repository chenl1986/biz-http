package com.thvos.spring.biz.http.client.test;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.thvos.spring.biz.base.exception.SysException;
import com.thvos.spring.biz.base.packet.BizPacket;
import com.thvos.spring.biz.http.client.HttpClient;

public class HttpClientTest {

	@Test
	@SuppressWarnings("resource")
	public void testGet() throws SysException {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath*:beans/**.xml");
		HttpClient client = ctx.getBean(HttpClient.class);
		BizPacket packet = client.get("https://www.baidu.com");
		System.out.println(packet.getCode());
		System.out.println(packet.getData());
	}
}