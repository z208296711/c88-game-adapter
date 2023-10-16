package com.c88.game.adapter.service.third.png;

import lombok.Data;

@Data
public class RegisterUserReq extends BaseXmlReq {

    String externalUserId;
    String userName;
    String nickName;
    String currency;
    String country;
    String birthDate;
    String registration;
    String brandId;
    String language;
    String ip;
    String locked;
    String gender;

    public String toXml() {
        StringBuilder message = new StringBuilder();
        message.append("<v1:RegisterUser>")
                .append("<v1:UserInfo>")
                .append("<v1:ExternalUserId>").append(externalUserId).append("</v1:ExternalUserId>")
                .append("<v1:Username>").append(userName).append("</v1:Username>")
                .append("<v1:Nickname>").append(nickName).append("</v1:Nickname>")
                .append("<v1:Currency>").append(currency).append("</v1:Currency>")
                .append("<v1:Country>").append(country).append("</v1:Country>")
                .append("<v1:Birthdate>").append(birthDate).append("</v1:Birthdate>")
                .append("<v1:Registration>").append(registration).append("</v1:Registration>")
                .append("<v1:BrandId>").append(brandId).append("</v1:BrandId>")
                .append("<v1:Language>").append(language).append("</v1:Language>")
                .append("<v1:IP>").append(ip).append("</v1:IP>")
                .append("<v1:Locked>").append(locked).append("</v1:Locked>")
                .append("<v1:Gender>").append(gender).append("</v1:Gender>")
                .append("</v1:UserInfo>")
                .append("</v1:RegisterUser>");
        return xmlFormat().replace("{0}", message.toString());
    }
}
