package service;

import dao.NotificationDAO;
import model.Notification;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class NotificationService {

    private final NotificationDAO notificationDAO = new NotificationDAO();
    
    private static final String from = "";
    private static final String password = "";

    public boolean createAndSendNotification(int userId, String title, String message, String type, Integer refId, String refType, String emailTo) {
        try {
            // 1. Save to DB
            Notification notif = new Notification(userId, title, message, type);
            notif.setReferenceId(refId);
            notif.setReferenceType(refType);
            notificationDAO.createNotification(notif);
            
            // 2. Send email in a separate thread
            if (emailTo != null && !emailTo.trim().isEmpty()) {
                new Thread(() -> {
                    sendEmail(emailTo, title, message);
                }).start();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendEmail(String to, String subject, String text) {
        if (from == null || from.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return; // Bỏ qua gửi email nếu thông tin tài khoản bị bỏ rỗng
        }
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        };
        
        Session session = Session.getInstance(props, auth);
        MimeMessage msg = new MimeMessage(session);
        
        try {
            msg.addHeader("Content-type", "text/html; charset=UTF-8");
            msg.setFrom(new InternetAddress(from, "FPT Library System"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            msg.setSubject(subject, "UTF-8");
            
            // Format HTML email nicely
            String content = "<h3>FPT Library Notification</h3>"
                    + "<p>" + text.replace("\n", "<br/>") + "</p>"
                    + "<hr/><p style='font-size:0.8rem;color:#7f8c8d;'>Đây là email tự động từ Hệ thống Quản lý Thư viện FPT Library. Vui lòng không trả lời email này.</p>";
            msg.setContent(content, "text/html; charset=UTF-8");
            Transport.send(msg);
            System.out.println("Notification email sent to " + to);
        } catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
        }
    }
}
