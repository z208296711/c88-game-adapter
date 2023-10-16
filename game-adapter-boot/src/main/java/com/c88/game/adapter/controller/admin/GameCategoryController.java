package com.c88.game.adapter.controller.admin;

import com.c88.common.core.result.Result;
import com.c88.common.web.annotation.AnnoLog;
import com.c88.common.web.log.LogOpResponse;
import com.c88.common.web.log.OperationEnum;
import com.c88.game.adapter.pojo.entity.GameCategory;
import com.c88.game.adapter.pojo.form.ModifyGameCategoryNoteForm;
import com.c88.game.adapter.pojo.form.ModifyGameCategorySortForm;
import com.c88.game.adapter.pojo.vo.GameCategoryVO;
import com.c88.game.adapter.service.IGameCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "遊戲類型")
@RequiredArgsConstructor
@RequestMapping("/api/v1/category")
public class GameCategoryController {

    private final IGameCategoryService iGameCategoryService;

    @Operation(summary = "取得遊戲類型")
    @GetMapping
    public Result<List<GameCategoryVO>> findGameCategory() {
        return Result.success(iGameCategoryService.findGameCategory());
    }

    @Operation(summary = "修改遊戲類型描述")
    @PutMapping("/note")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.name, #form.note}",
            desc = "修改了 {name} 的類型描述為 {note}",
            menu = "menu.content",
            menuPage = "menu.game-type",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "content_game_type.operation_log02")
    public Result<LogOpResponse> modifyGameCategoryNote(@RequestBody ModifyGameCategoryNoteForm form) {
        iGameCategoryService.modifyGameCategoryNote(form);
        LogOpResponse<GameCategory, GameCategory> response = new LogOpResponse<>();
        return Result.success(response);
    }

    @Operation(summary = "修改遊戲類型排序")
    @PutMapping("/sort")
    @AnnoLog(uuId = "#userId",
            operationEnum = OperationEnum.UPDATE,
            content = "new String[]{#form.name}",
            addition = "sort",
            desc = "修改了 {name} 的順序為 {sort}",
            menu = "menu.content",
            menuPage = "menu.game-type",
            ip = "#{T(com.c88.common.web.util.HttpUtils).getClientIp()}",
            i18nKey = "content_game_type.operation_log01")
    public Result<LogOpResponse> modifyGameCategorySort(@RequestBody ModifyGameCategorySortForm form) {
        GameCategory before = iGameCategoryService.getById(form.getId());

        iGameCategoryService.modifyGameCategorySort(form);

        GameCategory after = iGameCategoryService.getById(form.getId());

        LogOpResponse<GameCategory, GameCategory> response = new LogOpResponse<>();
        response.setAfter(after);
        response.setBefore(before);
        return Result.success(response);
    }

}
