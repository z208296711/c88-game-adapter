package com.c88.game.adapter.service.third.png;

import lombok.Data;

@Data
public class DebitReq extends BaseXmlReq {

    String externalUserId;
    String amount;
    String currency;
    String externalTransactionId;

    public String toXml() {
        StringBuilder message = new StringBuilder();
        message.append("<v1:Debit>")
                .append("<v1:ExternalUserId>").append(externalUserId).append("</v1:ExternalUserId>")
                .append("<v1:Amount>").append(amount).append("</v1:Amount>")
                .append("<v1:Currency>").append(currency).append("</v1:Currency>")
                .append("<v1:ExternalTransactionId>").append(externalTransactionId).append("</v1:ExternalTransactionId>")
                .append("</v1:Debit>");
        return xmlFormat().replace("{0}", message.toString());
    }
}
