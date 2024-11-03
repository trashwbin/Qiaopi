package com.qiaopi.utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiaopi.entity.Questions;
import lombok.extern.slf4j.Slf4j;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

@Slf4j
public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 加密
    public static String encrypt(String data, String secretKey) throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // 解密
    public static String decrypt(String encryptedData, String secretKey) throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] original = cipher.doFinal(decoded);
        return new String(original);
    }


    // 实现对集合中的每一个String进行加密
    public static List<String> encryptListAboutString(List<String> list, String secretKey) throws Exception {
        List<String> encryptedList = new ArrayList<>();
        for (String item : list) {
            encryptedList.add(encrypt(item, secretKey));
        }
        return encryptedList;
    }

    // 实现对集合中的每一个String进行解密
    public static List<String> decryptListAboutString(List<String> list, String secretKey) throws Exception {
        List<String> decryptedList = new ArrayList<>();
        for (String item : list) {
            decryptedList.add(decrypt(item, secretKey));
        }
        return decryptedList;
    }


    // 将 List<Question> 加密
    public static String encryptQuestions(List<Questions> questions, String secretKey) throws Exception {
        try {
            // 将 List<Question> 转换为 JSON 字符串
            String json = objectMapper.writeValueAsString(questions);
            // 加密 JSON 字符串
            return encrypt(json, secretKey);
        } catch (JsonProcessingException e) {
            log.error("Error converting questions to JSON", e);
            throw e;
        }
    }

    // 解密并将加密的 JSON 字符串转换为 List<Question>
    public static List<Questions> decryptQuestions(String encryptedQuestions, String secretKey) throws Exception {
        // 解密 JSON 字符串
        String json = AESUtil.decrypt(encryptedQuestions, secretKey);
        try {
            // 将 JSON 字符串转换为 List<Question>
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, Questions.class));
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to questions", e);
            throw e;
        }
    }


}
