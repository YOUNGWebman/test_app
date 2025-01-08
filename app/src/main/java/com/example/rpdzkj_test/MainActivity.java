package com.example.rpdzkj_test;

import com.example.rpdzkj_test.TimeDisplay;
import com.example.rpdzkj_test.TestInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
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
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.LayerDrawable;


import androidx.annotation.Nullable;
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
import android.widget.NumberPicker;
import android.widget.Toast;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;




import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import android.content.pm.ResolveInfo;

import android.net.ConnectivityManager;
import android.widget.Toast;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.content.ComponentName;
import android.content.ActivityNotFoundException;
import java.util.ArrayList;
import android.os.Build;
import java.util.concurrent.TimeUnit;
import android.content.SharedPreferences;
import android.os.Environment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.FrameLayout;







public class MainActivity extends AppCompatActivity {
    private Handler handler = new Handler();
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private NumberPicker hoursPicker, minutesPicker, secondsPicker;
    private  Runnable runnable;
    public static boolean isRunning = false;
    private TimeDisplay timeDisplay;
    private final int REQUEST_PERMISSION_CODE = 1001;

    private TextView getTimeTextView;
    private TextView cpuIdInput;
    private EditText pcbIdInput;
    private TextView snIdInput;

    private Button StorageButton;
    private Button WifiBtButton;
    private Button wifiStatusButton;
    private Button ethButton;
    private Button uartButton;
    private Button canButton;
    private Button spiButton;
    private Button saveIdButton;
    private Button testResultButton;
    private Button uploadTestResultButton;

    private TestInfo testInfo;

    private static final int REQUEST_CODE_0 = 0;
    private static final int REQUEST_CODE_1 = 1;
    private static final int REQUEST_CODE_2 = 2;
    private static final int REQUEST_CODE_3 = 3;
    private static final int REQUEST_CODE_4 = 4;
    private static final int REQUEST_CODE_5 = 5;
    private static final int REQUEST_CODE_6 = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         initPermission();
        // 尝试提升权限
        boolean success = upgradeRootPermission(getPackageCodePath());
        if (success) {
            Log.d(TAG, "Successfully upgraded root permission");
        } else {
            Log.e(TAG, "Failed to upgrade root permission");
        }




        getTimeTextView = findViewById(R.id.get_time_text_view);
        hoursPicker = findViewById(R.id.hours_picker);
        minutesPicker = findViewById(R.id.minutes_picker);
        secondsPicker = findViewById(R.id.seconds_picker);


        // 设置 NumberPicker 的最小值和最大值
        // 定义自定义显示的小时值
      /*  String[] displayedValues = {"0", "1", "6", "12", "24", "48"};
        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(displayedValues.length - 1);
        hoursPicker.setDisplayedValues(displayedValues); */
        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(48);
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);

        // 设置格式化器
        NumberPicker.Formatter formatter = value -> String.format("%02d", value);
        hoursPicker.setFormatter(formatter);
        minutesPicker.setFormatter(formatter);
        secondsPicker.setFormatter(formatter);




        // 检查蓝牙权限
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_ADMIN},
                    PERMISSION_REQUEST_CODE);
        }

        // 检查 WiFi 权限
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_WIFI_STATE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_WIFI_STATE},
                    PERMISSION_REQUEST_CODE);
        }

        // 检查外部存储权限
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }


        Button DVTestButton = findViewById(R.id.double_video_button);
        DVTestButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startApp("com.xbn.doublevideoplay");
            }

        });


        Button MCameraTestButton = findViewById(R.id.multi_camera_button);
        MCameraTestButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startNApp(MainActivity.this, "com.example.cam");
            }

        });


        Button CameraTestButton = findViewById(R.id.camera_test_button);
        CameraTestButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startAppWithShellCommand("com.cghs.stresstest" , "com.cghs.stresstest.test.CameraTest");

            }
        });

        Button RebootTestButton = findViewById(R.id.reboot_test_button);
        RebootTestButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                startAppWithShellCommand("com.cghs.stresstest" , "com.cghs.stresstest.test.RebootTest");
            }

        });

        if(getSystemProperty("ro.product.brand").equals("Allwinner"))
        {
            CameraTestButton.setVisibility(View.GONE);
            RebootTestButton.setVisibility(View.GONE);
        }


        StorageButton = findViewById(R.id.Storage_button);
        StorageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StorageTestActivity.class);
                startActivity(intent);
            }
        });

        wifiStatusButton = findViewById(R.id.wifi_status_test_button);
        wifiStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WifiTestActivity.class);
                startActivity(intent);
            }
        });

        ethButton = findViewById(R.id.ethernet_status_test_button);
        ethButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NetworkTestActivity.class);
                startActivity(intent);
            }
        });


        uartButton = findViewById(R.id.uart_test_button);
        uartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UartTestActivity.class);
                startActivity(intent);
            }
        });


        canButton = findViewById(R.id.can_test_button);
        canButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: button clicked");
                Drawable background = canButton.getBackground();
                if (background instanceof ColorDrawable) {
                    int color = ((ColorDrawable) background).getColor();
                    // Do something with the color
                    Log.d(TAG, "onClick: color is" + color );
                }
                Intent intent = new Intent(MainActivity.this, CanTestActivity.class);
                startActivity(intent);
            }
        });

        spiButton = findViewById(R.id.spi_test_button);
        spiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SpiTestActivity.class);
                startActivity(intent);
            }
        });


        Button cameraButton = findViewById(R.id.camera_display_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraTestActivity.class);
                startActivity(intent);
            }
        });

        Button startTime = findViewById(R.id.start_time_button);
        timeDisplay = new TimeDisplay(getTimeTextView);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime.setEnabled(false);
                timeDisplay.start();

            }
        });

        WifiBtButton = findViewById(R.id.Wifi_Bt_button);
        WifiBtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WifiBtSwitchTestActivity.class);
               // intent.putExtra("TIMER", false); // 不启动定时器
                startActivity(intent);
            }
        });



      /*  Button autoTestButton = findViewById(R.id.auto_test_button);
        autoTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoTestButton.setEnabled(false);
                int hours = hoursPicker.getValue();
                int minutes = minutesPicker.getValue();
                int seconds = secondsPicker.getValue();
                int timeInSeconds = hours * 3600 + minutes * 60 + seconds;
                int timeInMillis = timeInSeconds * 1000;


                Intent intent = new Intent(MainActivity.this, UartTestActivity.class);
                intent.putExtra("TIMER", true); // 传递是否启动定时器
                intent.putExtra("TIME_IN_SECONDS", timeInSeconds); // 传递定时时间
                startActivityForResult(intent, REQUEST_CODE_0);

                // Use CountDownTimer for countdown
                new CountDownTimer(timeInMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        int remainingHours = (int) (millisUntilFinished / 3600000);
                        int remainingMinutes = (int) (millisUntilFinished % 3600000) / 60000;
                        int remainingSeconds = (int) (millisUntilFinished % 60000) / 1000;
                        hoursPicker.setValue(remainingHours);
                        minutesPicker.setValue(remainingMinutes);
                        secondsPicker.setValue(remainingSeconds);
                    }
                    @Override
                    public void onFinish() {
                        hoursPicker.setValue(0);
                        minutesPicker.setValue(0);
                        secondsPicker.setValue(0);
                        autoTestButton.setEnabled(true);
                        Toast.makeText(MainActivity.this, "倒计时结束", Toast.LENGTH_SHORT).show();
                        uartButton.setBackgroundColor(Color.GREEN);
                        // Finish WifiBtSwitchTestActivity and return to MainActivity
                        //Intent resultIntent = new Intent();
                        //setResult(RESULT_OK, resultIntent);
                        //finish();
                    }
                }.start();
            }


        }); */

        cpuIdInput = findViewById(R.id.cpu_id_input);
        pcbIdInput = findViewById(R.id.pcb_id_input);
        snIdInput = findViewById(R.id.sn_id_input);
        saveIdButton = findViewById(R.id.save_id_button);
        testResultButton = findViewById(R.id.test_result_button);
        uploadTestResultButton = findViewById(R.id.upload_test_result_button);

        testInfo = new TestInfo(this);

        // 获取 CPU ID 并设置到输入框中
        String cpuId = testInfo .getCpuId();
        cpuIdInput.setText(cpuId);
        String board = testInfo.getBoard();

        // 获取 SN ID 并设置到输入框中
        String snId = getSystemProperty("ro.serialno");
        snIdInput.setText(snId);

        saveIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hours = hoursPicker.getValue();
                int minutes = minutesPicker.getValue();
                int seconds = secondsPicker.getValue();
                int totalDurationInSeconds = (hours * 3600) + (minutes * 60) + seconds;
                int totalHours = totalDurationInSeconds / 3600;
                int totalMinutes = (totalDurationInSeconds % 3600) / 60;
                int totalSeconds = totalDurationInSeconds % 60;
                String totalDurationFormatted = String.format("%02d:%02d:%02d", totalHours, totalMinutes, totalSeconds);
               // System.out.println("总时长: " + totalDurationFormatted);
                String pcbId = pcbIdInput.getText().toString();
                testInfo .saveIdsAsHtml(board, cpuId, pcbId, snId, totalDurationFormatted);
                //Intent intent = new Intent(MainActivity.this, UartTestActivity.class);
                //  intent.putExtra("file_path", new File(getFilesDir(), "saved_ids.html").getAbsolutePath());
                // startActivity(intent);
            }
        });

        testResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testInfo.viewSavedFile();
            }
        });

        uploadTestResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pcbId = pcbIdInput.getText().toString();
                testInfo.uploadFileToDownload();
            }
        });
        Button stopButton = findViewById(R.id.stop_button);
        Button autoTestButton = findViewById(R.id.auto_test_button);
        autoTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopButton.performClick();
                saveIdButton.performClick();
                autoTestButton.setEnabled(false);
                int hours = hoursPicker.getValue();
                int minutes = minutesPicker.getValue();
                int seconds = secondsPicker.getValue();
                int timeInSeconds = hours * 3600 + minutes * 60 + seconds;
                int timeInMillis = timeInSeconds * 1000;

                Intent intent = new Intent(MainActivity.this, UartTestActivity.class);
                intent.putExtra("TIMER", true); // 传递是否启动定时器
                intent.putExtra("TIME_IN_SECONDS", timeInSeconds); // 传递定时时间
                //intent.putExtra("file_path", new File(getFilesDir(), "saved_ids.html").getAbsolutePath());


                // 使用SharedPreferences存储状态
                SharedPreferences preferences = getSharedPreferences("TestStatus", MODE_PRIVATE);
                boolean isUartTestGreen = preferences.getBoolean("UartTestGreen", false);
                boolean isWifiBtTestGreen = preferences.getBoolean("WifiBtTestGreen", false);
                boolean isStorageTestGreen = preferences.getBoolean("StorageTestGreen", false);
                boolean isWifiTestGreen = preferences.getBoolean("WifiTestGreen", false);
                boolean isEthTestGreen = preferences.getBoolean("EthTestGreen", false);
                boolean isSpiTestGreen = preferences.getBoolean("SpiTestGreen", false);
                boolean isCanTestGreen = preferences.getBoolean("CanTestGreen", false);

                // 从第一个未完成的测试项开始
                if (!isUartTestGreen) {
                   // Intent intent = new Intent(MainActivity.this, UartTestActivity.class);
                    startTestActivity(UartTestActivity.class, timeInSeconds, REQUEST_CODE_0);
                } else if (!isWifiBtTestGreen) {
                    startTestActivity(WifiBtSwitchTestActivity.class, timeInSeconds, REQUEST_CODE_3);
                } else if (!isStorageTestGreen) {
                    startTestActivity(StorageTestActivity.class, timeInSeconds, REQUEST_CODE_2);
                } else if (!isWifiTestGreen) {
                    startTestActivity(WifiTestActivity.class, timeInSeconds, REQUEST_CODE_1);
                } else if (!isEthTestGreen) {
                    startTestActivity(NetworkTestActivity.class, timeInSeconds, REQUEST_CODE_4);
                } else if (!isSpiTestGreen) {
                    startTestActivity(SpiTestActivity.class, timeInSeconds, REQUEST_CODE_5);
                } else if (!isCanTestGreen) {
                    startTestActivity(CanTestActivity.class, timeInSeconds, REQUEST_CODE_6);
                }
                new CountDownTimer(timeInMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        int remainingHours = (int) (millisUntilFinished / 3600000);
                        int remainingMinutes = (int) (millisUntilFinished % 3600000) / 60000;
                        int remainingSeconds = (int) (millisUntilFinished % 60000) / 1000;
                        hoursPicker.setValue(remainingHours);
                        minutesPicker.setValue(remainingMinutes);
                        secondsPicker.setValue(remainingSeconds);
                    }

                    @Override
                    public void onFinish() {
                        hoursPicker.setValue(0);
                        minutesPicker.setValue(0);
                        secondsPicker.setValue(0);
                        autoTestButton.setEnabled(true);
                        Toast.makeText(MainActivity.this, "倒计时结束", Toast.LENGTH_SHORT).show();
                        uartButton.setBackgroundColor(Color.GREEN);

                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("UartTestGreen", true);
                        editor.apply();
                    }
                }.start();
            }
        });





        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoTestButton.setEnabled(true);

                // 清除所有状态
                SharedPreferences preferences = getSharedPreferences("TestStatus", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear(); // 清除所有存储的状态
                editor.apply();

                // 复原所有按钮的背景颜色
                uartButton.setBackgroundColor(Color.BLUE);
                WifiBtButton.setBackgroundColor(Color.BLUE);
                StorageButton.setBackgroundColor(Color.BLUE);
                wifiStatusButton.setBackgroundColor(Color.BLUE);
                ethButton.setBackgroundColor(Color.BLUE);
                spiButton.setBackgroundColor(Color.BLUE);
                canButton.setBackgroundColor(Color.BLUE);


                uartButton.setEnabled(true);
                WifiBtButton.setEnabled(true);
                StorageButton.setEnabled(true);
                wifiStatusButton.setEnabled(true);
                ethButton.setEnabled(true);
                startTime.setEnabled(true);
                timeDisplay.stop();

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "Result Code: " + resultCode);
        SharedPreferences preferences = getSharedPreferences("TestStatus", MODE_PRIVATE);

        if (resultCode == RESULT_OK && data != null) {
            boolean startTimer = data.getBooleanExtra("TIMER", false);
            boolean isNode = data.getBooleanExtra("NODE_NOT_EXIST", false);
            boolean isPass = data.getBooleanExtra("TEST_PASS", false);
            int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);

            switch (requestCode) {
                case REQUEST_CODE_0:
                    if (startTimer) {
                        if(! isPass)
                        {
                            uartButton.setBackgroundColor(Color.RED);
                        }
                        else {
                            uartButton.setBackgroundColor(Color.GREEN);
                        }
                        Log.d("MainActivity", "uart test start !!!!");
                        updateButtonStateAndStartNextTest(preferences, "UartTestGreen", WifiTestActivity.class, timeInSeconds, REQUEST_CODE_1);
                    }
                    break;
                case REQUEST_CODE_1:
                    if (startTimer) {
                        if(! isPass)
                        {
                            wifiStatusButton.setBackgroundColor(Color.RED);
                        }
                        else {
                            wifiStatusButton.setBackgroundColor(Color.GREEN);
                        }

                        Log.d("MainActivity", "Wifi status test start !!!!");
                        updateButtonStateAndStartNextTest(preferences, "WifiTestGreen", StorageTestActivity.class, timeInSeconds, REQUEST_CODE_2);
                    }
                    break;
                case REQUEST_CODE_2:
                    if (startTimer) {
                        if(! isPass)
                        {
                            StorageButton.setBackgroundColor(Color.RED);
                        }
                        else {
                            StorageButton.setBackgroundColor(Color.GREEN);
                        }
                        Log.d("MainActivity", "Storage test start !!!!");
                        updateButtonStateAndStartNextTest(preferences, "StorageTestGreen", WifiBtSwitchTestActivity.class, timeInSeconds, REQUEST_CODE_3);
                    }
                    break;
                case REQUEST_CODE_3:
                    if (startTimer) {
                     //   wifiStatusButton.setBackgroundColor(Color.GREEN);
                        if(! isPass)
                        {
                            WifiBtButton.setBackgroundColor(Color.RED);
                        }
                        else {
                            WifiBtButton.setBackgroundColor(Color.GREEN);
                        }
                        Log.d("MainActivity", "WifiBT test start !!!!");
                        updateButtonStateAndStartNextTest(preferences, "WifiBtTestGreen", NetworkTestActivity.class, timeInSeconds, REQUEST_CODE_4);
                    }
                    break;
                case REQUEST_CODE_4:
                    if (startTimer) {
                        if(! isPass)
                        {
                            ethButton.setBackgroundColor(Color.RED);
                        }
                        else {
                            ethButton.setBackgroundColor(Color.GREEN);
                        }
                        Log.d("MainActivity", "Network test start !!!!");
                        updateButtonStateAndStartNextTest(preferences, "EthTestGreen", SpiTestActivity.class, timeInSeconds, REQUEST_CODE_5);
                    }
                    break;
                case REQUEST_CODE_5:
                    if (startTimer) {
                        if(! isNode || ! isPass)
                        {
                            spiButton.setBackgroundColor(Color.RED);
                        }
                        else {
                            spiButton.setBackgroundColor(Color.GREEN);
                        }
                        Log.d("MainActivity", "SPI test start !!!!");
                        updateButtonStateAndStartNextTest(preferences, "SpiTestGreen", CanTestActivity.class, timeInSeconds, REQUEST_CODE_6);
                    }
                    break;
                case REQUEST_CODE_6:
                    if(startTimer)
                    {
                        if(! isNode || ! isPass)
                        {canButton.setBackgroundColor(Color.RED);}
                        else {
                            canButton.setBackgroundColor(Color.GREEN);
                        }
                    }
                    SharedPreferences.Editor editor6 = preferences.edit();
                    editor6.putBoolean("CanTestGreen", true);
                    editor6.apply();
                    break;
            }
        }
    }

    private void updateButtonStateAndStartNextTest(SharedPreferences preferences, String key, Class<?> nextActivityClass, int timeInSeconds, int nextRequestCode) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, true);
        editor.apply();

        Intent intent = new Intent(MainActivity.this, nextActivityClass);
        intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
        intent.putExtra("TIMER", true);
        startActivityForResult(intent, nextRequestCode);
    }

    private void startTestActivity(Class<?> activityClass, int timeInSeconds, int requestCode) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
        intent.putExtra("TIMER", true);
        startActivityForResult(intent, requestCode);
    }


   /* protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "Result Code: " + resultCode);

        // 检查 data 是否为 null
        if (data != null) {
            // 打印 Intent 中所有的 Extra 数据
            Bundle extras = data.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    Log.d("MainActivity", "Key: " + key + " Value: " + value);
                }
            }
        } else {
            Log.d("MainActivity", "Data is null");
        }

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_0) {
                boolean startTimer = data.getBooleanExtra("TIMER", false);
                int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
                if (startTimer) {
                    Log.d("MainActivity", "uart test start !!!!");
                    Intent intent = new Intent(MainActivity.this, WifiBtSwitchTestActivity.class);
                    intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    intent.putExtra("TIMER", true); // 传递是否启动定时器
                    startActivityForResult(intent, REQUEST_CODE_1);
                }
            }
           else if (requestCode == REQUEST_CODE_1) {

                boolean startTimer = data.getBooleanExtra("TIMER", false);
                int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
                if (startTimer) {
                    WifiBtButton.setBackgroundColor(Color.GREEN);
                    Log.d("MainActivity", "Storage test start !!!!");
                    Intent intent = new Intent(MainActivity.this, StorageTestActivity.class);
                    intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    intent.putExtra("TIMER", true); // 传递是否启动定时器
                    startActivityForResult(intent, REQUEST_CODE_2);
                }
            }   else if (requestCode == REQUEST_CODE_2) {
                Toast.makeText(MainActivity.this, "倒计时结束", Toast.LENGTH_SHORT).show();
                boolean startTimer = data.getBooleanExtra("TIMER", false);
                int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
                if (startTimer) {
                    StorageButton.setBackgroundColor(Color.GREEN);
                    Log.d("MainActivity", "Wifi test start !!!!");
                    Toast.makeText(MainActivity.this, "倒计时结束", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, WifiTestActivity.class);
                    intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    intent.putExtra("TIMER", true); // 传递是否启动定时器
                    startActivityForResult(intent, REQUEST_CODE_3);
                }
            }
            else if (requestCode == REQUEST_CODE_3) {
                Toast.makeText(MainActivity.this, "倒计时结束", Toast.LENGTH_SHORT).show();
                boolean startTimer = data.getBooleanExtra("TIMER", false);
                int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
                if (startTimer) {
                    wifiStatusButton.setBackgroundColor(Color.GREEN);
                    Log.d("MainActivity", "ETH test start !!!!");
                    Intent intent = new Intent(MainActivity.this, NetworkTestActivity.class);
                    intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    intent.putExtra("TIMER", true); // 传递是否启动定时器
                    startActivityForResult(intent, REQUEST_CODE_4);
                }
            }
            else if (requestCode == REQUEST_CODE_4) {
                Toast.makeText(MainActivity.this, "倒计时结束", Toast.LENGTH_SHORT).show();
                boolean startTimer = data.getBooleanExtra("TIMER", false);
                int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
                if (startTimer) {
                    ethButton.setBackgroundColor(Color.GREEN);
                    Log.d("MainActivity", "SPI test start !!!!");
                   // Intent intent = new Intent(MainActivity.this, CanTestActivity.class);
                    Intent intent = new Intent(MainActivity.this, SpiTestActivity.class);
                    intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    intent.putExtra("TIMER", true); // 传递是否启动定时器
                    startActivityForResult(intent, REQUEST_CODE_5);
                }
            }
            else if (requestCode == REQUEST_CODE_5) {
                Toast.makeText(MainActivity.this, "倒计时结束", Toast.LENGTH_SHORT).show();
                boolean startTimer = data.getBooleanExtra("TIMER", false);
                int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
                if (startTimer) {
                    spiButton.setBackgroundColor(Color.GREEN);
                    Log.d("MainActivity", "Can test start !!!!");
                   // Intent intent = new Intent(MainActivity.this, SpiTestActivity.class);
                    Intent intent = new Intent(MainActivity.this,  CanTestActivity.class);
                    intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    intent.putExtra("TIMER", true); // 传递是否启动定时器
                    startActivityForResult(intent, REQUEST_CODE_6);
                }
            }

            else if (requestCode == REQUEST_CODE_6) {
                Toast.makeText(MainActivity.this, "倒计时结束", Toast.LENGTH_SHORT).show();
                canButton.setBackgroundColor(Color.GREEN);
            //    boolean startTimer = data.getBooleanExtra("TIMER", false);
              //  int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
               //if (startTimer) {
                   // Log.d("MainActivity", "SPI test start !!!!");
                   // Intent intent = new Intent(MainActivity.this, UartTestActivity.class);
                  //  intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                  //  intent.putExtra("TIMER", true); // 传递是否启动定时器
                  //  startActivityForResult(intent, REQUEST_CODE_3);
                }
            }


        }
    } */

    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading root permission", e);
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                Log.e(TAG, "Error closing process", e);
            }
        }
        return true;
    }

    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, "unknown"));
        } catch (Exception e) {
            Log.e(TAG, "Error getting property", e);
        } finally {
            return value;
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


    public static class MyNetworkChangeReceiver extends BroadcastReceiver {
        private Handler handler;
        private Runnable runnable;
        private TextView networkStatusTextView;

        public MyNetworkChangeReceiver(Handler handler, Runnable runnable, TextView networkStatusTextView) {
            this.handler = handler;
            this.runnable = runnable;
            this.networkStatusTextView = networkStatusTextView;
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                boolean noConnectivity = intent.getBooleanExtra(
                        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false
                );

                if (noConnectivity) {
                    MainActivity.isRunning = false;
                    handler.removeCallbacks(runnable);  // 停止Runnable
                    // 不更新UI
                } else {
                    MainActivity.isRunning = true;
                }
            }
        }

    }

    public void startApp(String packageName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Toast.makeText(this, "没有找到应用", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean startNApp(Context context, String packageName) {
//String packageName = "XXX";
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> listInfos = pm.queryIntentActivities(intent, 0);
        String className = null;
        for (ResolveInfo info : listInfos) {
            if (packageName.equals(info.activityInfo.packageName)) {
                className = info.activityInfo.name;
                break;
            }
        }
        if (className != null && className.length() > 0) {
            intent.setComponent(new ComponentName(packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    public void startAppWithShellCommand(String packageName, String activityName) {
        Process processa = null;
        DataOutputStream os = null;
        try {
            String cmd="am start " + packageName + "/" + activityName;
            processa = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(processa.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            processa.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading root permission", e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (processa != null) {
                    processa.destroy();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing process", e);
            }
        }
    }

    private void initPermission() {
    List<String> mPermissionList = new ArrayList<>();
    // Android 版本大于等于 12 时，申请新的蓝牙权限
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        mPermissionList.add(Manifest.permission.BLUETOOTH_SCAN);
        mPermissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        mPermissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
        mPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mPermissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        mPermissionList.add(Manifest.permission.RECORD_AUDIO);

        //根据实际需要申请定位权限
        mPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        mPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
    } else {
        mPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        mPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    ActivityCompat.requestPermissions(this, mPermissionList.toArray(new String[0]), REQUEST_PERMISSION_CODE);
}


}

