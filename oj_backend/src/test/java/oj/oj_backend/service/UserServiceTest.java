package oj.oj_backend.service;

import oj.oj_backend.model.vo.LoginVo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.regex.Pattern;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/7/14 0:16
 */
@SpringBootTest
public class UserServiceTest {
    @Resource
    private UserService userService;
    private final static String PATTERN =
            "^[A-Za-z0-9\\u4e00-\\u9fa5][A-Za-z0-9._%+-\\u4e00-\\u9fa5]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Test

    public void test() {
//        String s = userService.sendVerificationCode("1105774747@qq.com");
//        System.out.println(s);
//        LoginVo loginVo = new LoginVo();
//        loginVo.setEmail("1105774747@qq.com");
//        loginVo.setInput_code("123456");
//        String loginOrRegister = userService.toLoginOrRegister(loginVo);
//        System.out.println(loginOrRegister);

        boolean matches = Pattern.matches(PATTERN, "1105774747@qq.com");
        System.out.println( matches);
    }

}
