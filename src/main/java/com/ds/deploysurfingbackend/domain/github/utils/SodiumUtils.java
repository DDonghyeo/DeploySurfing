package com.ds.deploysurfingbackend.domain.github.utils;

import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.interfaces.Box;
import com.goterl.lazysodium.utils.Key;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;

@RequiredArgsConstructor
@Component
public class SodiumUtils {

    private static final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());

    public static String encryptSecret(String repoPublicKey, String secretValue) {
        try {
            // 1. GitHub에서 제공한 Public Key를 Base64 디코딩
            byte[] decodedPublicKey = Base64.getDecoder().decode(repoPublicKey);
            Key publicKey = Key.fromBytes(decodedPublicKey);

            // 2. 암호화할 비밀값을 바이트 배열로 변환
            byte[] secretBytes = secretValue.getBytes();

            // 3. 암호화 (crypto_box_seal 사용, 공개 키만 사용)
            byte[] encryptedBytes = new byte[Box.SEALBYTES + secretBytes.length];
            boolean success = lazySodium.cryptoBoxSeal(encryptedBytes, secretBytes, secretBytes.length, publicKey.getAsBytes());

            if (!success) {
                throw new CustomException(CommonErrorCode.SERVER_ERROR, "Secret 암호화 실패");
            }

            // 4. 암호화된 데이터를 Base64 인코딩하여 반환
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new CustomException(CommonErrorCode.SERVER_ERROR, "Secret 암호화 중 오류 발생: " + e.getMessage());
        }
    }
}
