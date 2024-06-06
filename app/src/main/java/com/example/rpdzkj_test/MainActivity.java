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
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

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

    private WifiManager wifiManager;
    private BluetoothAdapter bluetoothAdapter;
    private EditText editText;
    private Handler handler = new Handler();
    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int DELAY_MS = 2000;  // 延迟2秒
    private Timer timer;
    private boolean wifiEnabled = false;

    private int wifiEnabledCount = 0;
    private int bluetoothEnabledCount = 0;
    private BroadcastReceiver wifiStateReceiver;
    private BroadcastReceiver bluetoothStateReceiver;
    private TextView wifiCountTextView;
    private TextView bluetoothCountTextView;
    private TextView sdStatusTextView;
    private TextView UsbStatusTextView;
    private TextView sataStatusTextView;
    private TextView M2StatusTextView;
    private TextView getTimeTextView;

    private TextView sdUsedSpaceTextView;
    private TextView udiskUsedSpaceTextView;
    private TextView sataUsedSpaceTextView;
    private TextView m2UsedSpaceTextView;

    private  Runnable runnable;
    public static boolean isRunning = false;
    private TimeDisplay timeDisplay;

    private TextView sdCheckView;
    private TextView udiskCheckView;
    private TextView sataCheckView;
    private TextView m2CheckView;
    private Runnable checkStorageTask;
    private long usedSpace = 0;
    private long totalSpace = 0;
    //private static final int MY_PERMISSIONS_REQUEST = 1;
    private final int REQUEST_PERMISSION_CODE = 1001;
   // private long startTime = 0;


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
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

       sdUsedSpaceTextView = findViewById(R.id.sd_used_space_text_view);
        sataUsedSpaceTextView = findViewById(R.id.sata_used_space_text_view);
        udiskUsedSpaceTextView = findViewById(R.id.udisk_used_space_text_view);
        m2UsedSpaceTextView = findViewById(R.id.m2_used_space_text_view);
        getTimeTextView = findViewById(R.id.get_time_text_view);

        sdCheckView = findViewById(R.id.sd_check_status_text_view);
        udiskCheckView = findViewById(R.id.udisk_check_status_text_view);
        sataCheckView = findViewById(R.id.sata_check_status_text_view);
        m2CheckView = findViewById(R.id.m2_check_status_text_view);
        sdStatusTextView = findViewById(R.id.sd_status_text_view);
        sataStatusTextView = findViewById(R.id.sata_status_text_view);
        UsbStatusTextView = findViewById(R.id.usb_status_text_view);
        M2StatusTextView = findViewById(R.id.M2_status_text_view);
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

        Button wifiButton = findViewById(R.id.wifi_button);
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
                                break;
                            default:
                                break;
                        }
                }
                }
            }
        };

        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));



        Button wifiStatusButton = findViewById(R.id.wifi_status_test_button);
        wifiStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WifiTestActivity.class);
                startActivity(intent);
            }
        });

        Button ethButton = findViewById(R.id.ethernet_status_test_button);
        ethButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NetworkTestActivity.class);
                startActivity(intent);
            }
        });


        Button uartButton = findViewById(R.id.uart_test_button);
        uartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UartTestActivity.class);
                startActivity(intent);
            }
        });

        Button canButton = findViewById(R.id.can_test_button);
        canButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CanTestActivity.class);
                startActivity(intent);
            }
        });


        Button spiButton = findViewById(R.id.spi_test_button);
        spiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SpiTestActivity.class);
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

        Button stopButton = findViewById(R.id.stop_button);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiButton.setEnabled(true);
                wifiButton.setBackgroundColor(Color.BLUE);
                bluetoothButton.setEnabled(true);
                bluetoothButton.setBackgroundColor(Color.BLUE );
                sdTestButton.setEnabled(true);
                sdTestButton.setBackgroundColor(Color.BLUE);
                sataTestButton.setEnabled(true);
                sataTestButton.setBackgroundColor(Color.BLUE);
                UsbTestButton.setEnabled(true);
                UsbTestButton.setBackgroundColor(Color.BLUE);
                M2TestButton.setEnabled(true);
                M2TestButton.setBackgroundColor(Color.BLUE);
                wifiStatusButton.setEnabled(true);
                ethButton.setEnabled(true);
                startTime.setEnabled(true);
                timeDisplay.stop();

                if (checkStorageTask != null) {
                    // 移除消息队列中所有的checkStorageTask任务
                    handler.removeCallbacksAndMessages(null);
                    checkStorageTask = null;
                }
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                wifiEnabled = false;
                setProperty("wifi_test", "0");
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiStateReceiver);
        unregisterReceiver(bluetoothStateReceiver);

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

