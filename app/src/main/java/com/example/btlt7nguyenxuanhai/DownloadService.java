package com.example.btlt7nguyenxuanhai;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends Service {

    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_ID = 1;

    private boolean isCancelled = false;
    private boolean isPaused = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String fileUrl = intent.getStringExtra("url");
        if (fileUrl == null || fileUrl.isEmpty()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getBaseNotification("Đang chuẩn bị tải...", 0).build());

        new Thread(() -> downloadFile(fileUrl)).start();

        return START_STICKY;
    }

    private void downloadFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            int totalLength = connection.getContentLength();
            if (totalLength <= 0) totalLength = 1;

            // ✅ Lưu vào thư mục "Download" mặc định có sẵn trên mọi máy ảo
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "downloaded_file.pdf");
            FileOutputStream output = new FileOutputStream(file);
            Log.d("DownloadService", "Đang lưu file tại: " + file.getAbsolutePath());

            InputStream input = new BufferedInputStream(connection.getInputStream());
            byte[] data = new byte[1024];
            int count;
            int total = 0;

            while ((count = input.read(data)) != -1) {
                if (isCancelled) {
                    Log.d("DownloadService", "Đã hủy tải");
                    break;
                }
                while (isPaused) Thread.sleep(500);

                total += count;
                output.write(data, 0, count);

                int progress = (int) ((total * 100L) / totalLength);
                updateNotification("Đang tải...", progress);
            }

            output.flush();
            output.close();
            input.close();

            if (!isCancelled) {
                showCompletedNotification(file.getAbsolutePath());
            }

            stopForeground(true);
            stopSelf();

        } catch (Exception e) {
            Log.e("DownloadService", "Lỗi tải: " + e.getMessage());
            stopSelf();
        }
    }

    private NotificationCompat.Builder getBaseNotification(String title, int progress) {
        Intent cancelIntent = new Intent(this, DownloadReceiver.class);
        cancelIntent.setAction("ACTION_CANCEL");
        PendingIntent pCancel = PendingIntent.getBroadcast(this, 1, cancelIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText("Tiến trình: " + progress + "%")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(100, progress, false)
                .addAction(android.R.drawable.ic_delete, "Cancel", pCancel)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
    }

    private void updateNotification(String title, int progress) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, getBaseNotification(title, progress).build());
    }

    private void showCompletedNotification(String filePath) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tải xuống hoàn tất ✅")
                .setContentText("File lưu tại: " + filePath)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(2, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Download Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
