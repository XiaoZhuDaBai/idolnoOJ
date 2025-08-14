package oj.oj_backend.service.impl;


import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import oj.oj_backend.comm.ResponseResult;
import oj.oj_backend.model.User;
import oj.oj_backend.mapper.UserMapper;
import oj.oj_backend.model.response.SubmissionResponse;
import oj.oj_backend.model.vo.LoginVo;
import oj.oj_backend.model.response.MyRecentSubmissionResponse;
import oj.oj_backend.model.response.MyRecentWrongSubmissionResponse;
import oj.oj_backend.model.response.MySubmissionDataResponse;
import oj.oj_backend.service.UserService;
import oj.oj_backend.util.EmailUtil;
import oj.oj_backend.util.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final UserMapper userMapper;
    @Value("${feedback.email.account}")
    private String account;
    @Value("${feedback.email.auth}")
    private String auth;
    private final static String PATTERN =
            "^[A-Za-z0-9\\u4e00-\\u9fa5][A-Za-z0-9._%+-\\u4e00-\\u9fa5]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String HTML = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>邮箱验证码</title>\n" +
            "    <style>\n" +
            "        table {\n" +
            "            width: 700px;\n" +
            "            margin: 0 auto;\n" +
            "        }\n" +
            "\n" +
            "        #top {\n" +
            "            width: 700px;\n" +
            "            border-bottom: 1px solid #ccc;\n" +
            "            margin: 0 auto 30px;\n" +
            "        }\n" +
            "\n" +
            "        #top table {\n" +
            "            font: 12px Tahoma, Arial, 宋体;\n" +
            "            height: 40px;\n" +
            "        }\n" +
            "\n" +
            "        #content {\n" +
            "            width: 680px;\n" +
            "            padding: 0 10px;\n" +
            "            margin: 0 auto;\n" +
            "        }\n" +
            "\n" +
            "        #content_top {\n" +
            "            line-height: 1.5;\n" +
            "            font-size: 14px;\n" +
            "            margin-bottom: 25px;\n" +
            "            color: #4d4d4d;\n" +
            "        }\n" +
            "\n" +
            "        #content_top strong {\n" +
            "            display: block;\n" +
            "            margin-bottom: 15px;\n" +
            "        }\n" +
            "\n" +
            "        #content_top strong span {\n" +
            "            color: #f60;\n" +
            "            font-size: 16px;\n" +
            "        }\n" +
            "\n" +
            "        #verificationCode {\n" +
            "            color: #f60;\n" +
            "            font-size: 24px;\n" +
            "        }\n" +
            "\n" +
            "        #content_bottom {\n" +
            "            margin-bottom: 30px;\n" +
            "        }\n" +
            "\n" +
            "        #content_bottom small {\n" +
            "            display: block;\n" +
            "            margin-bottom: 20px;\n" +
            "            font-size: 12px;\n" +
            "            color: #747474;\n" +
            "        }\n" +
            "\n" +
            "        #bottom {\n" +
            "            width: 700px;\n" +
            "            margin: 0 auto;\n" +
            "        }\n" +
            "\n" +
            "        #bottom div {\n" +
            "            padding: 10px 10px 0;\n" +
            "            border-top: 1px solid #ccc;\n" +
            "            color: #747474;\n" +
            "            margin-bottom: 20px;\n" +
            "            line-height: 1.3em;\n" +
            "            font-size: 12px;\n" +
            "        }\n" +
            "\n" +
            "        #content_top strong span {\n" +
            "            font-size: 18px;\n" +
            "            color: #FE4F70;\n" +
            "        }\n" +
            "\n" +
            "        #sign {\n" +
            "            text-align: right;\n" +
            "            font-size: 18px;\n" +
            "            color: #FE4F70;\n" +
            "            font-weight: bold;\n" +
            "        }\n" +
            "\n" +
            "        #verificationCode {\n" +
            "            height: 100px;\n" +
            "            width: 680px;\n" +
            "            text-align: center;\n" +
            "            margin: 30px 0;\n" +
            "        }\n" +
            "\n" +
            "        #verificationCode div {\n" +
            "            height: 100px;\n" +
            "            width: 680px;\n" +
            "\n" +
            "        }\n" +
            "\n" +
            "        .button {\n" +
            "            color: #FE4F70;\n" +
            "            margin-left: 10px;\n" +
            "            height: 80px;\n" +
            "            width: 80px;\n" +
            "            resize: none;\n" +
            "            font-size: 42px;\n" +
            "            border: none;\n" +
            "            outline: none;\n" +
            "            padding: 10px 15px;\n" +
            "            background: #ededed;\n" +
            "            text-align: center;\n" +
            "            border-radius: 17px;\n" +
            "            box-shadow: 6px 6px 12px #cccccc,\n" +
            "            -6px -6px 12px #ffffff;\n" +
            "        }\n" +
            "\n" +
            "        .button:hover {\n" +
            "            box-shadow: inset 6px 6px 4px #d1d1d1,\n" +
            "            inset -6px -6px 4px #ffffff;\n" +
            "        }\n" +
            "\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "<table>\n" +
            "    <tbody>\n" +
            "    <tr>\n" +
            "        <td>\n" +
            "            <div id=\"top\">\n" +
            "                <table>\n" +
            "                    <tbody><tr><td></td></tr></tbody>\n" +
            "                </table>\n" +
            "            </div>\n" +
            "\n" +
            "            <div id=\"content\">\n" +
            "                <div id=\"content_top\">\n" +
            "                    <strong>尊敬的用户：您好！</strong>\n" +
            "                    <strong>\n" +
            "                        您正在进行<span>注册账号</span>操作，请在验证码中输入以下验证码完成操作：\n" +
            "                    </strong>\n" +
            "                    <div id=\"verificationCode\">\n" +
            "                        %s\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "                <div id=\"content_bottom\">\n" +
            "                    <small>\n" +
            "                        注意：此操作可能会修改您的密码、登录邮箱或绑定手机。如非本人操作，请及时登录并修改密码以保证帐户安全\n" +
            "                        <br>（工作人员不会向你索取此验证码，请勿泄漏！)\n" +
            "                    </small>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "            <div id=\"bottom\">\n" +
            "                <div>\n" +
            "                    <p>此为系统邮件，请勿回复<br>\n" +
            "                        请保管好您的邮箱，避免账号被他人盗用\n" +
            "                    </p>\n" +
            "                    <p id=\"sign\">——Xiaozhudabai</p>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </td>\n" +
            "    </tr>\n" +
            "    </tbody>\n" +
            "</table>\n" +
            "</body>\n" +
            "</html>";

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    @Override
    public boolean isExistInTable(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        queryWrapper.eq("email", email);
        return userMapper.exists(queryWrapper);
    }

    @Override
    public ResponseResult<String> toLoginOrRegister(LoginVo loginVo) {
        String email = loginVo.getEmail();
        // 校验合法邮箱
        if (!Pattern.matches(PATTERN, email)) {
            return ResponseResult.fail( "邮箱格式错误");
        }

        String answer = stringRedisTemplate.opsForValue().get(email);
        if (answer == null) {
            return  ResponseResult.fail( "验证码失效");
        }

        String verifyCode = loginVo.getInput_code();
        if (!verifyCode.equals(answer)) {
            return ResponseResult.fail("验证码错误");
        }

        // 验证码正确，删除验证码
        stringRedisTemplate.delete(email);

        if (!isExistInTable(email)) {
            // 注册进数据库

            // 分配唯一id
            User uu = new User();
            uu.setUuid(UUID.randomUUID().toString());
            uu.setAuth("user");
            uu.setEmail(email);
            uu.setStatus(1);
            uu.setCreateTime(new Date());
            int insert = userMapper.insert(uu);
            if (insert <= 0) {
                return ResponseResult.fail("注册失败");
            }
        }

        // 分发token
        Map<String, Object> claims = new HashMap<>();

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));
        String uuid = user.getUuid();

        claims.put("uuid", uuid);
        claims.put("email", email);
        String token = JWTUtil.generateJWT(claims);

        // 存入redis
        stringRedisTemplate.opsForValue().set("jwt-" + uuid, token, 7 * 24 * 60 * 60, TimeUnit.SECONDS);

        return ResponseResult.success(200,"登录成功",JSONUtil.toJsonStr(user));
    }

    @Override
    public String getJWT(String uuid) {
        String s = stringRedisTemplate.opsForValue().get("jwt-" + uuid);
        if (s == null) {
            return "未登录成功";
        }
        return s;
    }


    @Override
    public String sendVerificationCode(String email) {
        // 接口防刷
        String key = "email:send_time:" + email;
        String lastSendTime = stringRedisTemplate.opsForValue().get(key);
        if (lastSendTime != null) {
            long interval = System.currentTimeMillis() - Long.parseLong(lastSendTime);
            if (interval < 60_000) { // 60秒内不能重复发送
                return "请勿频繁请求验证码";
            }
        }
        stringRedisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), 60, TimeUnit.SECONDS);

        //	创建一个配置文件，并保存
        Properties props = new Properties();

        //	SMTP服务器连接信息
        //  126——smtp.126.com
        //  163——smtp.163.com
        //  qq——smtp.qq.com"
        props.put("mail.smtp.host", "smtp.qq.com");//	SMTP主机名

        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true"); // 启用SSL加密
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // 强制使用SSL

        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(account, auth);
            }
        });

        //  控制台打印调试信息
        session.setDebug(true);
        // 创建邮件
        MimeMessage message = new MimeMessage(session);
        String code;
        try {
            message.setSubject("idolno-oj 验证码");
            code = EmailUtil.generateVerificationCode();
            String html = String.format(HTML, code);
            // 用 HTML 格式发送
            message.setContent(html, "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(account));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(email));
            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("发送失败" + e);
        }

        // 存入redis
        stringRedisTemplate.opsForValue().set(email, code, 300, TimeUnit.SECONDS);

        return "验证码发送成功";
    }

    @Override
    public List<MyRecentWrongSubmissionResponse> getMyRecentWrongSubmission(String uuid) {
        return userMapper.getMyRecentWrongSubmission(uuid);
    }


    @Override
    public List<MyRecentSubmissionResponse> getMyRecentSubmission(String uuid, int page) {
        return userMapper.getMyRecentSubmission(uuid, (page - 1) * 5);
    }

    @Override
    public int getMyRecentSubmissionCount(String uuid) {
        return userMapper.getMyRecentSubmissionCount(uuid);
    }

    @Override
    public List<MySubmissionDataResponse> getMySubmissionData(String uuid) {
        return userMapper.getMySubmissionData(uuid);
    }

    @Override
    public List<SubmissionResponse> getCommits(String problemName, String language, String status, String userType, String uuid,int page) {
        return userMapper.getCommits(problemName, language, status, userType, uuid, (page - 1) * 15);
    }

    @Override
    public int getCommitsCount(String problemName, String language, String status, String userType, String uuid) {
        return userMapper.getCommitsCount(problemName, language, status, userType, uuid);
    }
}
