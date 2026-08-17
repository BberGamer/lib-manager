package controller;

import service.AutoReminderService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ServletContextListener tự động khởi chạy và quản lý ScheduledExecutorService
 * Đặt lịch chạy Batch Job quét mượn trả, tính phạt và gửi email nhắc nhở lúc 07:00 sáng hàng ngày.
 */
@WebListener
public class NotificationSchedulerListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;
    private final AutoReminderService autoReminderService = new AutoReminderService();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[NotificationSchedulerListener] Khởi động hệ thống lập lịch tự động FPT Library Scheduler...");
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Library-AutoReminder-Thread");
            t.setDaemon(true);
            return t;
        });

        // Tính toán độ trễ (delay) đến 07:00 sáng tiếp theo
        long initialDelayMinutes = computeInitialDelayToTargetHour(7, 0);

        // Đặt lịch chạy định kỳ mỗi 24 giờ (1440 phút)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                autoReminderService.runBatchReminder("SYSTEM_SCHEDULER");
            } catch (Exception e) {
                System.err.println("[NotificationSchedulerListener] Lỗi trong quá trình thực thi Batch Job: " + e.getMessage());
            }
        }, initialDelayMinutes, 24 * 60, TimeUnit.MINUTES);

        System.out.println("[NotificationSchedulerListener] Lập lịch thành công. Batch Job tiếp theo sẽ kích hoạt sau: " 
                + initialDelayMinutes + " phút (Lúc 07:00 sáng).");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[NotificationSchedulerListener] Đang dừng hệ thống lập lịch tự động...");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Tính số phút từ hiện tại cho tới mốc giờ mục tiêu (ví dụ 07:00 sáng)
     */
    private long computeInitialDelayToTargetHour(int targetHour, int targetMinute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = now.with(LocalTime.of(targetHour, targetMinute, 0));

        // Nếu thời điểm mục tiêu trong ngày hôm nay đã qua, đặt sang 07:00 ngày mai
        if (now.compareTo(targetTime) >= 0) {
            targetTime = targetTime.plusDays(1);
        }

        Duration duration = Duration.between(now, targetTime);
        return Math.max(1, duration.toMinutes());
    }
}
