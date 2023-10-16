package com.c88.game.adapter.service.third.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.c88.common.core.result.Result;
import com.c88.common.core.util.GameUtil;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.enums.GetBetOrdersStatusEnum;
import com.c88.game.adapter.enums.PPBetInfoStatusEnum;
import com.c88.game.adapter.enums.ReBateStatusEnum;
import com.c88.game.adapter.pojo.entity.ApiParameter;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGameMember;
import com.c88.game.adapter.pojo.entity.TCBetOrderTime;
import com.c88.game.adapter.pojo.vo.TCGameBetInfoVO;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.ITCBetOrderTimeService;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.game.adapter.utils.FTPClientUtil;
import com.c88.game.adapter.vo.BetOrderVO;
import com.c88.kafka.producer.KafkaMessageProducer;
import com.c88.kafka.topics.KafkaTopicEnum;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.stream.JsonReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TCGameAdapter implements IGameAdapter {

    private final KafkaMessageProducer<BetOrderVO> kafkaMessageProducer;

    private final IPlatformGameMemberService iPlatformGameMemberService;

    private final ITCBetOrderTimeService itcBetOrderTimeService;

    private final RestTemplate restTemplate;

    private final IPlatformService iplatformService;

    private static final String PLATFORM_CODE = "TC";
    public static final Integer SUCCESS_CODE = 0;

    private static final String LOTTO_PATH = "/TLOTTO/SETTLED";
    private static final String VN_LOTTO_PATH = "/TCG_LOTTO_VN/TLOTTO/SETTLED";

    private String merchantCode;

    private String gameUrl;

    private String apiUrl;

    private String prefix;

    private String templateName;

    private String secretKey;

    private String ftpIp;

    private String ftpUserName;

    private String ftpPwd;

    private String shaKey;

    private String desKey;

    private String gameCode;

    private String lotteryBetMode;

    @PostConstruct
    public void init() {
        Platform platform = iplatformService.lambdaQuery()
                .eq(Platform::getCode, PLATFORM_CODE)
                .one();

        if (platform == null) {
            return;
        }

        ApiParameter apiParameter = platform.getApiParameter();
        this.secretKey = apiParameter.getApiKey();
        String[] keys = secretKey.split("\\^");
        if (keys.length >= 2) {
            desKey = keys[0];
            shaKey = keys[1];
        }
        String[] ftpX = apiParameter.getOthers().split("\\^");
        if (ftpX.length >= 5) {
            ftpIp = ftpX[0];
            ftpUserName = ftpX[1];
            ftpPwd = ftpX[2];
            gameCode = ftpX[3];
            lotteryBetMode = ftpX[4];
        }

        this.merchantCode = apiParameter.getApiId();
        this.gameUrl = apiParameter.getGameUrl();
        this.apiUrl = apiParameter.getApiUrl();
        this.templateName = "aliceblue";
        this.prefix = apiParameter.getPrefix();
    }

    @Override
    public String getUsername(String username) {
        return prefix + username;
    }

    @Override
    public String getGamePlatformCode() {
        return PLATFORM_CODE;
    }

    @Override
    public Result<String> login(String username, Map<String, String> param) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "lg");
        params.put("username", username);
        params.put("product_type", 2);
        params.put("platform", "html5-desktop");
        params.put("game_mode", "1");
        params.put("game_code", gameCode);
        params.put("lottery_bet_mode", lotteryBetMode);
        params.put("view", "Lobby");
        JSONObject jsonResult = getJsonResult(params);
        Integer code = jsonResult.getInteger("status");
        if (!SUCCESS_CODE.equals(code)) {
            log.info("TC login failed {}", jsonResult.getString("error_desc"));
            return Result.failed(jsonResult.getString("error_desc"));
        } else {
            log.info("TC login success {}", jsonResult.getString("game_url"));
            return Result.success(gameUrl + jsonResult.getString("game_url"));
        }
    }

    @Override
    public Result<String> register(Long memberId, String username, Platform platform) {

        Map<String, Object> paraX = new HashMap<>();
        paraX.put("method", "cm");
        paraX.put("username", username);
        paraX.put("password", username + "tc12");
        paraX.put("currency", "VND");
        log.info("TC register ");
        JSONObject jsonResult = getJsonResult(paraX);
        Integer code = jsonResult.getInteger("status");
        if (!SUCCESS_CODE.equals(code)) {
            return Result.failed(jsonResult.getString("error_desc"));
        }

        iPlatformGameMemberService.save(
                PlatformGameMember.builder()
                        .memberId(memberId)
                        .username(this.getUsername(username))
                        .platformId(platform.getId())
                        .code(PLATFORM_CODE)
                        .build()
        );

        return Result.success(username);
    }

    private JSONObject getJsonResult(Map<String, Object> paraX) {
        String param = toJSON(paraX);
        JSONObject jsonResult = null;
        try {
            jsonResult = getJsonObject(param);
        } catch (Exception e) {
            log.error(e.getMessage());
            new JSONObject();
        }
        return jsonResult;
    }

    private JSONObject getJsonObject(String params) throws Exception {
        String desParam = handleDes(params);

        MultiValueMap<String, Object> paraX = new LinkedMultiValueMap<>();
        paraX.add("merchant_code", this.merchantCode);
        paraX.add("params", desParam);
        paraX.add("sign", handleSHA256(desParam));
        log.info(" TC request merchant_code {} paras {} sign {}", this.merchantCode, params, handleSHA256(desParam));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(paraX, headers);
        String response = restTemplate.postForObject(this.apiUrl, request, String.class);
        return JSON.parseObject(response);
    }

    @Override
    public Result<BigDecimal> balance(String username) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "gb");
        params.put("username", username);
        params.put("product_type", 2);
        log.info("TC balance ");

        try{
            JSONObject jsonResult = getJsonResult(params);
            if (jsonResult == null) {
                return Result.success(BigDecimal.ZERO);
            }
            Integer code = jsonResult.getInteger("status");
            if (!SUCCESS_CODE.equals(code)) {
                return Result.failed(jsonResult.getString("error_desc"));
            } else {
                return Result.success(jsonResult.getBigDecimal("balance"));
            }
        }catch (Exception ex){
            return Result.failed(ex.getMessage());
        }
    }

    @Override
    public Result<TransferStateVO> transferIntoThird(String username, BigDecimal amount, String transactionNo) {
        log.info("TC transferIn ");
        return transfer(username, amount, transactionNo, 1);
    }

    @Override
    public Result<TransferStateVO> transferIntoPlatform(String username, BigDecimal amount, String transactionNo) {
        log.info("TC transferOut ");
        return transfer(username, amount, transactionNo, 2);
    }

    private Result<TransferStateVO> transfer(String username, BigDecimal amount, String transactionNo, int inOut) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "ft");
        params.put("username", username);
        params.put("product_type", 2);
        params.put("fund_type", inOut);
        params.put("amount", amount);
        params.put("reference_no", transactionNo);

        JSONObject jsonResult = getJsonResult(params);
        Integer code = jsonResult.getInteger("status");
        String tStatus = jsonResult.getString("transaction_status");
        TransferStateVO stateVO = new TransferStateVO();
        stateVO.setBalance(amount);
        if (SUCCESS_CODE.equals(code) && tStatus.equals("SUCCESS")) {
            stateVO.setState(AdapterTransferStateEnum.SUCCESS);
            return Result.success(stateVO);
        } else if (tStatus.equals("PENDING")) {
            stateVO.setState(AdapterTransferStateEnum.IN_PROGRESS);
            return Result.success(stateVO);
        } else {
            return Result.failed(jsonResult.getString("error_desc"));
        }
    }

    @Override
    public Result<String> findTicketStatus(String username, String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", "cs");
        params.put("product_type", 2);
        params.put("reference_no", orderId);
        log.info("TC findTicketStatus ");
        JSONObject jsonResult = getJsonResult(params);
        Integer code = jsonResult.getInteger("status");
        String tStatus = jsonResult.getString("transaction_status");

        if (SUCCESS_CODE.equals(code) && tStatus.equals("SUCCESS")) {
            return Result.success(tStatus);
        }
        return Result.failed(jsonResult.getJSONObject("transaction_details").toString());
    }

    @Override
    public void manualBetOrder(LocalDateTime startDateTime, LocalDateTime endDateTime, Map<String, String> param) {
        doFetchOrderInfo(startDateTime);
    }

    @Override
    public void doFetchBetOrderAction() {
        TCBetOrderTime lastExecutor = itcBetOrderTimeService.getLastVersion();
        lastExecutor = Optional.ofNullable(lastExecutor)
                .orElseGet(() -> {
                    TCBetOrderTime tc = new TCBetOrderTime();
                    tc.setStartTime(LocalDateTime.now().minusMinutes(30));
                    return tc;
                });
        try {
            doFetchOrderInfo(lastExecutor.getStartTime());
            lastExecutor.setEndTime(LocalDateTime.now());
            lastExecutor.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_DONE.getValue());
        } catch (Exception e) {
            log.error("TC doFetchBetOrder exception occur {}", e);
            lastExecutor.setStatus(GetBetOrdersStatusEnum.VERSION_STATUS_ERROR.getValue());

        } finally {
            itcBetOrderTimeService.saveOrUpdate(lastExecutor);
        }
    }

    @Override
    public <T> List<T> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime) {
        return null;
    }

    @Override
    public <T> List<T> fetchBetOrderVersion(Long Version) {
        return null;
    }

    public String getHashKey(String url, String gameKey) {
        return GameUtil.getMD5(url + gameKey);
    }

    private String handleDes(String content) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

        DESKeySpec desKeySpec = new DESKeySpec(desKey.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secureKey = keyFactory.generateSecret(desKeySpec);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, secureKey);
        byte[] result = cipher.doFinal(content.getBytes("utf-8"));
        byte[] base64 = Base64.getEncoder().encode(result);
        return new String(base64);
    }

    private String handleSHA256(String content) {
        return DigestUtils.sha256Hex(content + shaKey);
    }

    private String toJSON(Map<String, Object> in) {
        Gson gObj = new Gson();
        return gObj.toJson(in);
    }

    private void doFetchOrderInfo(LocalDateTime startTime) {
        log.info(" doFetchOrderInfo ftpIp {} ftp {} ftpPwd {} startTime {}", ftpIp, ftpUserName, ftpPwd, startTime);
        try (FTPClientUtil ftpClient = new FTPClientUtil(ftpIp, ftpUserName, ftpPwd)) {

            getGameRounds(startTime, ftpClient, LOTTO_PATH)
                    .stream()
                    .forEach(x -> Optional.ofNullable(toOrder(x))
                            .ifPresent(y -> kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), y)));

            getGameRounds(startTime, ftpClient, VN_LOTTO_PATH)
                    .stream()
                    .forEach(x -> Optional.ofNullable(toOrder(x))
                            .ifPresent(y -> kafkaMessageProducer.sendMessage(KafkaTopicEnum.SAVE_BET_ORDER.getTopic(), y)));
        } catch (IOException e) {
            log.error("TC fetchOrder occurs ftp problem {} ", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //
    private List<TCGameBetInfoVO> getGameRounds(LocalDateTime triggerTime, FTPClientUtil ftpClient, String ftpPath) throws IOException {
        String fileName = toFileName(triggerTime);

        return toTCGameInfoList(ftpClient.downLoad(ftpPath + "/" + fileName));
    }

    private BetOrderVO toOrder(TCGameBetInfoVO betInfo) {
        PlatformGameMember platformGameMember = iPlatformGameMemberService.findByPlatformAndUsername(PLATFORM_CODE, betInfo.getUserName());
        if (Objects.isNull(platformGameMember)) return null;
        BetOrderVO betOrder = new BetOrderVO();
        betOrder.setTransactionNo(String.valueOf(betInfo.getOrderMasterId()));
        betOrder.setTransactionSerial(betInfo.getOrderNum());
        betOrder.setTransactionTime(betInfo.getBettingTime());
        betOrder.setMemberId(platformGameMember.getMemberId());
        betOrder.setUsername(platformGameMember.getUsername().substring(3));
        betOrder.setPlatformId(platformGameMember.getPlatformId());
        betOrder.setGameId(betInfo.getGameCode());
        betOrder.setGameName(betInfo.getGameCode());
        betOrder.setGameCategoryCode("lottery");
        betOrder.setValidBetAmount(BigDecimal.ZERO);
        betOrder.setBetAmount(betInfo.getBetAmount());
        betOrder.setSettle(betInfo.getWinAmount());
        betOrder.setSettleTime(betInfo.getDrawTime());
        betOrder.setBetState(PPBetInfoStatusEnum.BET_STATUS_SETTLED.getValue());

        betOrder.setWinLoss(betInfo.getWinAmount());
        betOrder.setRebateState(ReBateStatusEnum.UN_SETTLED.getValue());
        betOrder.setPlatformCode(PLATFORM_CODE);

        return betOrder;
    }

    private String toFileName(LocalDateTime time) {
        int mod = time.getMinute() % 5;
        LocalDateTime fileNameTime = time.minusMinutes(mod);
        return fileNameTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + "_0001.json";
    }

    private String toReferenceNo() {
        LocalDateTime refTime = LocalDateTime.now();
        int r = (int) (Math.random() * (60 - 0 + 1)) + 1;
        return refTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + r;
    }

    private List<TCGameBetInfoVO> toTCGameInfoList(InputStream is) {
        List<TCGameBetInfoVO> retVOX = List.of();
        try (
                JsonReader reader = new JsonReader(new InputStreamReader(is));

        ) {
            Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> {

                try {
                    return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (DateTimeParseException e) {
                    return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
                }

            }).create();
            reader.beginObject();
            while (reader.hasNext()) {
                String token = reader.nextName();
                if (token.equals("list")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        TCGameBetInfoVO vo = gson.fromJson(reader, TCGameBetInfoVO.class);
                        retVOX.add(vo);
                    }
                }
            }
            return retVOX;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
