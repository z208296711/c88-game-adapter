package com.c88.game.adapter.service.third.adapter;

import com.c88.common.core.result.Result;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.service.third.vo.TransferStateVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IGameAdapter {

    String getUsername(String username);

    String getGamePlatformCode();

    /**
     * 登入
     *
     * @param username 玩家帳號
     * @param param    其他參數
     */
    Result<String> login(String username, Map<String, String> param);

    /**
     * 創建用戶
     *
     * @param username 玩家帳號
     * @return 名稱
     */
    Result<String> register(Long memberId,
                            String username,
                            Platform platform);

    /**
     * 查詢用戶餘額
     */
    Result<BigDecimal> balance(String username);

    /**
     * 轉入三方
     */
    Result<TransferStateVO> transferIntoThird(String username,
                                              BigDecimal amount,
                                              String transactionNo);

    /**
     * 轉入平台
     */
    Result<TransferStateVO> transferIntoPlatform(String username,
                                                 BigDecimal amount,
                                                 String transactionNo);

    /**
     * 查询存取款交易状态
     */
    Result<String> findTicketStatus(String username, String orderId);

    /**
     * 手動補單
     */
    void manualBetOrder(LocalDateTime startDateTime,
                        LocalDateTime endDateTime,
                        Map<String, String> param);

    /**
     * 拉取注單
     */
    void doFetchBetOrderAction();

    <T> List<T> fetchBetOrder(LocalDateTime startTime, LocalDateTime endTime);

    <T> List<T> fetchBetOrderVersion(Long Version);
}
