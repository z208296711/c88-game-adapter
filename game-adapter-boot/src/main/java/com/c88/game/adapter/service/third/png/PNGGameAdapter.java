// package com.c88.game.adapter.service.third.png;
//
// import com.alibaba.fastjson.JSONArray;
// import com.alibaba.fastjson.JSONObject;
// import com.c88.common.core.enums.EnableEnum;
// import com.c88.common.core.result.Result;
// import com.c88.common.redis.utils.RedisUtils;
// import com.c88.game.adapter.BalanceResponse;
// import com.c88.game.adapter.CreditAccountResponse;
// import com.c88.game.adapter.DebitAccountResponse;
// import com.c88.game.adapter.GetTicketResponse;
// import com.c88.game.adapter.enums.AdapterTransferStateEnum;
// import com.c88.game.adapter.enums.BetOrderTypeEnum;
// import com.c88.game.adapter.pojo.entity.ApiParameter;
// import com.c88.game.adapter.pojo.entity.Platform;
// import com.c88.game.adapter.pojo.entity.PlatformGame;
// import com.c88.game.adapter.pojo.entity.PlatformGameMember;
// import com.c88.game.adapter.service.IPlatformGameMemberService;
// import com.c88.game.adapter.service.IPlatformGameService;
// import com.c88.game.adapter.service.IPlatformService;
// import com.c88.game.adapter.service.third.adapter.IGameAdapter;
// import com.c88.game.adapter.service.third.vo.TransferStateVO;
// import com.c88.game.adapter.vo.BetOrderVO;
// import com.c88.kafka.producer.KafkaMessageProducer;
// import com.c88.kafka.topics.KafkaTopicEnum;
// import io.fabric8.kubernetes.client.dsl.Execable;
// import lombok.RequiredArgsConstructor;
// import lombok.SneakyThrows;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;
//
// import javax.annotation.PostConstruct;
// import javax.xml.bind.JAXBContext;
// import javax.xml.bind.JAXBException;
// import javax.xml.bind.Unmarshaller;
// import java.io.StringReader;
// import java.math.BigDecimal;
// import java.net.URI;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.Base64;
// import java.util.List;
// import java.util.Map;
// import java.util.Objects;
//
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class PNGGameAdapter implements IGameAdapter {
//
//     private final IPlatformGameMemberService iPlatformGameMemberService;
//
//     private final IPlatformService iPlatformService;
//
//     private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;
//
//     private final IPlatformGameService iPlatformGameService;
//
//     private final RedisTemplate<String, Object> redisTemplate;
//
//     private String prefix;
//     private String apiId;
//     private String apiKey;
//     private String apiUrl;
//     private String recordUrl;
//     private String locale;
//
//     private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//     private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
//
//     @PostConstruct
//     public void init() {
//         Platform platform = iPlatformService.lambdaQuery()
//                 .eq(Platform::getCode, "PNG")
//                 .oneOpt()
//                 .orElse(Platform.builder().code("PNG").enable(EnableEnum.STOP.getCode()).apiParameter(new ApiParameter()).build());
//
//         ApiParameter apiParameter = platform.getApiParameter();
//         this.apiId = apiParameter.getApiId();
//         this.apiKey = apiParameter.getApiKey();
//         this.apiUrl = apiParameter.getApiUrl();
//         this.recordUrl = apiParameter.getRecordUrl();
//         this.prefix = apiParameter.getPrefix();
//         this.locale = apiParameter.getLocale();
//     }
//
//     @Override
//     public String getUsername(String username) {
//         return prefix + username;
//     }
//
//     @Override
//     public String getGamePlatformCode() {
//         return "PNG";
//     }
//
//     @Override
//     public Result<String> login(String username, Map<String, String> param) {
//         String soapAction = "http://playngo.com/v1/CasinoGameService/GetTicket";
//
//         GetTicketReq request = new GetTicketReq();
//         request.setExternalUserId(this.getUsername(username));
//         HttpResponse<String> response = this.doSoapPost(apiUrl, soapAction, request.toXml());
//         if (response.statusCode() != 200) {
//             return Result.failed(response.toString());
//         }
//         GetTicketResponse getTicketResponse = (GetTicketResponse) jaxbXMLToObject(response.body(), GetTicketResponse.class);
//
//         StringBuilder sb = new StringBuilder();
//         sb.append("https://asistage.playngonetwork.com/casino/ContainerLauncher");
//         sb.append("?pid=").append(1323);
//         sb.append("&gid=").append(param.get("GameId"));//代填
//         sb.append("&channel=").append("mobile");
//         sb.append("&lang=").append("vi_VN");
//         sb.append("&practice=").append(0);
//         sb.append("&ticket=").append(getTicketResponse.getTicket().getValue());
//         sb.append("&origin=").append("https://dev-c88-frontsite.hyu.tw");
//         return Result.success(sb.toString());
//     }
//
//     @Override
//     public Result<String> register(Long memberId, String username, Platform platform) {
//         RegisterUserReq req = new RegisterUserReq();
//         req.setExternalUserId(this.getUsername(username));
//         req.setUserName(username);
//         req.setNickName(username);
//         req.setBrandId("C88");
//         req.setCurrency("VND");
//         req.setRegistration(LocalDateTime.now().format(formatter));
//         req.setLanguage("vi_VN");
//         req.setCountry("VN");
//         req.setBirthDate("1991-07-07");
//         req.setGender("m");
//         req.setLocked("false");
//         req.setIp("127.0.0.1");
//         String soapAction = "http://playngo.com/v1/CasinoGameService/RegisterUser";
//         HttpResponse<String> response = this.doSoapPost(apiUrl, soapAction, req.toXml());
//
// //        if (response.statusCode() != 200) {
// //            return Result.failed(response.toString());
// //        }
// //        RegisterUserResponse registerUserResponse = (RegisterUserResponse) jaxbXMLToObject(response.body(), RegisterUserResponse.class);
// //        log.info("PNG REGISTER:{}", JSON.toJSONString(registerUserResponse));
//
//         iPlatformGameMemberService.save(
//                 PlatformGameMember.builder()
//                         .memberId(memberId)
//                         .username(this.getUsername(username))
//                         .platformId(platform.getId())
//                         .code(platform.getCode())
//                         .build()
//         );
//         return Result.success(this.getUsername(username));
//     }
//
//     @Override
//     public Result<BigDecimal> balance(String username) {
//         BalanceReq req = new BalanceReq();
//         req.setExternalUserId(this.getUsername(username));
//         String soapAction = "http://playngo.com/v1/CasinoGameService/Balance";
//
//         try {
//             HttpResponse<String> response = doSoapPost(this.apiUrl, soapAction, req.toXml());
//             if (response.statusCode() != 200) {
//                 return Result.failed(response.toString());
//             }
//             BalanceResponse balanceResponse = (BalanceResponse) jaxbXMLToObject(response.body(), BalanceResponse.class);
//             return Result.success(Objects.requireNonNull(balanceResponse).getUserBalance().getValue().getReal());
//         }catch (Exception ex){
//             return Result.failed(ex.getMessage());
//         }
//     }
//
//     @Override
//     public Result<TransferStateVO> transferIn(String username, BigDecimal amount, String transactionNo) {
//
//         CreditAccountReq req = new CreditAccountReq();
//         req.setExternalUserId(this.getUsername(username));
//         req.setAmount(amount.toPlainString());
//         req.setExternalTransactionId(transactionNo);
//
//         String soapAction = "http://playngo.com/v1/CasinoGameService/CreditAccount";
//         HttpResponse<String> response = doSoapPost(this.apiUrl, soapAction, req.toXml());
//         if (response.statusCode() != 200) {
//             return Result.failed(response.toString());
//         }
//         CreditAccountResponse creditAccountResponse = (CreditAccountResponse) jaxbXMLToObject(response.body(), CreditAccountResponse.class);
//
//         TransferStateVO stateVO = new TransferStateVO();
//         stateVO.setBalance(creditAccountResponse.getUserAccount().getValue().getReal());
//         stateVO.setState(AdapterTransferStateEnum.SUCCESS);
//         return Result.success(stateVO);
//
//     }
//
//     @Override
//     public Result<TransferStateVO> transferOut(String username, BigDecimal amount, String transactionNo) {
//         DebitAccountReq req = new DebitAccountReq();
//         req.setExternalUserId(this.getUsername(username));
//         req.setAmount(amount.toPlainString());
//         req.setExternalTransactionId(transactionNo);
//
//         String soapAction = "http://playngo.com/v1/CasinoGameService/DebitAccount";
//         HttpResponse<String> response = this.doSoapPost(this.apiUrl, soapAction, req.toXml());
//         if (response.statusCode() != 200) {
//             return Result.failed(response.toString());
//         }
//         DebitAccountResponse debitAccountResponse = (DebitAccountResponse) jaxbXMLToObject(response.body(), CreditAccountResponse.class);
//
//         TransferStateVO stateVO = new TransferStateVO();
//         stateVO.setBalance(debitAccountResponse.getUserAccount().getValue().getReal());
//         stateVO.setState(AdapterTransferStateEnum.SUCCESS);
//         return Result.success(stateVO);
//     }
//
//     @Override
//     public Result<String> findTicketStatus(String username, String orderId) {
//         return null;
//     }
//
//     @Override
//     public void manualBetOrder(LocalDateTime startDateTime, LocalDateTime endDateTime, Map<String, String> param) {
//
//     }
//
//     @Override
//     public void doFetchBetOrderAction() {
//
//     }
//
//     @Override
//     public <T> List<T> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
//         return null;
//     }
//
//     @SneakyThrows
//     private HttpResponse<String> doSoapPost(String url, String soapAction, String message) {
//         String userpassword = this.apiId + ":" + this.apiKey;
//         String encodedAuthorization = Base64.getEncoder().encodeToString(userpassword.getBytes());
//
//         HttpRequest request = HttpRequest.newBuilder()
//                 .POST(HttpRequest.BodyPublishers.ofString(message))
//                 .uri(URI.create(url))
//                 .header("Content-Type", "text/xml")
//                 .header("SOAPAction", soapAction)
//                 .header("Authorization", "Basic " + encodedAuthorization)
//                 .build();
//
//         HttpClient httpClient = HttpClient.newBuilder()
//                 .version(HttpClient.Version.HTTP_2)
//                 .build();
//         log.info("doSoapPost request:{}, soapAction:{} ,req:{}", request.uri().toString(), soapAction, message);
//         HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//         log.info("doSoapPost response:{}", response.body());
//         return response;
//     }
//
//     private static Object jaxbXMLToObject(String xml, Class clazz) {
//         JAXBContext jaxbContext;
//         try {
//             jaxbContext = JAXBContext.newInstance(clazz);
//             Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//             xml = xml.replace("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body>", "");
//             xml = xml.replace("</s:Body></s:Envelope>", "");
//             return jaxbUnmarshaller.unmarshal(new StringReader(xml));
//         } catch (JAXBException e) {
//             e.printStackTrace();
//             return null;
//         }
//     }
//
//     @Transactional
//     public void processingPNGMessage(JSONObject jsonObject) {
//         log.info("Msg:{}", jsonObject.toJSONString());
//
//         JSONArray jsonArray = jsonObject.getJSONArray("Messages");
//         for (int i = 0; i < jsonArray.size(); i++) {
//
//             JSONObject betInfo = jsonArray.getJSONObject(i);
//             if (betInfo.getInteger("MessageType") != 4) {
//                 continue;
//             }
//             PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(this.getGamePlatformCode(), betInfo.getString("ExternalUserId"));
//             if (platformGameMember == null) {
//                 continue;
//             }
//
//             BetOrderVO betOrder = new BetOrderVO();
//             betOrder.setTransactionNo("" + betInfo.getInteger("GameId") + betInfo.getInteger("RoundId") + betInfo.getInteger("GamesessionId"));
//             betOrder.setTransactionSerial("" + betInfo.getInteger("GameId") + betInfo.getInteger("RoundId") + betInfo.getInteger("GamesessionId"));
//             betOrder.setTransactionTime(LocalDateTime.parse(betInfo.getString("Time"), dateTimeFormatter));
//             betOrder.setMemberId(platformGameMember.getMemberId());
//             betOrder.setUsername(this.getOriginUsername(platformGameMember.getUsername()));
//             betOrder.setPlatformId(platformGameMember.getPlatformId());
//             betOrder.setPlatformCode(platformGameMember.getCode());
//
//             String gameId = betInfo.getString("GameId");
//             String gameIdKey = RedisUtils.buildKey("PNG", gameId);
//             String gameIdName = (String) redisTemplate.opsForValue().get(gameIdKey);
//             if (Objects.isNull(gameIdName)) {
//                 PlatformGame platformGame = iPlatformGameService.lambdaQuery()
//                         .eq(PlatformGame::getPlatformName, "PNG")
//                         .eq(PlatformGame::getExtendField, gameId)
//                         .oneOpt()
//                         .orElse(PlatformGame.builder().gameId(gameId).build());
//
//                 redisTemplate.opsForValue().set(gameIdKey, platformGame.getGameId());
//                 gameIdName = platformGame.getGameId();
//             }
//
//             betOrder.setGameId(gameId);
//             betOrder.setGameName(gameIdName);
//             betOrder.setGameCategoryCode("slot");
//
//             BigDecimal validBet = betInfo.getBigDecimal("RoundLoss");
//             BigDecimal settle = betInfo.getBigDecimal("Amount");
//             betOrder.setValidBetAmount(validBet);
//             betOrder.setBetAmount(validBet);
//             betOrder.setWinLoss(settle.subtract(validBet));
//             betOrder.setSettle(settle);
//             betOrder.setSettleTime(LocalDateTime.parse(betInfo.getString("Time"), dateTimeFormatter));
//             betOrder.setBetState(BetOrderTypeEnum.BET_STATUS_SETTLED.getValue());
//             betOrder.setRebateState(0);
//
//             //發送Mq
//             kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), betOrder);
//         }
//     }
//
//     private String getOriginUsername(String username) {
//         return username.substring(this.prefix.length());
//     }
// }
