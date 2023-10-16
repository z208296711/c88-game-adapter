package com.c88.game.adapter.controller.admin;

import com.c88.common.core.result.Result;
import com.c88.game.adapter.mapstruct.PlatformGameStyleConverter;
import com.c88.game.adapter.pojo.form.ModifyPlatformGameStyleForm;
import com.c88.game.adapter.pojo.vo.PlatformGameStyleVO;
import com.c88.game.adapter.service.IPlatformGameStyleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Tag(name = "遊戲類型樣式")
@RequiredArgsConstructor
@RequestMapping("/api/v1/platform/game/style")
public class PlatformGameStyleController {

    private final IPlatformGameStyleService iPlatformGameStyleService;

    private final PlatformGameStyleConverter platformGameStyleConverter;

    @Operation(summary = "找遊戲類型樣式")
    @GetMapping
    public Result<List<PlatformGameStyleVO>> findPlatformGameStyle() {
        return Result.success(iPlatformGameStyleService.list()
                .stream()
                .map(platformGameStyleConverter::toVO)
                .collect(Collectors.toList())
        );
    }

    @Operation(summary = "修改遊戲類型樣式")
    @PutMapping
    public Result<Boolean> modifyPlatformGameStyle(@Validated @RequestBody ModifyPlatformGameStyleForm form) {
        return Result.success(iPlatformGameStyleService.modifyPlatformGameStyle(form));
    }

}
