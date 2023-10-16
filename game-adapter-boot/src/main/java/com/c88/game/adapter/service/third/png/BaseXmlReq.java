package com.c88.game.adapter.service.third.png;

public class BaseXmlReq {

    protected String xmlFormat() {
        StringBuilder xml = new StringBuilder();
        xml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://playngo.com/v1\">")
                .append("<soapenv:Header/>")
                .append("<soapenv:Body>")
                .append("{0}")
                .append("</soapenv:Body>")
                .append("</soapenv:Envelope>");
        return xml.toString();
    }
}
