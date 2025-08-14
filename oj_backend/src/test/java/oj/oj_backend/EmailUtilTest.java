package oj.oj_backend;

import oj.oj_backend.util.EmailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/6/24 21:03
 */
@SpringBootTest
public class EmailUtilTest {
    @Value("${feedback.email.account}")
    private String account;
    @Value("${feedback.email.auth}")
    private String auth;

    @Test
    public void test() {
        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.qq.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true"); // 启用SSL加密
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // 强制使用SSL

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account, auth);
            }
        });

        session.setDebug(true);
//	创建邮件对象
        MimeMessage message = new MimeMessage(session);
        try {
            message.setSubject("主题");
            message.setText("文本信息 : 我在测试2");
            message.setFrom(new InternetAddress(account));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(account));

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }


        System.out.println("成功");
    }
}
