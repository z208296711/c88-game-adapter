package com.c88.game.adapter.service.third.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.Platform;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: TODO
 * @author: marcoyang
 * @date: 2022/12/21
 **/
@SpringBootTest(properties = "spring.profiles.active:local")
public class PPGameAdapterTest {
    @Autowired
    private PPGameAdapter ppGameAdapter;

    @Autowired
    private TCGameAdapter tcGameAdapter;

    @Test
    void register() {
        String apiParam = "{\"apiId\": \"umd7fv90pl\", \"apiKey\": \"\", \"apiUrl\": \"http://v6h9tsa.bw6688.com\", \"others\": \"C88bet\", \"prefix\": \"dev\", \"recordUrl\": \"\"}";
        JSONObject jsonObject = JSON.parseObject(apiParam);
        ApiParameter apiParameter = new ApiParameter();
        apiParameter.setApiId(jsonObject.getString("apiId"));
        apiParameter.setApiUrl(jsonObject.getString("apiUrl"));
        apiParameter.setOthers(jsonObject.getString("others"));

//        Platform platform = Platform.builder().id(24L).apiParameter(apiParameter).build();
//        JSONObject jObject  = ppGameAdapter.gameList();
        //ppGameAdapter.register(118l, "marco258", platform);
        Map<String,String> m =  new HashMap<>();
        m.put("GameId","vs20starlight");
        System.out.println(ppGameAdapter.login("marco258",m).getData());
    }

    @Test
    void login(){
        Map map = new HashMap();
//        map.put("vendor_id", apiParameter.getApiId());
        map.put("gameplat", 1);
        map.put("vendor_member_id", "allen");
        ppGameAdapter.balance("marco258");
        ppGameAdapter.transferIntoThird("marco258", BigDecimal.valueOf(1000),"A123456703");
        ppGameAdapter.transferIntoPlatform("marco258", BigDecimal.valueOf(3000),"A123456704");
        ppGameAdapter.balance("marco258");
        //ppGameAdapter.login("marco369", map);
    }

    @Test
    void reader(){
        ApiParameter apiParameter = new ApiParameter();
        Platform platform = Platform.builder().id(28L).apiParameter(apiParameter).build();
//        tcGameAdapter.register(123456L,"marco147",platform);
        Map<String,String> m = new HashMap<>();
        m.put("GameId","2717");
        System.out.println(tcGameAdapter.login("marco147",m));
    }


}
