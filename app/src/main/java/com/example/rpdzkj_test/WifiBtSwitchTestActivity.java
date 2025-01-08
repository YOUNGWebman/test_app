package com.example.rpdzkj_test;

import com.example.rpdzkj_test.TimeDisplay;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.security.keystore.StrongBoxUnavailableException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.provider.Settings;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import android.graphics.Color;
import java.util.concurrent.atomic.AtomicBoolean;
import com.example.rpdzkj_test.TestInfo;

public class WifiBtSwitchTestActivity extends AppCompatActivity {
    private WifiManager wifiManager;
    private BluetoothAdapter bluetoothAdapter;
    private int wifiEnabledCount = 0;
    private int bluetoothEnabledCount = 0;
    private BroadcastReceiver wifiStateReceiver;
    private BroadcastReceiver bluetoothStateReceiver;
    private TextView wifiCountTextView;
    private TextView bluetoothCountTextView;
    private boolean wifiEnabled = false;
    private TimeDisplay timeDisplay;
    private TextView getTimeTextView;
    private Timer timer;
    private static final String TAG = "WifiBtSwitchTestActivity";
    private static final int DELAY_MS = 2000;  // 延迟2秒
    private boolean timerEnabled;
    private int timeInSeconds;
    private TestInfo testInfo;

    private AtomicBoolean  shouldPause = new AtomicBoolean(false); // 标志是否应暂停



    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_wifi_bt_switch_test);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();
        timerEnabled = intent.getBooleanExtra("TIMER", false);
        timeInSeconds = intent.getIntExtra("TIME_IN_SECONDS", 0);

        getTimeTextView = findViewById(R.id.testTime);
        timeDisplay = new TimeDisplay(getTimeTextView);
        timeDisplay.start();
        testInfo = new TestInfo(this);
        Button wifiButton = findViewById(R.id.wifi_button);
        File saveFile = new File(getFilesDir(), "saved_ids.html");
        testInfo.setSavedFile(saveFile);
        wifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiButton.setBackgroundColor(Color.LTGRAY);
                setProperty("wifi_test", "1");
                wifiEnabledCount = 0;
                wifiCountTextView.setText("WiFi打开成功的次数: " + wifiEnabledCount);
                wifiButton.setEnabled(false);
                wifiEnabled = !wifiEnabled;
                // setProperty("wifi_test", wifiEnabled ? "1" : "0");
            }
        });

        Button bluetoothButton = findViewById(R.id.bluetooth_button);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothButton.setBackgroundColor(Color.LTGRAY);
                bluetoothEnabledCount = 0;
                bluetoothCountTextView.setText("蓝牙打开成功的次数: " + bluetoothEnabledCount);
                bluetoothButton.setEnabled(false);
                if (bluetoothAdapter != null) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (bluetoothAdapter.isEnabled()) {
                                // 关闭蓝牙
                                bluetoothAdapter.disable();
                            } else {
                                // 开启蓝牙
                                bluetoothAdapter.enable();
                            }
                        }
                    }, 0, DELAY_MS);
                }
            }
        });

        wifiCountTextView = findViewById(R.id.wifi_count_text_view);
        bluetoothCountTextView = findViewById(R.id.bluetooth_count_text_view);

        wifiStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if(!wifiButton.isEnabled()) {
                    switch (wifiStateExtra) {
                        case WifiManager.WIFI_STATE_ENABLED:
                            wifiEnabledCount++;
                            wifiCountTextView.setText("WiFi打开成功的次数: " + wifiEnabledCount);
                            break;
                        case WifiManager.WIFI_STATE_DISABLED:
                            // WiFi已关闭
                            break;
                        case WifiManager.WIFI_STATE_UNKNOWN:
                            // WiFi启动失败，将按钮颜色设置为红色
                            wifiButton.setBackgroundColor(Color.RED);
                            timeDisplay.pause();
                            shouldPause.set(true);
                            break;
                        default:
                            break;
                    }
                }
            }
        };

        registerReceiver(wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        bluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (!bluetoothButton.isEnabled())
                    {
                        switch (state) {
                            case BluetoothAdapter.STATE_ON:
                                bluetoothEnabledCount++;
                                bluetoothCountTextView.setText("蓝牙打开成功的次数: " + bluetoothEnabledCount);
                                break;
                            case BluetoothAdapter.STATE_OFF:
                                // 蓝牙已关闭
                                break;
                            case BluetoothAdapter.ERROR:
                                bluetoothButton.setBackgroundColor(Color.RED);
                                timeDisplay.pause();
                                shouldPause.set(true);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        };

        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        Button stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiButton.setEnabled(true);
                wifiButton.setBackgroundColor(Color.BLUE);
                bluetoothButton.setEnabled(true);
                bluetoothButton.setBackgroundColor(Color.BLUE );

                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                wifiEnabled = false;
                setProperty("wifi_test", "0");
            }
        });

        if (timerEnabled) {
            int timeInMillis = timeInSeconds * 1000;
            wifiButton.performClick();
            bluetoothButton.performClick();
            // 启动倒计时器，定时退出
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("TIMER", true);
                    resultIntent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    if( shouldPause.get())
                    {resultIntent.putExtra("TEST_PASS", false);}
                    else
                    {resultIntent.putExtra("TEST_PASS", true);}
                    setResult(RESULT_OK, resultIntent);
                    stopButton.performClick();
                   /* if (!shouldPause.get() && !isFinishing()) {
                        stopButton.performClick();
                        finish();
                    } */
                    String WifiSwTimes = wifiCountTextView.getText().toString();
                    String BtSwTimes = bluetoothCountTextView.getText().toString();
                    testInfo.appendAdditionalWifiBtSwContent(WifiSwTimes, BtSwTimes);
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
            Intent intent = new Intent(WifiBtSwitchTestActivity.this, MainActivity.class);
            setResult(RESULT_OK, intent); // 返回结果
            finish(); // 确保调用 finish() 方法
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiStateReceiver);
        unregisterReceiver(bluetoothStateReceiver);
    }

}
