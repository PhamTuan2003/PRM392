package com.vishwajeeth.medicinetime.alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.vishwajeeth.medicinetime.R;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
                String pillName = intent.getStringExtra("pillName");
                int userProfileId = intent.getIntExtra("userProfileId", -1);
                String memberName = intent.getStringExtra("memberName");
                String time = intent.getStringExtra("time");
                int alarmId = intent.getIntExtra("alarmId", 0);

                android.util.Log.d("DEBUG_ALARM",
                                "AlarmReceiver onReceive! memberName=" + memberName + ", pillName=" + pillName);

                // Tạo notification channel nếu cần
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(
                                        "medicinenotify",
                                        "Nhắc nhở uống thuốc",
                                        NotificationManager.IMPORTANCE_HIGH);
                        channel.enableVibration(true);
                        channel.setVibrationPattern(new long[] { 0, 500, 500, 500 });
                        NotificationManager manager = context.getSystemService(NotificationManager.class);
                        manager.createNotificationChannel(channel);
                }

                // Intent mở app khi bấm vào notification
                Intent openAppIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                PendingIntent pendingIntent = PendingIntent.getActivity(context, alarmId, openAppIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                String content = "Thành viên: " + (memberName != null ? memberName : userProfileId) + "\nThuốc: "
                                + pillName
                                + (time != null ? ("\nGiờ: " + time) : "");

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medicinenotify")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Nhắc nhở uống thuốc")
                                .setContentText(content)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setVibrate(new long[] { 0, 500, 500, 500 })
                                .setContentIntent(pendingIntent);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(alarmId, builder.build());
        }
}