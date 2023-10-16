// package com.c88.game.adapter.service.third.adapter;
//
// import com.alibaba.fastjson.JSON;
// import com.alibaba.fastjson.JSONObject;
// import com.c88.common.core.result.Result;
// import com.c88.game.adapter.pojo.entity.ApiParameter;
// import com.c88.game.adapter.pojo.entity.Platform;
// import com.c88.game.adapter.service.third.png.PNGGameAdapter;
// import com.c88.game.adapter.service.third.vo.TransferStateVO;
// import lombok.extern.slf4j.Slf4j;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
//
// import java.math.BigDecimal;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.UUID;
//
// @Slf4j
// @SpringBootTest(properties = "spring.profiles.active:local")
// class PNGGameAdapterTest {
//     @Autowired
//     private PNGGameAdapter pngGameAdapter;
//
//     ApiParameter apiParameter;
//
//     @BeforeEach
//     void init() {
//         pngGameAdapter.init();
//     }
//
//     @Test
//     void register() {
//         String apiParam = "{\"apiId\": \"\", \"apiKey\": \"\", \"apiUrl\": \"https://asistage.playngonetwork.com\", \"others\": \"C88BET\", \"prefix\": \"dev\", \"gameUrl\": \"\"}";
//         JSONObject jsonObject = JSON.parseObject(apiParam);
//         ApiParameter apiParameter = new ApiParameter();
//         apiParameter.setApiId(jsonObject.getString("apiId"));
//         apiParameter.setApiUrl(jsonObject.getString("apiUrl"));
//         apiParameter.setOthers(jsonObject.getString("others"));
//
//         Platform platform = Platform.builder().id(62l)
//                 .code("PNG").apiParameter(apiParameter).build();
//         pngGameAdapter.register(117l, "garytest", platform);
//     }
//
//     @Test
//     void login() {
//         Map map = new HashMap();
//         map.put("GameId", "twentyfourkdragonmobile");
//         Result<String> urlRes = pngGameAdapter.login("garytest", map);
//         log.info(urlRes.getData());
//     }
//
//
//     @Test
//     void balance() {
//         Result<BigDecimal> urlRes = pngGameAdapter.balance("garytest");
//         System.out.println("balance: " + urlRes.getData());
//     }
//
//     @Test
//     void transferIn() {
//         Result<TransferStateVO> urlRes = pngGameAdapter.transferIn("garytest", BigDecimal.valueOf(1000000l), UUID.randomUUID().toString());
//         System.out.println("transferIn:" + urlRes.getData());
//     }
//
//     @Test
//     void transferOut() {
//         Result<TransferStateVO> urlRes = pngGameAdapter.transferOut("garytest", BigDecimal.valueOf(1000l), UUID.randomUUID().toString());
//         System.out.println("transferOut:" + urlRes.getData());
//     }
// }