package com.c88.game.adapter.service.third.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.service.third.v8.V8GameAdapter;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@SpringBootTest(properties = "spring.profiles.active:local")
class V8GameAdapterTest {
    @Autowired
    private V8GameAdapter v8GameAdapter;

    ApiParameter apiParameter;

    @BeforeEach
    void init() {
        v8GameAdapter.init();
    }

    @Test
    void register() {
        String apiParam = "{\"apiId\": \"70635\", \"apiKey\": \"EADA1341FD2A57AD\", \"apiUrl\": \"https://wc2-api.twow42.com/channelHandle\", \"others\": \"93FA416A7EC9A2A1\", \"prefix\": \"dev\", \"gameUrl\": \"\"}";
        JSONObject jsonObject = JSON.parseObject(apiParam);
        ApiParameter apiParameter = new ApiParameter();
        apiParameter.setApiId(jsonObject.getString("apiId"));
        apiParameter.setApiUrl(jsonObject.getString("apiUrl"));
        apiParameter.setApiKey(jsonObject.getString("apiKey"));
        apiParameter.setOthers(jsonObject.getString("others"));

        Platform platform = Platform.builder().id(21l).apiParameter(apiParameter).build();
        v8GameAdapter.register(117l, "garytest", platform);
    }

    @Test
    void login() {
        Map map = new HashMap();
        map.put("GameId", "0");
        Result<String> urlRes = v8GameAdapter.login("garytest", map);
        log.info(urlRes.getData());
    }


    @Test
    void balance() {
        Result<BigDecimal> urlRes = v8GameAdapter.balance("garytest");
        log.info("balance: " + urlRes.getData());
    }

    @Test
    void transferIn() {
        Result<TransferStateVO> urlRes = v8GameAdapter.transferIntoThird("garytest", BigDecimal.valueOf(1000000l), UUID.randomUUID().toString());
        System.out.println("transferIn:" + urlRes.getData());
    }

    @Test
    void transferOut() {
        Result<TransferStateVO> urlRes = v8GameAdapter.transferIntoPlatform("garytest", BigDecimal.valueOf(1000l), UUID.randomUUID().toString());
        System.out.println("transferOut:" + urlRes.getData());
    }

    @Test
    void fetchBetOrder() {
        v8GameAdapter.fetchBetOrder(LocalDateTime.now().minusHours(1), LocalDateTime.now());

    }

    @Test
    void doFetchBetOrderAction() {
        v8GameAdapter.doFetchBetOrderAction();

    }
}