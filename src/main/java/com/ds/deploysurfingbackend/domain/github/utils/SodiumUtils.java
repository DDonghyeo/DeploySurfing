package com.ds.deploysurfingbackend.domain.github.utils;

import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import com.goterl.lazysodium.interfaces.Box;
import com.goterl.lazysodium.utils.Key;
import com.goterl.lazysodium.utils.KeyPair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;

@RequiredArgsConstructor
@Component
public class SodiumUtils {

    private static final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());

    public static String encryptSecret(String repoPublicKey, String message) {
        try {
            // 1. Repository Public Key 디코딩
            byte[] decodedPublicKey = Base64.getDecoder().decode(repoPublicKey);
            Key publicKey = Key.fromBytes(decodedPublicKey);

            // 2. 임시 키 쌍 생성
            KeyPair ephemeralKeyPair = lazySodium.cryptoBoxKeypair();

            // 3. 암호화
            byte[] messageBytes = message.getBytes();
            byte[] cipherText = new byte[Box.SEALBYTES + messageBytes.length];
            lazySodium.cryptoBoxSeal(cipherText, messageBytes, messageBytes.length, publicKey.getAsBytes());

            // 4. 암호화된 메시지, 임시 공개키 결합
            byte[] combined = new byte[ephemeralKeyPair.getPublicKey().getAsBytes().length + cipherText.length];
            System.arraycopy(ephemeralKeyPair.getPublicKey().getAsBytes(), 0, combined, 0, ephemeralKeyPair.getPublicKey().getAsBytes().length);
            System.arraycopy(cipherText, 0, combined, ephemeralKeyPair.getPublicKey().getAsBytes().length, cipherText.length);

            // 5. Base64 인코딩
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new CustomException(CommonErrorCode.SERVER_ERROR, "Secret 암호화 과정에 문제가 발생했습니다.");
        }
    }
}
