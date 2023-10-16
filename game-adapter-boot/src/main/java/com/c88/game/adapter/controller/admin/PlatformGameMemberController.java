package com.c88.game.adapter.controller.admin;

import com.c88.common.core.result.PageResult;
import com.c88.game.adapter.pojo.form.FindPlatformGameMemberForm;
import com.c88.game.adapter.pojo.vo.PlatformGameMemberVO;
import com.c88.game.adapter.service.IPlatformGameMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "平台遊戲會員")
@RequiredArgsConstructor
@RequestMapping("/api/v1/platform/game")
public class PlatformGameMemberController {

    private final IPlatformGameMemberService iPlatformGameMemberService;

    @Operation(summary = "查詢平台遊戲會員")
    @GetMapping(value = "/member")
    public PageResult<PlatformGameMemberVO> findPlatformGameMember(FindPlatformGameMemberForm form) {
        return PageResult.success(iPlatformGameMemberService.findPlatformGameMember(form));
    }

}
