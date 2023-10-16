package com.c88.game.adapter.service;

import com.c88.game.adapter.constants.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallBackService {

    private final RedisTemplate<String, String> redisTemplate;

    public String validCMDToken(String token) {
        log.info("=====cmd-valid-token-start===== token:{}", token);
        String redisValidKey = RedisKey.TOKEN_VALID_BY_CMD + ":" + token;
        String cmdTokenValid = redisTemplate.opsForValue().get(redisValidKey);
        redisTemplate.delete(redisValidKey);

        StringBuilder sb = new StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<authenticate>")
                .append("<member_id>").append(StringUtils.isNotBlank(cmdTokenValid) ? cmdTokenValid : "").append("</member_id>")
                .append("<status_code>").append(StringUtils.isNotBlank(cmdTokenValid) ? "0" : "2").append("</status_code>")
                .append("<message>").append(StringUtils.isNotBlank(cmdTokenValid) ? "Success" : "Failed").append("</message>")
                .append("</authenticate>");

        log.info("=====cmd-valid-token-end=====");
        return sb.toString();
    }
}
