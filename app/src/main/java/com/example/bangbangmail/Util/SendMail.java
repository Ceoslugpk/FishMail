package com.example.bangbangmail.Util;

/**
 * Created by Kenshin on 2017/5/26.
 */


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class SendMail
{
    public static String mailId;
    public static String date;
    // 指定发送邮件的主机
    private final static String host = "120.77.168.57";
    private static final String TAG = "SendMail";

    public static boolean send(String to, String subject, String content) throws MessagingException
    {
        boolean  result = false;
        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", host);

        // 获取默认session对象
        properties.put("mail.smtp.auth", "true");

        Log.d("发送邮件", "send: " +  FishMailApplication.getMail() + FishMailApplication.getPwd());

        String from = FishMailApplication.getMail();

        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties,new Authenticator(){
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(FishMailApplication.getMail(), FishMailApplication.getPwd()); //发件人邮件用户名、密码
            }
        });
        try{
            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(session);

            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));

            // Set To: 头部头字段
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));
            message.setSentDate(new Date());

            Log.d(TAG, "From:" + from);
            Log.d(TAG, "To:" + to );
            Log.d(TAG, "主题：" + subject);
            Log.d(TAG, "内容：" + content);

            //加密后设置主题
            String encryptSubject = Base64.encode(subject.getBytes());
            message.setSubject(encryptSubject);

            // 加密后设置消息体
            String encryptContent = Base64.encode(content.getBytes());
            message.setText(encryptContent);

            Log.d(TAG, "加密后主题：" + encryptSubject);
            Log.d(TAG, "加密后内容：" + encryptContent);

            // 发送消息
            try{
                Log.d(TAG, "send: 调用发送方法");
                Transport.send(message);
                mailId = message.getMessageID();
                date = message.getSentDate().toString();
//                Log.d(TAG, "send: date:" + date);
//                date = date.substring(0, date.indexOf("+") - 1);
                Log.d(TAG, "send: date:" + date);
                SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = sdf2.format(sdf1.parse(date));
                Log.d(TAG, "send: MailID:" + mailId);
                result = true;
            }catch(Exception e){
                result = false;
            }

        }catch (MessagingException mex) {
            mex.printStackTrace();
        }
        return result;
    }
}
