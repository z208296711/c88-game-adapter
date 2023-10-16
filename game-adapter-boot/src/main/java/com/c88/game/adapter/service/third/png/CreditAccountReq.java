package com.c88.game.adapter.service.third.png;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CreditAccountReq extends BaseXmlReq {

    String externalUserId;
    String amount;
    String currency;
    String externalTransactionId;
    String game;
    String externalGameSessionId;

    public String toXml() {
        StringBuilder message = new StringBuilder();
        message.append("<v1:CreditAccount>")
                .append("<v1:ExternalUserId>").append(externalUserId).append("</v1:ExternalUserId>")
                .append("<v1:Amount>").append(amount).append("</v1:Amount>")
                .append("<v1:Currency>").append(currency).append("</v1:Currency>")
                .append("<v1:ExternalTransactionId>").append(externalTransactionId).append("</v1:ExternalTransactionId>")
                .append("<v1:Game>").append(StringUtils.isBlank(game) ? "" : game).append("</v1:Game>")
                .append("<v1:ExternalGameSessionId>").append(externalGameSessionId).append("</v1:ExternalGameSessionId>")
                .append("</v1:CreditAccount>");
        return xmlFormat().replace("{0}", message.toString());
    }
}
