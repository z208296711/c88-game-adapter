package com.c88.game.adapter.service.third.png;

import lombok.Data;

@Data
public class GetTicketReq extends BaseXmlReq {

    String externalUserId;

    public String toXml() {
        StringBuilder message = new StringBuilder();
        message.append("<v1:GetTicket>")
                .append("<v1:ExternalUserId>").append(externalUserId).append("</v1:ExternalUserId>")
                .append("</v1:GetTicket>");
        return xmlFormat().replace("{0}", message.toString());
    }
}
