package com.c88.game.adapter.service.third.png;

import lombok.Data;

@Data
public class GetUnfinishedGamesReq extends BaseXmlReq {

    String externalId;

    public String toXml() {
        StringBuilder message = new StringBuilder();
        message.append("<v1:GetUnfinishedGames>")
                .append("<v1:ExternalId>").append(externalId).append("</v1:ExternalId>")
                .append("</v1:GetUnfinishedGames>");
        return xmlFormat().replace("{0}", message.toString());
    }
}
