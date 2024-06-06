package com.example.rpdzkj_test;

import android.os.Handler;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeDisplay {
    private TextView timeTextView;
    private Handler handler = new Handler();
    private Runnable runnable;
    private long startTime;

    public TimeDisplay(TextView textView) {
        this.timeTextView = textView;
    }

    public void start() {
        startTime = System.nanoTime();
        runnable = new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.nanoTime() - startTime;
                // 将纳秒转换为毫秒
                long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedTime);
                // 格式化显示时间
                String time = formatElapsedTime(elapsedMillis);
                timeTextView.setText("当前运行时间： " + time);

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(runnable);
    }

    public void stop() {
        handler.removeCallbacks(runnable);
        timeTextView.setText("当前运行时间： 00:00:00");
    }

    private String formatElapsedTime(long elapsedMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMillis));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedMillis));
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

}
