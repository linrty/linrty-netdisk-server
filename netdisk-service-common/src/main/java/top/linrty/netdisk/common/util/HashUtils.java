package top.linrty.netdisk.common.util;

import cn.hutool.core.util.HexUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    public HashUtils() {
    }

    public static MessageDigest getDigest(String algorithmName) {
        try {
            return MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException var3) {
            String var2 = "No native '" + algorithmName + "' MessageDigest instance available on the current JVM.";
            return null;
        }
    }

    public static String hashHex(String algorithmName, String source, String salt, int hashIterations) {
        return salt == null ? hashHex(algorithmName, (byte[])source.getBytes(StandardCharsets.UTF_8), (byte[])null, hashIterations) : hashHex(algorithmName, source.getBytes(StandardCharsets.UTF_8), salt.getBytes(StandardCharsets.UTF_8), hashIterations);
    }

    public static String hashHex(String algorithmName, byte[] bytes, byte[] salt, int hashIterations) {
        byte[] hash = hash(bytes, algorithmName, salt, hashIterations);
        return HexUtil.encodeHexStr(hash);
    }

    public static byte[] hash(byte[] bytes, String algorithmName, byte[] salt, int hashIterations) {
        MessageDigest digest = getDigest(algorithmName);
        if (salt != null) {
            digest.reset();
            digest.update(salt);
        }

        byte[] hashed = digest.digest(bytes);
        int iterations = hashIterations - 1;

        for(int i = 0; i < iterations; ++i) {
            digest.reset();
            hashed = digest.digest(hashed);
        }

        return hashed;
    }
}
