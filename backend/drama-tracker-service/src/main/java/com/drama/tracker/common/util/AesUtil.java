package com.drama.tracker.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 加解密工具。
 * 格式: Base64(iv + ciphertext + tag)
 */
public class AesUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private AesUtil() {}

    /**
     * 将密钥字符串转为 32 字节的 AES 密钥（取 SHA-256 或截断/补齐）。
     */
    private static SecretKeySpec deriveKey(String secret) {
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];
        System.arraycopy(raw, 0, key, 0, Math.min(raw.length, 32));
        return new SecretKeySpec(key, "AES");
    }

    /**
     * 加密明文。
     * @param plaintext 明文
     * @param secret 密钥字符串（至少32字符）
     * @return Base64 编码的密文
     */
    public static String encrypt(String plaintext, String secret) {
        if (plaintext == null || plaintext.isEmpty()) return plaintext;
        try {
            SecretKeySpec keySpec = deriveKey(secret);
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // iv + encrypted (包含 tag)
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

            return "ENC:" + Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("AES encrypt failed", e);
        }
    }

    /**
     * 解密密文。
     * @param ciphertext Base64 编码的密文（以 "ENC:" 开头）
     * @param secret 密钥字符串
     * @return 明文
     */
    public static String decrypt(String ciphertext, String secret) {
        if (ciphertext == null || ciphertext.isEmpty()) return ciphertext;
        // 兼容未加密的旧数据
        if (!ciphertext.startsWith("ENC:")) return ciphertext;
        try {
            byte[] decoded = Base64.getDecoder().decode(ciphertext.substring(4));
            SecretKeySpec keySpec = deriveKey(secret);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
            byte[] encrypted = new byte[decoded.length - IV_LENGTH];
            System.arraycopy(decoded, IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 解密失败返回原文（可能是旧数据）
            return ciphertext;
        }
    }
}
