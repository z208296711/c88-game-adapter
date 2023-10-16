package com.c88.game.adapter.api;

import com.c88.common.core.result.Result;
import com.c88.game.adapter.dto.CategoryRebateRecordDTO;
import com.c88.game.adapter.dto.GameCategoryVO;
import com.c88.game.adapter.dto.MemberRebateRecordDTO;
import com.c88.game.adapter.dto.GameVO;
import com.c88.game.adapter.dto.MemberRebateRecordTotalDTO;
import com.c88.game.adapter.vo.ReportBetOrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@FeignClient(name = "c88-game-adapter", path = "/game-adapter")
public interface GameFeignClient {

    @GetMapping("/h5/game/findAllBalance/{uid}/{username}")
    Result<BigDecimal> findAllBalance(@PathVariable Long uid, @PathVariable String username);

    @GetMapping("/h5/game/findMemberNonSettleBetAmount/{uid}")
    Result<BigDecimal> findMemberNonSettleBetAmount(@PathVariable Long uid);

    @GetMapping("/h5/game/lastSession/{uid}")
    Result<String> lastMemberGameSession(@PathVariable Long uid);

    @GetMapping("/api/v1/bet/order/{memberId}")
    Result<BigDecimal> getMemberValidBet(@PathVariable Long memberId,
                                         @RequestParam(required = false) String platformCode,
                                         @RequestParam(required = false) String gameName,
                                         @RequestParam(required = false) String gameCategoryCode,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime);

    @GetMapping("/api/v1/bet/order/report/company")
    Result<ReportBetOrderVO> getBerOrderCount(@RequestParam() String startTime, @RequestParam() String endTime);
    @GetMapping("/api/v1/bet/order/report/company/settle")
    Result<ReportBetOrderVO> getBerOrderCountBySettleDate(@RequestParam() String startTime, @RequestParam() String endTime);

    @GetMapping("/api/v1/bet/order/report/betSum")
    Result<List<Map<String,Object>>> getBetSumByMemberIds(@RequestParam() List<Long> memberIds, @RequestParam() String startTime, @RequestParam() String endTime);

    @GetMapping("/api/v1/category")
    Result<List<GameCategoryVO>> findGameCategory();

    @GetMapping("/api/v1/report/rebate/sum")
    Result<MemberRebateRecordTotalDTO> findRebateRecordSum(@RequestParam() String startTime, @RequestParam() String endTime);

    @GetMapping("/api/v1/report/rebate/category")
    Result<List<CategoryRebateRecordDTO>> findRebateRecordCategory(@RequestParam() String startTime, @RequestParam() String endTime);

    @GetMapping("/api/v1/platform/game/all")
    Result<Map<String, List<GameVO>>> getGameListByPlatforms(@RequestParam List<String> platformCodeX);


    @GetMapping("/api/v1/report/rebate/member")
    Result<List<MemberRebateRecordDTO>> findRebateRecordMember(@RequestParam() String startTime, @RequestParam() String endTime);
}
