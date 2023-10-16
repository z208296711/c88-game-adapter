package com.c88.game.adapter.service.third.adapter;

import com.c88.common.core.result.Result;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.Platform;
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
class MPGameAdapterTest {

    @Autowired
    private MPGameAdapter mpGameAdapter;
    ApiParameter apiParameter;

    @BeforeEach
    void init() {
        mpGameAdapter.init();
    }

    @Test
    void register() {

        Platform platform = Platform.builder().id(67l).apiParameter(apiParameter).build();
        mpGameAdapter.register(9000008l, "terryzhang", platform);
    }

    @Test
    void login() {
        Map map = new HashMap();
        map.put("GameId", "0");
        Result<String> urlRes = mpGameAdapter.login("terryzhang", map);
        log.info(urlRes.getData());
    }


    @Test
    void balance() {
        Result<BigDecimal> urlRes = mpGameAdapter.balance("terryzhang");
        log.info("balance: " + urlRes.getData());
    }

    @Test
    void transferIn() {
        Result<TransferStateVO> urlRes = mpGameAdapter.transferIntoThird("terryzhang", BigDecimal.valueOf(2000l), UUID.randomUUID().toString());
        System.out.println("transferIn:" + urlRes.getData());
    }

    @Test
    void transferOut() {
        Result<TransferStateVO> urlRes = mpGameAdapter.transferIntoPlatform("terryzhang", BigDecimal.valueOf(10l), UUID.randomUUID().toString());
        System.out.println("transferOut:" + urlRes.getData());
    }

    @Test
    void fetchBetOrder() {
        mpGameAdapter.fetchBetOrder(LocalDateTime.now().minusHours(1), LocalDateTime.now());

    }

    @Test
    void findTicketStatus() {
        mpGameAdapter.findTicketStatus("terryzhang", "30040620230312023403134devterryzhang");
    }

    @Test
    void doFetchBetOrderAction() {
        mpGameAdapter.doFetchBetOrderAction();

    }
}
