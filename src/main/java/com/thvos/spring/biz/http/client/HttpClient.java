package com.thvos.spring.biz.http.client;

import org.springframework.beans.factory.InitializingBean;

import com.thvos.spring.biz.base.exception.SysException;
import com.thvos.spring.biz.base.packet.BizPacket;
import com.thvos.spring.biz.base.util.RuiPacket;

public interface HttpClient extends InitializingBean {

	public BizPacket get(String url) throws SysException;

	public BizPacket get(String url, RuiPacket<String, Object> packet) throws SysException;

	public BizPacket delete(String url, RuiPacket<String, Object> packet) throws SysException;

	public BizPacket post(String url, RuiPacket<String, Object> packet) throws SysException;
	
	public BizPacket post(String url,String xmlParam) throws SysException; 
}