package com.c88.game.adapter.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
@Slf4j
public class PSGenerateTokenUtils {

    private static final String ALGORITHM = "AES";
    private static final String INIT_VECTOR = "RandomInitVector";
    public static String generateToken(String agentCode, String agentKey, String secretKey) {
        String sTimestamp = String.valueOf(System.currentTimeMillis());
        String hashToken = DigestUtils.md5Hex(agentCode + sTimestamp + agentKey);
        String tokenPayLoad = String.format("%s|%s|%s", agentCode, sTimestamp, hashToken);
        String token = encryptAES(secretKey, tokenPayLoad);
        return token;
    }

    public static String encryptAES(String secretKey, String tokenPayLoad) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            byte[] encrypted = cipher.doFinal(tokenPayLoad.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            log.info("An exceptionn occurred when encript key:{}, plain text: {}, exception {}",
                    secretKey, tokenPayLoad, ex.toString());
        }
        return null;

    }
}
