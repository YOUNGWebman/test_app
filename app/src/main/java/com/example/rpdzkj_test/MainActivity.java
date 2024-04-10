package com.example.rpdzkj_test;

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
import java.util.HashMap;
import java.util.Map;


/*
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.net.Uri;
import java.util.List;
import android.os.storage.StorageVolume;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import java.util.Objects;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.net.HttpURLConnection;
import java.net.URL;

 */
import java.util.List;
import android.content.pm.ResolveInfo;

import android.net.ConnectivityManager;
import android.widget.Toast;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.content.ComponentName;
import android.content.ActivityNotFoundException;









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

    private TextView sdUsedSpaceTextView;
    private TextView udiskUsedSpaceTextView;
    private TextView sataUsedSpaceTextView;
    private TextView m2UsedSpaceTextView;

    private  Runnable runnable;
    public static boolean isRunning = false;

    private TextView sdCheckView;
    private TextView udiskCheckView;
    private TextView sataCheckView;
    private TextView m2CheckView;
    private Runnable checkStorageTask;
    private long usedSpace = 0;
    private long totalSpace = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        sdCheckView = findViewById(R.id.sd_check_status_text_view);
        udiskCheckView = findViewById(R.id.udisk_check_status_text_view);
        sataCheckView = findViewById(R.id.sata_check_status_text_view);
        m2CheckView = findViewById(R.id.m2_check_status_text_view);
        sdStatusTextView = findViewById(R.id.sd_status_text_view);
        sataStatusTextView = findViewById(R.id.sata_status_text_view);
        UsbStatusTextView = findViewById(R.id.usb_status_text_view);
        M2StatusTextView = findViewById(R.id.M2_status_text_view);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        // 注册广播接收器
        // 注册storageStateReceiver
      /*  IntentFilter storageFilter = new IntentFilter();
        storageFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        storageFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        storageFilter.addDataScheme("file");
        registerReceiver(storageStateReceiver, storageFilter);
*/


        File sdCardRoot = getExternalFilesDir(null);
        String sdCardPath = sdCardRoot.getAbsolutePath();
        Log.d(TAG, "SD 卡路径：" + sdCardPath);

        // 获取其他外部存储路径
        File[] externalFilesDirs = getExternalFilesDirs(null);
        String externalPath = null;

        for (File dir : externalFilesDirs) {
            if (dir != null) {
                externalPath = dir.getAbsolutePath();
                Log.d(TAG, "其他外部存储路径：" + externalPath);
            }
        }

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
                setSystemProperty("sdcard_test",  "1");
             /*   new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);  // 延迟1秒
                            // 执行您的操作
                            setSystemProperty("sdcard_test",  "1");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

              */
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
                setSystemProperty("udisk_test",  "1");
           /*     new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);  // 延迟2秒
                            // 执行您的操作
                            setSystemProperty("udisk_test",  "1");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            */
                // 添加延迟
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Boolean udiskflag = Boolean.parseBoolean(getSystemProperty("rp.udisk.storage.flag"));
                        if(udiskflag)
                        {
                            UsbStatusTextView.setText("U盘已挂载");
                        }else {
                            sdStatusTextView.setText("U盘未挂载或未格式化");
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
                setSystemProperty("sata_test",  "1");

           /*     new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);  // 延迟1秒
                            // 执行您的操作
                            setSystemProperty("sata_test",  "1");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            */


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
                setSystemProperty("m2_test",  "1");
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);  // 延迟1秒
                            // 执行您的操作
                            setSystemProperty("m2_test",  "1");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                 */
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
               // seStartApp("com.cghs.stresstest", "com.cghs.stresstest.test.CameraTest");
              //  seStartApp("com.cghs.stresstest" , "com.cghs.stresstest.StressTestActivity");
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

        wifiCountTextView = findViewById(R.id.wifi_count_text_view);
        bluetoothCountTextView = findViewById(R.id.bluetooth_count_text_view);

        wifiStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

                switch (wifiStateExtra) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        wifiEnabledCount++;
                        wifiCountTextView.setText("WiFi打开成功的次数: " + wifiEnabledCount);
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        // WiFi已关闭
                        break;
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
                    switch (state) {
                        case BluetoothAdapter.STATE_ON:
                            bluetoothEnabledCount++;
                            bluetoothCountTextView.setText("蓝牙打开成功的次数: " + bluetoothEnabledCount);
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            // 蓝牙已关闭
                            break;
                    }
                }
            }
        };

        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));



        Button wifiStatusButton = findViewById(R.id.wifi_status_test_button);
        wifiStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // wifiStatusButton.setEnabled(false);
                Intent intent = new Intent(MainActivity.this, WifiTestActivity.class);
                startActivity(intent);
             /*  if (!isRunning) {
                    isRunning = true;

                    // 获取IP地址并开始计时
                  //  final String ip = NetworkUtils.getNetWorkIp();
                 final    String ip = NetworkUtils.getNetWorkIp("wlan0");
                    final long startTime = System.currentTimeMillis();

                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (!isRunning) {
                                return;
                            }

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //final String ip = NetworkUtils.getNetWorkIp();
                                    final    String ip = NetworkUtils.getNetWorkIp("wlan0");
                                    final String pingResult = NetworkUtils.ping("www.qq.com");

                                    // 在主线程中更新TextView
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            long currentTime = System.currentTimeMillis();
                                            long duration = currentTime - startTime;
                                            double durationInSeconds = duration / 1000.0;
                                            networkStatusTextView.setText("IP: " + ip + "\nPing result: " + pingResult + "\nDuration: " + durationInSeconds + " s");
                                        }
                                    });
                                }
                            }).start();

                            // 每秒执行一次
                            handler.postDelayed(this, 1000);
                        }
                    };


                    handler.post(runnable);
                } */
            }
        });

        Button ethButton = findViewById(R.id.ethernet_status_test_button);


        ethButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NetworkTestActivity.class);
                startActivity(intent);
            }
         /*  public void onClick(View v) {
                ethButton.setEnabled(false);
                final long startTime = System.currentTimeMillis();
             //   String ip = NetworkUtils.getNetWorkIp(); // 修改这里
               final String ipEth0 = NetworkUtils.getNetWorkIp("eth0");
               final String ipEth1 = NetworkUtils.getNetWorkIp("eth1");
                if (ipEth0 != null || ipEth1 != null) {
                    networkStatusTextView.setText("Current IPs: " + ipEth0 + ", " + ipEth1);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                           // String pingResult = NetworkUtils.ping("www.qq.com");
                            String pingResultEth0 = NetworkUtils.ping("www.qq.com");
                            String pingResultEth1 = NetworkUtils.ping("www.qq.com");
                            long duration = System.currentTimeMillis() - startTime;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                   // networkStatusTextView.append("\nPing result:\n" + pingResult);
                                    networkStatusTextView.append("\nPing result for eth0:\n" + pingResultEth0);
                                    networkStatusTextView.append("\nPing result for eth1:\n" + pingResultEth1);
                                    networkStatusTextView.append("\nDuration: " + duration + " ms");
                                }
                            });
                        }
                    }).start();
                } else {
                    networkStatusTextView.setText("IP not found");
                }
            } */
        });



        Button stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiButton.setEnabled(true);
                bluetoothButton.setEnabled(true);
                sdTestButton.setEnabled(true);
                sataTestButton.setEnabled(true);
                UsbTestButton.setEnabled(true);
                M2TestButton.setEnabled(true);
                wifiStatusButton.setEnabled(true);
                ethButton.setEnabled(true);


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


/*
    private long getUsedSpace(File directory) {
        StatFs statFs = new StatFs(directory.getPath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        long availableBlocks = statFs.getAvailableBlocksLong();
        return (totalBlocks - availableBlocks) * blockSize;
    }

    private long getTotalSpace(File directory) {
        StatFs statFs = new StatFs(directory.getPath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    private String formatFileSize(long size) {
        return Formatter.formatFileSize(this, size);
    }


    private String getStorageType(Context context, String path) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        List

                <StorageVolume> volumes = storageManager.getStorageVolumes();
        for (StorageVolume volume : volumes) {
            File dir = volume.getDirectory();
            if (dir != null && path.startsWith(dir.getAbsolutePath())) {
                String description = volume.getDescription(context);
                Log.d(TAG, "存储卷描述：" + description);
                if (description.toLowerCase().contains("usb") || description.toLowerCase().contains("sd") || description.toLowerCase().contains("u")) {
                    // 这是一个可移动的存储设备，可能是USB设备、SD卡或U盘
                    return "USB/SD";
                } else {
                    // 这是一个内部存储设备，可能是SATA设备或M.2设备
                    return "SATA/M.2";
                }
            }
        }
        // 未找到匹配的存储设备
        return "Unknown";
    }

    private void checkStorageState(String path, String deviceName) {
        File directory = new File(path);
        if (directory.exists()) {
            // 存储设备已挂载
            StringBuilder status = new StringBuilder();
            // 获取已使用的容量
            long usedSpace = getUsedSpace(directory);
            // 获取总容量
            long totalSpace = getTotalSpace(directory);
            status.append(deviceName).append(" 已挂载    ")
                    .append("已使用空间：").append(formatFileSize(usedSpace))
                    .append(" / 总容量：").append(formatFileSize(totalSpace)).append("\n");
            String newStatus = status.toString();
            // 根据设备类型更新对应的TextView
            switch (deviceName) {
                case "M.2":
                    usedSpaceTextView3.setText(newStatus);
                    break;
                case "SATA":
                    usedSpaceTextView1.setText(newStatus);
                    break;
                case "USB":
                    usedSpaceTextView2.setText(newStatus);
                    break;
                case "SD":
                    usedSpaceTextView.setText(newStatus);
                    break;
            }
            // 1秒后再次执行此任务
            handler.postDelayed(() -> checkStorageState(path, deviceName), 1000);
        } else {
            // 存储设备未挂载或已被拔出
            String newStatus = deviceName + " 未挂载或已被拔出\n";
            // 根据设备类型更新对应的TextView
            switch (deviceName) {
                case "M.2":
                    usedSpaceTextView3.setText(newStatus);
                    break;
                case "SATA":
                    usedSpaceTextView1.setText(newStatus);
                    break;
                case "USB":
                    usedSpaceTextView2.setText(newStatus);
                    break;
                case "SD":
                    usedSpaceTextView.setText(newStatus);
                    break;
            }
        }
    }
*/

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

    /*public void startNApp(String packageName, String activityName) {
        Intent intent = new Intent();
        ComponentName cn = new ComponentName(packageName, activityName);
        intent.setComponent(cn);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "没有找到应用", Toast.LENGTH_SHORT).show();
        }
    }

     */
/*
    public void seStartApp(String packageName, String activityName) {
        Intent intent = new Intent();
        intent.setClassName(packageName, activityName);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "没有找到应用", Toast.LENGTH_SHORT).show();
        }
    }

 */
    public void seStartApp(String packageName, String activityName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(packageName, activityName));

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "没有找到应用", Toast.LENGTH_SHORT).show();
        }
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




}

