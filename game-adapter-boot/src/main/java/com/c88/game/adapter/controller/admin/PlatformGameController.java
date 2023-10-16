package com.c88.game.adapter.controller.admin;

import com.c88.common.core.result.PageResult;
import com.c88.common.core.result.Result;
import com.c88.game.adapter.dto.GameVO;
import com.c88.game.adapter.pojo.form.*;
import com.c88.game.adapter.pojo.vo.PlatformGameSortVO;
import com.c88.game.adapter.pojo.vo.PlatformGameVO;
import com.c88.game.adapter.service.IPlatformGameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "平台遊戲列表")
@RequiredArgsConstructor
@RequestMapping("/api/v1/platform/game")
public class PlatformGameController {

    private final IPlatformGameService iPlatformGameService;

    @Operation(summary = "查詢平台遊戲")
    @GetMapping
    public PageResult<PlatformGameVO> findPlatformGame(@ParameterObject FindPlatformGameForm form) {
        return PageResult.success(iPlatformGameService.findPlatformGame(form));
    }

    @Operation(summary = "新增平台遊戲")
    @PostMapping
    public Result<Boolean> addPlatformGame(@Validated @RequestBody AddPlatformGameForm form) {
        return Result.success(iPlatformGameService.addPlatformGame(form));
    }

    @Operation(summary = "修改平台遊戲")
    @PutMapping
    public Result<Boolean> modifyPlatformGame(@Validated @RequestBody ModifyPlatformGameForm form) {
        return Result.success(iPlatformGameService.modifyPlatformGameForm(form));
    }

    @Operation(summary = "批量刪除平台遊戲")
    @DeleteMapping
    public Result<Boolean> deletePlatformGame(@Parameter(description = "平台遊戲ID") @RequestBody List<Integer> ids) {
        return Result.success(iPlatformGameService.deletePlatformGame(ids));
    }

    @Operation(summary = "查詢平台遊戲排序內容")
    @GetMapping("/sort")
    public PageResult<PlatformGameSortVO> findPlatformGameSort(@Validated @ParameterObject FindPlatformGameSortForm form) {
        return PageResult.success(iPlatformGameService.findPlatformGameSort(form));
    }

    @Operation(summary = "修改平台遊戲排序 互換", description = "互換")
    @PutMapping("/sort")
    public Result<Boolean> modifyPlatformGameSort(@Validated @RequestBody ModifyPlatformGameSortForm form) {
        return Result.success(iPlatformGameService.modifyPlatformGameSort(form));
    }

    @Operation(summary = "修改平台遊戲排序 置頂置底", description = "0置頂1置底")
    @PutMapping("/sort/top/bottom")
    public Result<Boolean> modifyPlatformGameSortTopBottom(@Validated @RequestBody ModifyPlatformGameSortTopBottomForm form) {
        return Result.success(iPlatformGameService.modifyPlatformGameSortTopBottom(form));
    }

    @Operation(summary = "取得平台底下所有遊戲", description = "內部調用", hidden = true)
    @GetMapping("/all")
    public Result<Map<String, List<GameVO>>> getGameListByPlatforms(@RequestParam List<String> platformCodeX) {
        return Result.success(iPlatformGameService.getGameListByPlatforms(platformCodeX));
    }

}
