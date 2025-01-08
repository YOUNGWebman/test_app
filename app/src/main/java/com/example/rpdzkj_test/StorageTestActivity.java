package com.example.rpdzkj_test;

import com.example.rpdzkj_test.TimeDisplay;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.security.keystore.StrongBoxUnavailableException;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Color;
import com.example.rpdzkj_test.TestInfo;


public class StorageTestActivity extends AppCompatActivity  {
    private static final String TAG = "StorageActivity";
    private TextView sdStatusTextView;
    private TextView UsbStatusTextView;
    private TextView sataStatusTextView;
    private TextView M2StatusTextView;

    private TextView sdUsedSpaceTextView;
    private TextView udiskUsedSpaceTextView;
    private TextView sataUsedSpaceTextView;
    private TextView m2UsedSpaceTextView;

    private TextView sdCheckView;
    private TextView udiskCheckView;
    private TextView sataCheckView;
    private TextView m2CheckView;
    private Runnable checkStorageTask;
    private boolean timerEnabled;
    private int timeInSeconds;
    private TimeDisplay timeDisplay;
    private TextView getTimeTextView;
    private long usedSpace = 0;
    private long totalSpace = 0;
    private Handler handler = new Handler();

    private AtomicBoolean isTimerRunning = new AtomicBoolean(false); // 用于检查定时器状态
    private AtomicBoolean  shouldPause = new AtomicBoolean(false); // 标志是否应暂停
    private TestInfo testInfo;


    public void onCreate(Bundle paramBundle){
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_storage_test);
        sdUsedSpaceTextView = findViewById(R.id.sd_used_space_text_view);
        sataUsedSpaceTextView = findViewById(R.id.sata_used_space_text_view);
        udiskUsedSpaceTextView = findViewById(R.id.udisk_used_space_text_view);
        m2UsedSpaceTextView = findViewById(R.id.m2_used_space_text_view);

        sdCheckView = findViewById(R.id.sd_check_status_text_view);
        udiskCheckView = findViewById(R.id.udisk_check_status_text_view);
        sataCheckView = findViewById(R.id.sata_check_status_text_view);
        m2CheckView = findViewById(R.id.m2_check_status_text_view);
        sdStatusTextView = findViewById(R.id.sd_status_text_view);
        sataStatusTextView = findViewById(R.id.sata_status_text_view);
        UsbStatusTextView = findViewById(R.id.usb_status_text_view);
        M2StatusTextView = findViewById(R.id.M2_status_text_view);

        getTimeTextView = findViewById(R.id.testTime);
        timeDisplay = new TimeDisplay(getTimeTextView);
        timeDisplay.start();
        testInfo = new TestInfo(this);
        Intent intent = getIntent();
        timerEnabled = intent.getBooleanExtra("TIMER", false);
        timeInSeconds = intent.getIntExtra("TIME_IN_SECONDS", 0);
        File saveFile = new File(getFilesDir(), "saved_ids.html");
        testInfo.setSavedFile(saveFile);

        Button sdTestButton = findViewById(R.id.sd_test_button);
        sdTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sdTestButton.setEnabled(false);
                sdTestButton.setBackgroundColor(Color.LTGRAY);
                setSystemProperty("sdcard_test",  "1");
                // 添加延迟
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Boolean sdflag = Boolean.parseBoolean(getSystemProperty("rp.sdcard.storage.flag"));
                        if(sdflag)
                        {
                            sdStatusTextView.setText("SD已挂载");

                        }else {
                            sdStatusTextView.setText("SD未挂载或未格式化");
                            setSystemProperty("sdcard_test",  "0");
                            return;
                        }
                        handler.post(checkStorageTask);

                        checkStorageTask = new Runnable() {
                            @Override
                            public void run() {
                                // 检查存储状态
                                Boolean sderrflag = Boolean.parseBoolean(getSystemProperty("rp.sdcard.rw.err"));
                                if(sderrflag)
                                {
                                    sdTestButton.setBackgroundColor(Color.RED);
                                    timeDisplay.pause(); // 停止时间
                                    shouldPause.set(true);
                                }
                                String executionCount = getSystemProperty("rp.sdcard.rw.count");
                                Log.d(TAG, "脚本执行次数：" + executionCount);
                                sdCheckView.setText("测试次数: " + executionCount);
                                // 1秒后再次执行此任务
                                handler.postDelayed(this, 1000);

                                // 获取存储总容量和已使用容量
                                String totalStorageStr = getSystemProperty("rp.sdcard.storage.total");
                                String usedStorageStr = getSystemProperty("rp.sdcard.storage.used");

                                // 将存储容量从字节转换为GB
                                double totalStorage = Double.parseDouble(totalStorageStr) / 1000000;
                                double usedStorage = Double.parseDouble(usedStorageStr) / 1000000;

                                // 更新used1_space_text_view的文本
                                // TextView used1SpaceTextView = findViewById(R.id.used_space_text_view);
                                sdUsedSpaceTextView.setText("已使用容量: " + String.format("%.2f", usedStorage) + " GB\\总容量: " + String.format("%.2f", totalStorage) + " GB");
                            }
                        };
// 启动检查任务
                        handler.post(checkStorageTask);

                    }
                }, 500);  // 延迟1秒
            }
        });

        Button UsbTestButton = findViewById(R.id.usb_test_button);
        UsbTestButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                UsbTestButton.setEnabled(false);
                UsbTestButton.setBackgroundColor(Color.LTGRAY);
                setSystemProperty("udisk_test",  "1");
                // 添加延迟
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Boolean udiskflag = Boolean.parseBoolean(getSystemProperty("rp.udisk.storage.flag"));
                        if(udiskflag)
                        {
                            UsbStatusTextView.setText("U盘已挂载");
                        }else {
                            UsbStatusTextView.setText("U盘未挂载或未格式化");
                            setSystemProperty("udisk_test",  "0");
                            return;
                        }

                        // 启动检查任务
                        handler.post(checkStorageTask);
                    }
                }, 500);  // 延迟1秒

                // 初始化checkStorageTask
                checkStorageTask = new Runnable() {
                    @Override
                    public void run() {
                        // 检查存储状态
                        Boolean uderrflag = Boolean.parseBoolean(getSystemProperty("rp.udisk.rw.err"));
                        if(uderrflag)
                        {
                            UsbTestButton.setBackgroundColor(Color.RED);
                            timeDisplay.pause(); // 停止时间
                            shouldPause.set(true);
                        }
                        String executionCount = getSystemProperty("rp.udisk.rw.count");
                        Log.d(TAG, "脚本执行次数：" + executionCount);
                        udiskCheckView.setText("测试次数: " + executionCount);
                        // 1秒后再次执行此任务
                        handler.postDelayed(this, 1000);

                        // 获取存储总容量和已使用容量
                        String totalStorageStr = getSystemProperty("rp.udisk.storage.total");
                        String usedStorageStr = getSystemProperty("rp.udisk.storage.used");

                        // 将存储容量从字节转换为GB
                        double totalStorage = Double.parseDouble(totalStorageStr) / 1000000;
                        double usedStorage = Double.parseDouble(usedStorageStr) / 1000000;

                        // 更新used1_space_text_view的文本
                        //   TextView used1SpaceTextView = findViewById(R.id.used1_space_text_view);
                        udiskUsedSpaceTextView.setText("已使用容量: " + String.format("%.2f", usedStorage) + " GB\\总容量: " + String.format("%.2f", totalStorage) + " GB");
                    }
                };


            }
        });
        Button sataTestButton = findViewById(R.id.sata_test_button);
        sataTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sataTestButton.setEnabled(false);
                sataTestButton.setBackgroundColor(Color.LTGRAY);
                setSystemProperty("sata_test",  "1");
                // 添加延迟
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Boolean sataflag = Boolean.parseBoolean(getSystemProperty("rp.sata.storage.flag"));
                        if(sataflag)
                        {
                            sataStatusTextView.setText("SATA已挂载");
                        }else {
                            sataStatusTextView.setText("SATA未挂载或未格式化");
                            setSystemProperty("sata_test",  "0");
                            return;
                        }
                        handler.post(checkStorageTask);

                        checkStorageTask = new Runnable() {
                            @Override
                            public void run() {
                                // 检查存储状态
                                Boolean sterrflag = Boolean.parseBoolean(getSystemProperty("rp.sata.rw.err"));
                                if(sterrflag)
                                {
                                    sataTestButton.setBackgroundColor(Color.RED);
                                    timeDisplay.pause(); // 停止时间
                                    shouldPause.set(true);
                                }
                                String executionCount = getSystemProperty("rp.sata.rw.count");
                                Log.d(TAG, "脚本执行次数：" + executionCount);
                                sataCheckView.setText("测试次数: " + executionCount);
                                // 1秒后再次执行此任务
                                handler.postDelayed(this, 1000);

                                // 获取存储总容量和已使用容量
                                String totalStorageStr = getSystemProperty("rp.sata.storage.total");
                                String usedStorageStr = getSystemProperty("rp.sata.storage.used");

                                // 将存储容量从字节转换为GB
                                double totalStorage = Double.parseDouble(totalStorageStr) / 1000000;
                                double usedStorage = Double.parseDouble(usedStorageStr) / 1000000;

                                // 更新used1_space_text_view的文本
                                sataUsedSpaceTextView.setText("已使用容量: " + String.format("%.2f", usedStorage) + " GB\\总容量: " + String.format("%.2f", totalStorage) + " GB");
                            }
                        };
                        // 启动检查任务
                        handler.post(checkStorageTask);
                    }
                }, 500);  // 延迟1秒
            }
        });
        Button M2TestButton = findViewById(R.id.m2_test_button);
        M2TestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                M2TestButton.setEnabled(false);
                M2TestButton.setBackgroundColor(Color.LTGRAY);
                setSystemProperty("m2_test",  "1");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Boolean m2flag = Boolean.parseBoolean(getSystemProperty("rp.m2.storage.flag"));
                        if(m2flag)
                        {
                            M2StatusTextView.setText("M2已挂载");
                        }else {
                            M2StatusTextView.setText("M2未挂载或未格式化");
                            setSystemProperty("m2_test",  "0");
                            return;
                        }
                        handler.post(checkStorageTask);

                        checkStorageTask = new Runnable() {
                            @Override
                            public void run() {
                                // 检查存储状态
                                Boolean m2errflag = Boolean.parseBoolean(getSystemProperty("rp.m2.rw.err"));
                                if(m2errflag)
                                {
                                    M2TestButton.setBackgroundColor(Color.RED);
                                    timeDisplay.pause(); // 停止时间
                                    shouldPause.set(true);
                                }
                                String executionCount = getSystemProperty("rp.m2.rw.count");
                                Log.d(TAG, "脚本执行次数：" + executionCount);
                                m2CheckView.setText("测试次数: " + executionCount);
                                // 1秒后再次执行此任务
                                handler.postDelayed(this, 1000);

                                // 获取存储总容量和已使用容量
                                String totalStorageStr = getSystemProperty("rp.m2.storage.total");
                                String usedStorageStr = getSystemProperty("rp.m2.storage.used");

                                // 将存储容量从字节转换为GB
                                double totalStorage = Double.parseDouble(totalStorageStr) / 1000000;
                                double usedStorage = Double.parseDouble(usedStorageStr) / 1000000;

                                // 更新used1_space_text_view的文本
                                // TextView used1SpaceTextView = findViewById(R.id.used1_space_text_view);
                                m2UsedSpaceTextView.setText("已使用容量: " + String.format("%.2f", usedStorage) + " GB\\总容量: " + String.format("%.2f", totalStorage) + " GB");
                            }
                        };
// 启动检查任务
                        handler.post(checkStorageTask);


                    }
                }, 500);  // 延迟1秒
            }
        });

        Button stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sdTestButton.setEnabled(true);
                sdTestButton.setBackgroundColor(Color.BLUE);
                sataTestButton.setEnabled(true);
                sataTestButton.setBackgroundColor(Color.BLUE);
                UsbTestButton.setEnabled(true);
                UsbTestButton.setBackgroundColor(Color.BLUE);
                M2TestButton.setEnabled(true);
                M2TestButton.setBackgroundColor(Color.BLUE);


                if (checkStorageTask != null) {
                    // 移除消息队列中所有的checkStorageTask任务
                    handler.removeCallbacksAndMessages(null);
                    checkStorageTask = null;
                }


                setProperty("sdcard_test", "0");
                setProperty("udisk_test", "0");
                setProperty("sata_test", "0");
                setProperty("m2_test", "0");
                // 清除statusTextView的文本
                sdStatusTextView.setText("SD状态");
                sdUsedSpaceTextView.setText("已使用容量/总容量");
                sdCheckView.setText("测试次数：");
                setSystemProperty("rp.sdcard.rw.count" , "0");
                sataStatusTextView.setText("SATA状态");
                sataUsedSpaceTextView.setText("已使用容量/总容量");
                sataCheckView.setText("测试次数：");
                setSystemProperty("rp.sata.rw.count" , "0");
                UsbStatusTextView.setText("U盘状态");
                udiskUsedSpaceTextView.setText("已使用容量/总容量");
                udiskCheckView.setText("测试次数：");
                setSystemProperty("rp.udisk.rw.count" , "0");
                M2StatusTextView.setText("M2状态");
                m2UsedSpaceTextView.setText("已使用容量/总容量");
                m2CheckView.setText("测试次数：");
                setSystemProperty("rp.m2.rw.count" , "0");
            }
        });

        if (timerEnabled) {
            int timeInMillis = timeInSeconds * 1000;
            stopButton.performClick();
            try {
                Thread.sleep(1000);
                sdTestButton.performClick();

                Thread.sleep(1000);
                UsbTestButton.performClick();

                Thread.sleep(1000);
                sataTestButton.performClick();

                Thread.sleep(1000);
                M2TestButton.performClick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 启动倒计时器，定时退出
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("TIMER", true);
                    resultIntent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    if( shouldPause.get() || isFinishing())
                    {resultIntent.putExtra("TEST_PASS", false);}
                    else
                    {resultIntent.putExtra("TEST_PASS", true);}
                    setResult(RESULT_OK, resultIntent);
                    // 添加标志位检查并设置是否需要停止
                 /*   if (!shouldPause.get() && !isFinishing()) {
                        stopButton.performClick();
                        finish();
                    } */
                    String sdStatus = sdStatusTextView.getText().toString();
                    String sataStatus = sataStatusTextView.getText().toString();
                    String udiskStatus = UsbStatusTextView.getText().toString();
                    String m2Status = M2StatusTextView.getText().toString();
                    String sdTimes = sdCheckView.getText().toString();
                    String sataTimes = sataCheckView.getText().toString();
                    String udiskTimes = udiskCheckView.getText().toString();
                    String m2Times = m2CheckView.getText().toString();
                    testInfo.appendAdditionalStorageContent(sdStatus, sataStatus, udiskStatus, m2Status, sdTimes, sataTimes, udiskTimes, m2Times);
                    stopButton.performClick();
                    finish();
                }
            }, timeInMillis);
        }

    }

    public static int setProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Error setting property", e);
            return -1;
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent(StorageTestActivity.this, MainActivity.class);
            setResult(RESULT_OK, intent); // 返回结果
            finish(); // 确保调用 finish() 方法
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private String getSystemProperty(String propertyName) {
        Process process = null;
        BufferedReader reader = null;
        try {
            process = new ProcessBuilder("getprop", propertyName).start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    private void setSystemProperty(String propertyName, String propertyValue) {
        Process process = null;
        try {
            process = new ProcessBuilder("setprop", propertyName, propertyValue).start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
