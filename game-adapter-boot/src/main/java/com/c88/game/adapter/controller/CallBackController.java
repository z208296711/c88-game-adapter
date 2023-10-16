package com.c88.game.adapter.controller;

import com.alibaba.fastjson.JSONObject;
import com.c88.game.adapter.dto.PGVerifySessionDTO;
import com.c88.game.adapter.service.CallBackService;
// import com.c88.game.adapter.service.third.png.PNGGameAdapter;
import com.c88.game.adapter.service.third.adapter.PGGameAdapter;
import com.c88.game.adapter.vo.PGVerifySessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "遊戲回call", description = "https://{profile}-c88-callback.hyu.tw/game-adapter/callBack/**")
@RequiredArgsConstructor
@RequestMapping("/callBack")
public class CallBackController {

    private final CallBackService callBackService;

    // private final PNGGameAdapter pngGameAdapter;

    private final PGGameAdapter pgGameAdapter;

    @Operation(summary = "驗證SMB的Token")
    @GetMapping(value = "/auth/valid/cmd", produces = MediaType.TEXT_XML_VALUE)
    public String validCMDToken(
            @Parameter(description = "token") String token,
            @Parameter(name = "secret_key", hidden = true, description = "现在永远使用 [String.Empty]值") String secretKey
    ) {
        return callBackService.validCMDToken(token);
    }

    @Operation(summary = "PNG消息")
    @PostMapping(value = "/png/game/record")
    public String pngMessage(@RequestBody JSONObject jsonObject) {
        log.info("Msg:{}", jsonObject.toJSONString());
        try {
            // pngGameAdapter.processingPNGMessage(jsonObject);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.info("Pre DealWith Msg:{}", jsonObject.toJSONString());
        }
        return "success";
    }

    @Operation(summary = "PG登入驗證")
    @PostMapping(value = "/VerifySession", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public PGVerifySessionVO verifyPGToken(PGVerifySessionDTO form, @RequestParam(name = "trace_id") String traceId) {
        log.info("inputDTO:{}, traceId :{}", form, traceId);
        return pgGameAdapter.verifyLoginSession(form);
    }


}
