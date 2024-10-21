package com.example.rpdzkj_test;

import com.example.rpdzkj_test.TimeDisplay;
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





public class MainActivity extends AppCompatActivity {


    private EditText editText;
    private Handler handler = new Handler();
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextView getTimeTextView;
    private NumberPicker hoursPicker, minutesPicker, secondsPicker;
    private  Runnable runnable;
    public static boolean isRunning = false;
    private TimeDisplay timeDisplay;
    private final int REQUEST_PERMISSION_CODE = 1001;

    private Button StorageButton;
    private Button wifiStatusButton;
    private Button ethButton;
    private Button uartButton;
    private Button canButton;
    private Button spiButton;


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
        hoursPicker.setMinValue(1);
        hoursPicker.setMaxValue(24);
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);

        // 设置格式化器，确保个位数前加上0
        NumberPicker.Formatter formatter = value -> String.format("%02d", value);
        hoursPicker.setFormatter(formatter);
        minutesPicker.setFormatter(formatter);
        secondsPicker.setFormatter(formatter);



        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();


        if (actionBar != null) {
            // 设置logo
            actionBar.setLogo(R.drawable.rp_test); // 替换成你的logo资源
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

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
      /*   if((!wifiButton.isEnabled()) || (!bluetoothButton.isEnabled()) || (!sdTestButton.isEnabled()) || (!sataTestButton.isEnabled()) || (!UsbTestButton.isEnabled()) || (!M2TestButton.isEnabled()))
        {
            timeDisplay = new TimeDisplay(getTimeTextView);
            timeDisplay.start();
        } */

        Button startTime = findViewById(R.id.start_time_button);
        timeDisplay = new TimeDisplay(getTimeTextView);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime.setEnabled(false);
                timeDisplay.start();

            }
        });

        Button WifiBtButton = findViewById(R.id.Wifi_Bt_button);
        WifiBtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WifiBtSwitchTestActivity.class);
                intent.putExtra("TIMER", false); // 不启动定时器
                startActivity(intent);
            }
        });



        Button autoTestButton = findViewById(R.id.auto_test_button);
        autoTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoTestButton.setEnabled(false);
                int hours = hoursPicker.getValue();
                int minutes = minutesPicker.getValue();
                int seconds = secondsPicker.getValue();
                int timeInSeconds = hours * 3600 + minutes * 60 + seconds;
                int timeInMillis = timeInSeconds * 1000;

                // Start WifiBtSwitchTestActivity with request code
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
                       /* Intent resultIntent = new Intent();
                        setResult(RESULT_OK, resultIntent);
                        finish(); */
                    }
                }.start();
            }


        });

        Button stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoTestButton.setEnabled(true);
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
                    Log.d("MainActivity", "WifiBt test start !!!!");
                    Intent intent = new Intent(MainActivity.this, WifiBtSwitchTestActivity.class);
                    intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    intent.putExtra("TIMER", true); // 传递是否启动定时器
                    startActivityForResult(intent, REQUEST_CODE_1);
                }
            }
           else if (requestCode == REQUEST_CODE_1) {
               wifiStatusButton.setBackgroundColor(Color.GREEN);
                boolean startTimer = data.getBooleanExtra("TIMER", false);
                int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
                if (startTimer) {
                    Log.d("MainActivity", "Storage test start !!!!");
                    Intent intent = new Intent(MainActivity.this, StorageTestActivity.class);
                    intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    intent.putExtra("TIMER", true); // 传递是否启动定时器
                    startActivityForResult(intent, REQUEST_CODE_2);
                }
            }   else if (requestCode == REQUEST_CODE_2) {
                Toast.makeText(MainActivity.this, "倒计时结束", Toast.LENGTH_SHORT).show();
                StorageButton.setBackgroundColor(Color.GREEN);
                boolean startTimer = data.getBooleanExtra("TIMER", false);
                int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
                if (startTimer) {
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
                wifiStatusButton.setBackgroundColor(Color.GREEN);
                boolean startTimer = data.getBooleanExtra("TIMER", false);
                int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
                if (startTimer) {
                    Log.d("MainActivity", "ETH test start !!!!");
                    Intent intent = new Intent(MainActivity.this, NetworkTestActivity.class);
                    intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    intent.putExtra("TIMER", true); // 传递是否启动定时器
                    startActivityForResult(intent, REQUEST_CODE_4);
                }
            }
            else if (requestCode == REQUEST_CODE_4) {
                Toast.makeText(MainActivity.this, "倒计时结束", Toast.LENGTH_SHORT).show();
                ethButton.setBackgroundColor(Color.GREEN);
                boolean startTimer = data.getBooleanExtra("TIMER", false);
                int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
                if (startTimer) {
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
                spiButton.setBackgroundColor(Color.GREEN);
                boolean startTimer = data.getBooleanExtra("TIMER", false);
                int timeInSeconds = data.getIntExtra("TIME_IN_SECONDS", 0);
                if (startTimer) {
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
             /*   if (startTimer) {
                    Log.d("MainActivity", "SPI test start !!!!");
                    Intent intent = new Intent(MainActivity.this, UartTestActivity.class);
                    intent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    intent.putExtra("TIMER", true); // 传递是否启动定时器
                  //  startActivityForResult(intent, REQUEST_CODE_3);
                }*/
            }


        }
    }

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

