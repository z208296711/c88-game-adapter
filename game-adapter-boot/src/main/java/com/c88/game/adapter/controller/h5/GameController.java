package com.c88.game.adapter.controller.h5;

import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.redis.annotation.Limit;
import com.c88.common.redis.enums.LimitType;
import com.c88.common.web.exception.BizException;
import com.c88.common.web.util.MemberUtils;
import com.c88.game.adapter.constants.RedisKey;
import com.c88.game.adapter.dto.PlatformDTO;
import com.c88.game.adapter.enums.PlatformGameLabelEnum;
import com.c88.game.adapter.pojo.entity.MemberGameSession;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.entity.PlatformGame;
import com.c88.game.adapter.pojo.form.FindKeywordSearchForm;
import com.c88.game.adapter.pojo.form.FindPlatformGameLabelForm;
import com.c88.game.adapter.pojo.form.SyncBalanceToPlatformForm;
import com.c88.game.adapter.pojo.vo.GameCategoryVO;
import com.c88.game.adapter.pojo.vo.H5GameCategoryVO;
import com.c88.game.adapter.pojo.vo.PlatformGameLabelVO;
import com.c88.game.adapter.pojo.vo.PlatformRateVO;
import com.c88.game.adapter.service.GameService;
import com.c88.game.adapter.service.IBetOrderService;
import com.c88.game.adapter.service.IGameCategoryService;
import com.c88.game.adapter.service.IPlatformGameService;
import com.c88.game.adapter.service.IPlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@Tag(name = "遊戲相關")
@RequiredArgsConstructor
@RequestMapping("/h5/game")
public class GameController {

    private final GameService gameService;

    private final IPlatformGameService iPlatformGameService;

    private final IPlatformService iPlatformService;

    private final IBetOrderService iBetOrderService;

    private final IGameCategoryService iGameCategoryService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Operation(summary = "查詢遊戲場館費率", description = "倍數")
    @GetMapping("/platform/rate")
    public Result<List<PlatformRateVO>> findPlatformRate() {
        return Result.success(iPlatformService.findPlatformRate());
    }

    @Operation(summary = "開啟遊戲連結")
    @GetMapping("/login")
    public Result<String> login(@Parameter(description = "遊戲列表ID") Integer platformGameId, HttpServletRequest request) {
        PlatformGame platformGame = iPlatformGameService.getPlatformGameById(platformGameId);
        String username = MemberUtils.getUsername();
        Long memberId = MemberUtils.getMemberId();
        if (StringUtils.isBlank(username) || Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(gameService.login(memberId, username, platformGame, request));
    }

    @Operation(summary = "取得最後開啟遊戲記錄")
    @GetMapping("/lastSession/{uid}")
    public Result<String> lastMemberGameSession(@PathVariable Long uid) {
        MemberGameSession memberGameSession = gameService.getLastMemberGameSession(uid);
        return Result.success(memberGameSession != null ? memberGameSession.getIp() : null);
    }

    @Operation(summary = "取所有玩過的三方餘額取回會員錢包")
    @GetMapping("/findAllBalance")
    public Result<BigDecimal> findAllBalance() {
        Long memberId = MemberUtils.getMemberId();
        if (Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(getMemberBalance(memberId));
    }

    @Operation(summary = "刷新餘額")
    @GetMapping("/refreshAllBalance")
    @Limit(limitType = LimitType.BALANCE, prefix = "refreshAllBalance", period = 30, count = 1)
    public Result<BigDecimal> refreshAllBalance() {
        Long memberId = MemberUtils.getMemberId();
        if (Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        redisTemplate.opsForValue().set(RedisKey.MEMBER_BALANCE + ":" + memberId, String.valueOf(gameService.findAllBalance(memberId)), 30, TimeUnit.SECONDS);
        return Result.success(getMemberBalance(memberId));
    }

    private BigDecimal getMemberBalance(Long memberId) {
        BigDecimal balance;
        String balanceKey = RedisKey.MEMBER_BALANCE + ":" + memberId;
        String cacheBalance = (String) redisTemplate.opsForValue().get(balanceKey);
        if (StringUtils.isNotBlank(cacheBalance)) {
            balance = new BigDecimal(cacheBalance);
        } else {
            balance = gameService.findAllBalance(memberId);
            redisTemplate.opsForValue().set(balanceKey, String.valueOf(balance), 60, TimeUnit.SECONDS);
        }
        return balance;
    }

    @Operation(summary = "取所有玩過的三方餘額取回會員錢包", hidden = true)
    @GetMapping("/findAllBalance/{uid}/{username}")
    public Result<BigDecimal> findAllBalance(@PathVariable Long uid, @PathVariable String username) {
        if (StringUtils.isBlank(username) || Objects.isNull(uid)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        return Result.success(gameService.findAllBalance(uid));
    }

    @Operation(summary = "會員所有未結算下注總額", hidden = true)
    @GetMapping("/findMemberNonSettleBetAmount/{uid}")
    public Result<BigDecimal> findMemberNonSettleBetAmount(@PathVariable Long uid) {
        return Result.success(iBetOrderService.findMemberNonSettleBetAmount(uid));
    }

    @Operation(summary = "同步目前錢包至指定三方")
    @PostMapping("/syncBalanceToPlatform")
    @Limit(limitType = LimitType.PLATFORM_TRANSFER, prefix = "syncBalanceToPlatform", period = 5, count = 1)
    public Result<Boolean> syncBalanceToPlatform(@Validated @RequestBody SyncBalanceToPlatformForm form) {
        Long memberId = MemberUtils.getMemberId();
        String username = MemberUtils.getUsername();
        if (StringUtils.isBlank(username) || Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }

        Platform platform = iPlatformService.getPlatformById(form.getPlatformId());
        if (platform.getCanTransferIn() == 0) {
            return Result.success(false);
        }

        redisTemplate.opsForValue().set(RedisKey.MEMBER_BALANCE + ":" + memberId, String.valueOf(gameService.findAllBalance(memberId)), 30, TimeUnit.SECONDS);
        return Result.success(gameService.syncBalanceToPlatform(memberId, username, platform));
    }

    @Operation(summary = "取得遊戲列表，適用於棋牌與電子")
    @GetMapping("/platformGameLabel")
    public PageResult<PlatformGameLabelVO> findPlatformGameLabel(@ParameterObject @Validated FindPlatformGameLabelForm form) {
        Long memberId = MemberUtils.getMemberId();
        if (Objects.equals(form.getLabelType(), PlatformGameLabelEnum.RECENT.getCode()) && Objects.isNull(memberId)) {
            throw new BizException(ResultCode.USER_NOT_EXIST);
        }
        return PageResult.success(gameService.findPlatformGameLabel(memberId, form));
    }

    @Operation(summary = "取得遊戲類型列表與底下平台")
    @GetMapping("/gameCategory/platform")
    public Result<List<H5GameCategoryVO>> findGameCategoryPlatform() {
        return Result.success(gameService.findGameCategoryPlatform());
    }

    @Deprecated
    @Operation(summary = "取得關鍵字清單")
    @GetMapping("/complete/search")
    public Result<Set<String>> findCompleteSearch(@Parameter(description = "查詢") String q) {
        // 保留實作autocomplete
        return Result.success(gameService.findCompleteSearch(q));
    }

    @Operation(summary = "取得關鍵字遊戲")
    @GetMapping("/keyword/search")
    public PageResult<PlatformGameLabelVO> findKeywordSearch(@ParameterObject FindKeywordSearchForm form) {
        return PageResult.success(gameService.findKeywordSearch(form));
    }

    @Operation(summary = "取得遊戲平台")
    @GetMapping("/platform")
    public Result<List<PlatformDTO>> findPlatform() {
        return Result.success(iPlatformService.findAllPlatformDTO());
    }

    @Operation(summary = "取得遊戲種類")
    @GetMapping("/category")
    public Result<List<GameCategoryVO>> findCategory() {
        return Result.success(iGameCategoryService.findGameCategory());
    }

}
