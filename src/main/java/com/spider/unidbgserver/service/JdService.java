package com.spider.unidbgserver.service;

//import com.sun.org.apache.xerces.internal.impl.xpath.XPath;

public interface JdService {

    public String getSign(String functionId, String uuid, String body);

    public String getLoginBody(byte[] input);

    public String getJniDecryptMsg(byte[] response);

    public String getJdsgParams(String uri, String param, String eid, String version, String unknown);
}
