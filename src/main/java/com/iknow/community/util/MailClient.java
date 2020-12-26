package com.iknow.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 发送邮件工具类
 */
@Component
public class MailClient {

    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
    @Autowired
    JavaMailSender mailSender;
    // 获取配置文件中的username
    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to,String subject,String content){
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content,true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败："+e.getMessage());
        }
    }
}
