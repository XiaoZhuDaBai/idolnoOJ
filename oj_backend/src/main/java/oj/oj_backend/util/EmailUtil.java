package oj.oj_backend.util;

import org.springframework.beans.factory.annotation.Value;

import javax.mail.Session;
import java.util.Properties;
import java.util.Random;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/6/24 20:51
 */
public class EmailUtil {
    /**
     * 生产的验证码位数
     */
    private final static int generateVerificationCodeLength = 6;

    private final static String[] metaCode={"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
            "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public static String generateVerificationCode() {
        Random random = new Random();
        StringBuilder verificationCode = new StringBuilder();
        while (verificationCode.length()<generateVerificationCodeLength){
            int i = random.nextInt(metaCode.length);
            verificationCode.append(metaCode[i]);
        }
        return verificationCode.toString();
    }


}
