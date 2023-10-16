package com.c88.game.adapter;

import com.c88.game.adapter.repository.IBetOrderRepository;
import com.c88.game.adapter.service.IBetOrderService;
import com.c88.game.adapter.service.ICmdVersionsService;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.third.GameAdapterExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(properties = "spring.profiles.active:local")
class GameAdapterExecutorTests {

    @Autowired
    private GameAdapterExecutor gameAdapterExecutor;
    @MockBean
    private IPlatformGameMemberService iPlatformGameMemberService;
    @MockBean
    private RestTemplate restTemplate;
    @MockBean
    private RedisTemplate<String, String> redisTemplate;
    @MockBean
    private IPlatformService iplatformService;
    @MockBean
    private ICmdVersionsService iCmdVersionsService;
    @MockBean
    private IBetOrderService iBetOrderService;
    @MockBean
    private IBetOrderRepository iBetOrderRepository;

    @Test
    void contextLoads() {
//        CMDGameAdapter gameAdapter = new CMDGameAdapter(iPlatformGameMemberService, restTemplate, redisTemplate, iplatformService, iCmdVersionsService,  iBetOrderService, iBetOrderRepository);
//        Mockito.doReturn(gameAdapter).when(gameAdapterExecutor).findByGamePlatFormByCode("CMD");
//        gameAdapter.doFetchBetOrderAction();
//        Assert.isNull(gameAdapter);
    }

}
