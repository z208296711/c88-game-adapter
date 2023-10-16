package com.c88.game.adapter.service.third.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(properties = "spring.profiles.active:local")
class SABAGameAdapterTest {
    @Autowired
    private SABAGameAdapter sabaGameAdapter;

    ApiParameter apiParameter;

    @BeforeEach
    void init(){
//        apiParameter =
                sabaGameAdapter.init();
    }

    @Test
    void register() {
        String apiParam = "{\"apiId\": \"umd7fv90pl\", \"apiKey\": \"\", \"apiUrl\": \"http://v6h9tsa.bw6688.com\", \"others\": \"C88bet\", \"prefix\": \"dev\", \"recordUrl\": \"\"}";
        JSONObject jsonObject = JSON.parseObject(apiParam);
        ApiParameter apiParameter = new ApiParameter();
        apiParameter.setApiId(jsonObject.getString("apiId"));
        apiParameter.setApiUrl(jsonObject.getString("apiUrl"));
        apiParameter.setOthers(jsonObject.getString("others"));

        Platform platform = Platform.builder().id(27l).apiParameter(apiParameter).build();
        sabaGameAdapter.register(118l, "allen03", platform);
    }

    @Test
    void login(){
        Map map = new HashMap();
//        map.put("vendor_id", apiParameter.getApiId());
        map.put("gameplat", 1);
        map.put("vendor_member_id", "allen");

        sabaGameAdapter.login("allen", map);
    }

    @Test
    void balance(){
        sabaGameAdapter.balance("allen");
    }

    @Test
    void transferIn(){
        sabaGameAdapter.transferIntoThird("allen", new BigDecimal(100), "a123456");
    }

    @Test
    void transferOut(){
        sabaGameAdapter.transferIntoPlatform("allen", new BigDecimal(100), "c123456");
    }

    @Test
    void findTicketStatus(){
        sabaGameAdapter.findTicketStatus("allen", "c123456");
    }

    @Test
    void fetchBetOrder(){
        sabaGameAdapter.fetchBetOrder(0l);
    }

    @Test
    void doFetchBetOrderAction(){
        sabaGameAdapter.doFetchBetOrderAction();
    }
}