package com.c88.game.adapter.api;

import com.c88.common.core.result.Result;
import com.c88.game.adapter.dto.GameCategoryVO;
import com.c88.game.adapter.dto.PlatformDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "c88-game-adapter", path = "/game-adapter/api/v1/platform")
public interface PlatformClient {

    @GetMapping("/all/dto")
    Result<List<PlatformDTO>> findAllPlatformDTO();


}
