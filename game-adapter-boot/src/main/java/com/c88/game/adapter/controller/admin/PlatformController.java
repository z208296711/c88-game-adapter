package com.c88.game.adapter.controller.admin;

import com.c88.common.core.base.BasePageQuery;
import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.common.web.util.UUIDUtils;
import com.c88.game.adapter.dto.PlatformDTO;
import com.c88.game.adapter.enums.AdapterTransferStateEnum;
import com.c88.game.adapter.pojo.entity.Platform;
import com.c88.game.adapter.pojo.form.ModifyPlatformForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformGameSortForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformSortForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformSortHotGameForm;
import com.c88.game.adapter.pojo.form.ModifyPlatformSortTopBottomForm;
import com.c88.game.adapter.pojo.vo.CapitalSummaryCenterVO;
import com.c88.game.adapter.pojo.vo.PlatformRateVO;
import com.c88.game.adapter.pojo.vo.PlatformSortVO;
import com.c88.game.adapter.pojo.vo.PlatformVO;
import com.c88.game.adapter.service.IGameCategoryService;
import com.c88.game.adapter.service.IPlatformService;
import com.c88.game.adapter.service.third.GameAdapterExecutor;
import com.c88.game.adapter.service.third.adapter.IGameAdapter;
import com.c88.game.adapter.service.third.vo.TransferStateVO;
import com.c88.member.api.MemberFeignClient;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.member.vo.OptionVO;
import com.c88.payment.client.PaymentClient;
import com.c88.payment.dto.AddBalanceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.c88.common.core.enums.BalanceChangeTypeLinkEnum.TRANSFER_INTO_PLATFORM;

@RestController
@Tag(name = "遊戲平台")
@RequiredArgsConstructor
@RequestMapping("/api/v1/platform")
public class PlatformController {

    private final IPlatformService iPlatformService;

    private final GameAdapterExecutor gameAdapterExecutor;

    private final MemberFeignClient memberFeignClient;

    private final PaymentClient paymentClient;
    
    private final IGameCategoryService iGameCategoryService;

    @Operation(summary = "查詢遊戲平台")
    @GetMapping
    public PageResult<PlatformVO> findPlatform(@ParameterObject BasePageQuery form) {
        return PageResult.success(iPlatformService.findPlatform(form));
    }

    @Operation(summary = "查詢全部遊戲平台")
    @GetMapping("/all/dto")
    public Result<List<PlatformDTO>> findAllPlatformDTO() {
        return Result.success(iPlatformService.findAllPlatformDTO());
    }

    @Operation(summary = "查詢平台選單")
    @GetMapping("/option")
    public Result<List<OptionVO<Long>>> findPlatformOption() {
        return Result.success(iPlatformService.findPlatformOption());
    }

    @Operation(summary = "查詢平台選單")
    @GetMapping("/code/option")
    public Result<List<OptionVO<String>>> findPlatformCodeOption() {
        return Result.success(iPlatformService.findPlatformCodeOption());
    }

    @Operation(summary = "查詢單一平台類型")
    @GetMapping("/code/category/option/{id}")
    public Result<List<OptionVO>> findPlatformCategoryOption(@PathVariable("id") int platformId) {
        return Result.success(iGameCategoryService.getGameCategoryByGame(platformId));
    }

    @Operation(summary = "查詢遊戲場館費率", description = "倍數")
    @GetMapping("/rate")
    public Result<List<PlatformRateVO>> findPlatformRate() {
        return Result.success(iPlatformService.findPlatformRate());
    }

    @Operation(summary = "修改遊戲平台場館費率")
    @PutMapping("/rate")
    public Result<Boolean> modifyPlatform(@RequestBody Map<Long, BigDecimal> map) {
        return Result.success(iPlatformService.modifyPlatformRate(map));
    }

    @Operation(summary = "修改遊戲平台")
    @PutMapping
    public Result<Boolean> modifyPlatform(@Valid @RequestBody ModifyPlatformForm form) {
        return Result.success(iPlatformService.modifyPlatform(form));
    }

    @Operation(summary = "查詢平台排序內容")
    @GetMapping("/sort/{gameCategoryId}")
    public Result<List<PlatformSortVO>> findPlatformSort(@PathVariable("gameCategoryId") Integer gameCategoryId) {
        return Result.success(iPlatformService.findPlatformSort(gameCategoryId));
    }

    @Operation(summary = "查詢平台排序內容", description = "熱門")
    @GetMapping("/sort/hot")
    public Result<List<PlatformSortVO>> findPlatformSortByHot() {
        return Result.success(iPlatformService.findPlatformSortByHot());
    }

    @Operation(summary = "修改遊戲平台熱門遊戲")
    @PutMapping("/sort/hot/game")
    public Result<Boolean> modifyPlatformSortHotGame(@Valid @RequestBody ModifyPlatformSortHotGameForm form) {
        return Result.success(iPlatformService.modifyPlatformSortHotGame(form));
    }

    @Operation(summary = "修改遊戲平台排序 互換", description = "互換")
    @PutMapping("/sort")
    public Result<Boolean> modifyPlatformSort(@Valid @RequestBody ModifyPlatformSortForm form) {
        return Result.success(iPlatformService.modifyPlatformSort(form));
    }

    @Operation(summary = "修改遊戲平台排序 置頂置底", description = "0置頂1置底")
    @PutMapping("/sort/top/bottom")
    public Result<Boolean> modifyPlatformSortTopBottom(@Valid @RequestBody ModifyPlatformSortTopBottomForm form) {
        return Result.success(iPlatformService.modifyPlatformSortTopBottom(form));
    }

    @Operation(summary = "修改遊戲平台排序", description = "熱門")
    @PutMapping("/sort/hot")
    public Result<Boolean> modifyPlatformSortByHot(@Valid @RequestBody ModifyPlatformGameSortForm form) {
        return Result.success(iPlatformService.modifyPlatformSortByHot(form));
    }

    @Operation(summary = "修改遊戲平台排序 置頂置底", description = "熱門 0置頂 1置底")
    @PutMapping("/sort/hot/top/bottom")
    public Result<Boolean> modifyPlatformSortTopBottomByHot(@Valid @RequestBody ModifyPlatformSortTopBottomForm form) {
        return Result.success(iPlatformService.modifyPlatformSortTopBottomByHot(form));
    }

    @Operation(summary = "資本概括 中心錢包", description = "取得全部平台的會員錢包")
    @GetMapping("/capital/summary/center/{username}")
    public Result<List<CapitalSummaryCenterVO>> findAllCapitalSummaryCenter(@PathVariable("username") String username) {
        Map<String, String> platformCodeMap = iPlatformService.lambdaQuery()
                .select(Platform::getCode, Platform::getName)
                .list()
                .stream()
                .collect(Collectors.toMap(Platform::getCode, Platform::getName));

        List<CapitalSummaryCenterVO> vos = platformCodeMap.entrySet()
                .parallelStream()
                .filter(filter -> {
                            // 排除未實作的平台遊戲
                            try {
                                gameAdapterExecutor.findByGamePlatFormByCode(filter.getKey());
                            } catch (Exception e) {
                                return false;
                            }
                            return true;
                        }
                )
                .map(platformMap -> {
                            IGameAdapter gameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(platformMap.getKey());

                            Result<BigDecimal> balanceResult = gameAdapter.balance(username);

                            CapitalSummaryCenterVO vo = CapitalSummaryCenterVO.builder()
                                    .platformCode(platformMap.getKey())
                                    .platformName(platformMap.getValue())
                                    .build();

                            if (Result.isSuccess(balanceResult)) {
                                vo.setBalance(balanceResult.getData());
                            }

                            return vo;
                        }
                )
                .collect(Collectors.toList());

        return Result.success(vos);
    }

    @Operation(summary = "資本概括 中心錢包", description = "取得指定平台的會員錢包")
    @GetMapping("/capital/summary/center/{platformCode}/{username}")
    public Result<BigDecimal> findCapitalSummaryCenter(@PathVariable("platformCode") String platformCode, @PathVariable("username") String username) {
        IGameAdapter gameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(platformCode);

        BigDecimal balance = null;
        Result<BigDecimal> balanceResult = gameAdapter.balance(username);
        if (Result.isSuccess(balanceResult)) {
            balance = balanceResult.getData();
        }

        return Result.success(balance);
    }

    @Operation(summary = "將會員在三方的餘額取回平台")
    @PutMapping("/all/platform/balance/{username}")
    public Result<Boolean> getAllPlatformBalance(@PathVariable("username") String username) {
        List<String> platformCodes = iPlatformService.lambdaQuery()
                .select(Platform::getCode)
                .list()
                .stream()
                .map(Platform::getCode)
                .collect(Collectors.toList());

        platformCodes.stream()
                .filter(platformCode -> {
                            // 排除未實作的遊戲平台
                            try {
                                gameAdapterExecutor.findByGamePlatFormByCode(platformCode);
                            } catch (Exception e) {
                                return false;
                            }
                            return true;
                        }
                )
                .forEach(platformCode -> {
                            // 轉回錢包的金額
                            BigDecimal transferOutBalance = BigDecimal.ZERO;

                            IGameAdapter gameAdapter = gameAdapterExecutor.findByGamePlatFormByCode(platformCode);

                            // 取得會員在遊戲裡的餘額
                            Result<BigDecimal> balanceResult = gameAdapter.balance(username);
                            if (Result.isSuccess(balanceResult)) {
                                BigDecimal balance = balanceResult.getData();
                                // 判斷有餘額時將餘額轉回錢包
                                if (Objects.nonNull(balance) && balance.compareTo(BigDecimal.ZERO) > 0) {
                                    Result<TransferStateVO> resultTransferOutBalance = gameAdapter.transferIntoPlatform(username, balance, UUIDUtils.genOrderId("TC"));
                                    // 轉回錢包成功後加入轉回錢包的金額
                                    if (Result.isSuccess(resultTransferOutBalance)) {
                                        TransferStateVO transferStateVO = resultTransferOutBalance.getData();
                                        transferOutBalance = transferOutBalance.add(AdapterTransferStateEnum.SUCCESS == transferStateVO.getState() ? transferStateVO.getBalance() : BigDecimal.ZERO);
                                    }
                                }
                            }

                            // 有取回餘額時將餘額加回會員錢包
                            if (transferOutBalance.compareTo(BigDecimal.ZERO) > 0) {
                                Result<MemberInfoDTO> resultMemberInfo = memberFeignClient.getMemberInfo(username);
                                if (Result.isSuccess(resultMemberInfo)) {
                                    MemberInfoDTO memberInfo = resultMemberInfo.getData();
                                    paymentClient.addBalance(
                                            AddBalanceDTO.builder()
                                                    .memberId(memberInfo.getId())
                                                    .balance(transferOutBalance)
                                                    .type(TRANSFER_INTO_PLATFORM.getType())
                                                    .betRate(BigDecimal.ONE)
                                                    .balanceChangeTypeLinkEnum(TRANSFER_INTO_PLATFORM)
                                                    .note(platformCode + "/" +TRANSFER_INTO_PLATFORM.getI18n())
                                                    .build()
                                    );
                                }
                            }
                        }
                );

        return Result.success(true);
    }


}
