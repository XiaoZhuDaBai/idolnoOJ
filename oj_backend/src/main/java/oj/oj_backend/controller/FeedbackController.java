package oj.oj_backend.controller;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import oj.oj_backend.comm.ResponseResult;
import oj.oj_backend.model.vo.EmailVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/6/15 22:26
 */

@RestController
@RequestMapping("/feedback")
public class FeedbackController {
    @Value("${feedback.email.account}")
    private String account;
    @Value("${feedback.email.auth}")
    private String auth;

    @PostMapping("/email")
    public ResponseResult<String> sendFeedbackToMyEmail(@RequestBody EmailVo emailVo) {
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

        try {
            message.setSubject(emailVo.getTitle());

            // markdown 转 html
            Parser parser = Parser.builder().build();
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            Node document = parser.parse(emailVo.getContent());
            String html = renderer.render(document);
            // 用 HTML 格式发送
            message.setContent(html, "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(account));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(account));

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("发送失败" + e);
        }

        return ResponseResult.success("发送成功");
    }

    @PostMapping("/upload")
    public ResponseResult<String> uploadFeedback(@RequestBody String formData) {

        return ResponseResult.success("上传成功");
    }
}
