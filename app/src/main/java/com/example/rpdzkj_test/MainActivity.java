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
import android.net.ConnectivityManager;









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
    private BroadcastReceiver storageStateReceiver;
    private NetworkChangeReceiver networkChangeReceiver;
    private TextView wifiCountTextView;
    private TextView bluetoothCountTextView;
    private TextView sdUsbStatusTextView;
    private TextView sataM2StatusTextView;
    private TextView usedSpaceTextView;
    private TextView usedSpaceTextView2;


    private  Runnable runnable;
    public static boolean isRunning = false;

    private TextView checkView;
    private Runnable checkStorageTask;
    private long usedSpace = 0;
    private long totalSpace = 0;
    private TextView statusTextView;
    private TextView wifiStatusTextView;


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

      //  usedSpaceTextView = findViewById(R.id.used_space_text_view);
        //usedSpaceTextView2 = findViewById(R.id.used_space2_text_view);
        checkView = findViewById(R.id.check_status_text_view);
        statusTextView = findViewById(R.id.status_text_view);
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
      //  TextView sdUsbStatusTextView = findViewById(R.id.sd_usb_status_text_view);
        sdTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sdTestButton.setEnabled(false);
                // 获取所有的外部存储路径
                File[] externalFilesDirs = getExternalFilesDirs(null);
                Log.d(TAG, "获取到的外部存储路径数量：" + externalFilesDirs.length);
                setProperty("sdcard_test",  "1");
                for (File dir : externalFilesDirs) {
                    if (dir != null) {
                        String externalPath = dir.getAbsolutePath();
                        Log.d(TAG, "检查路径：" + externalPath);
                        String type = getStorageType(MainActivity.this, externalPath);
                        Log.d(TAG, "存储类型：" + type);
                        if (type.equals("USB/SD")) {
                            //  checkStorageState(externalPath, "SD", sdUsbStatusTextView, usedSpaceTextView);
                            checkStorageState(externalPath, "SD");
                            checkStorageTask = new Runnable() {
                                @Override
                                public void run() {
                                    // 检查存储状态
                                    // checkStorageState(externalPath, "SD", sdUsbStatusTextView, usedSpaceTextView);
                                    checkStorageState(externalPath, "SD");
                                    String executionCount = getSystemProperty("my.script.execution.count");
                                    Log.d(TAG, "脚本执行次数：" + executionCount);
                                    checkView.setText("测试次数: " + executionCount);
                                    // 1秒后再次执行此任务
                                    handler.postDelayed(this, 1000);
                                }
                            };
                            // 启动检查任务
                            handler.post(checkStorageTask);
                            // 找到第一个 "USB/SD" 类型的路径后就跳出循环
                            break;
                        }
                    }
                }
              //  registerReceiver(storageStateReceiver, filter);
            }
        });

        Button UsbTestButton = findViewById(R.id.usb_test_button);
        UsbTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsbTestButton.setEnabled(false);
                if (checkStorageTask != null) {
                    handler.removeCallbacks(checkStorageTask);
                }
                // 获取所有的外部存储路径
                File[] externalFilesDirs = getExternalFilesDirs(null);
                Log.d(TAG, "获取到的外部存储路径数量：" + externalFilesDirs.length);
                setProperty("udisk_test",  "1");
                for (File dir : externalFilesDirs) {
                    if (dir != null) {
                        String externalPath = dir.getAbsolutePath();
                        Log.d(TAG, "检查路径：" + externalPath);
                        String type = getStorageType(MainActivity.this, externalPath);
                        Log.d(TAG, "存储类型：" + type);
                        if (type.equals("USB/SD")) {
                            checkStorageState(externalPath, "USB");
                            checkStorageTask = new Runnable() {
                                String lastExecutionCount = "";
                                @Override
                                public void run() {
                                    // 检查存储状态
                                    checkStorageState(externalPath, "USB");
                                    String executionCount = getSystemProperty("my.script.execution.count");
                                    // 只有在执行次数发生变化时才更新TextView和打印日志
                                    if (!executionCount.equals(lastExecutionCount)) {
                                        Log.d(TAG, "脚本执行次数：" + executionCount);
                                        checkView.setText("测试次数: " + executionCount);
                                        lastExecutionCount = executionCount;
                                    }
                                    // 1秒后再次执行此任务
                                    handler.postDelayed(this, 1000);
                                }
                            };
                            // 启动检查任务
                            handler.post(checkStorageTask);
                            // 找到第一个 "USB/SD" 类型的路径后就跳出循环
                            break;
                        }
                    }
                }
            }
        });



        Button sataTestButton = findViewById(R.id.sata_test_button);
      //  TextView sataM2StatusTextView = findViewById(R.id.sata_m2_status_text_view);

        sataTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sataTestButton.setEnabled(false);
                handler.post(checkStorageTask);
                // 获取所有的外部存储路径
                File[] externalFilesDirs = getExternalFilesDirs(null);
                Log.d(TAG, "获取到的外部存储路径数量：" + externalFilesDirs.length);
                setProperty("sata_test",  "1");
                for (File dir : externalFilesDirs) {
                    if (dir != null) {
                        String externalPath = dir.getAbsolutePath();
                        if (!externalPath.contains("/storage/emulated/0/")) {
                            Log.d(TAG, "检查路径：" + externalPath);
                            String type = getStorageType(MainActivity.this, externalPath);
                            Log.d(TAG, "存储类型：" + type);
                            if (type.equals("USB/SD") || type.equals("SATA/M.2")) {
                                checkStorageState(externalPath, "SATA");
                                //checkStorageState(externalPath, "SATA", sdUsbStatusTextView, usedSpaceTextView);
                                checkStorageTask = new Runnable() {
                                    @Override
                                    public void run() {
                                        // 检查存储状态
                                        //       checkStorageState(externalPath, "SATA",

                                        checkStorageState(externalPath, "SATA");
                                        String executionCount = getSystemProperty("my.script.execution.count");
                                        Log.d(TAG, "脚本执行次数：" + executionCount);
                                        checkView.setText("测试次数: " + executionCount);
                                        // 1秒后再次执行此任务
                                        handler.postDelayed(this, 1000);
                                    }
                                };
                                // 启动检查任务
                                handler.post(checkStorageTask);
                                // 找到第一个就跳出循环
                                break;
                            }
                        }
                    }
                }
             //   registerReceiver(storageStateReceiver, filter);
            }

        });

        Button M2TestButton = findViewById(R.id.m2_test_button);
        M2TestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                M2TestButton.setEnabled(false);
                handler.post(checkStorageTask);
                // 获取所有的外部存储路径
                File[] externalFilesDirs = getExternalFilesDirs(null);
                Log.d(TAG, "获取到的外部存储路径数量：" + externalFilesDirs.length);
                setProperty("m2_test",  "1");
                for (File dir : externalFilesDirs) {
                    if (dir != null) {
                        String externalPath = dir.getAbsolutePath();
                        if (!externalPath.contains("/storage/emulated/0/")) {
                            Log.d(TAG, "检查路径：" + externalPath);
                            String type = getStorageType(MainActivity.this, externalPath);
                            Log.d(TAG, "存储类型：" + type);
                            if (type.equals("USB/SD") || type.equals("SATA/M.2")) {
                                // checkStorageState(externalPath, "M.2", sdUsbStatusTextView, usedSpaceTextView);
                                checkStorageState(externalPath, "M.2");
                                checkStorageTask = new Runnable() {
                                    @Override
                                    public void run() {
                                        // 检查存储状态
                                        //    checkStorageState(externalPath, "M.2", sdUsbStatusTextView, usedSpaceTextView);
                                        checkStorageState(externalPath, "M.2");
                                        String executionCount = getSystemProperty("my.script.execution.count");
                                        Log.d(TAG, "脚本执行次数：" + executionCount);
                                        checkView.setText("测试次数: " + executionCount);
                                        // 1秒后再次执行此任务
                                        handler.postDelayed(this, 1000);
                                    }
                                };
                                // 启动检查任务
                                handler.post(checkStorageTask);
                                // 找到第一个就跳出循环
                                break;
                            }
                        }
                    }
                }
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


        // 初始化并注册storageStateReceiver
        storageStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Uri data = intent.getData();
                String path = data == null ? null : data.getPath();
                if (action != null && path != null) {
                    String type = getStorageType(context, path);
                    if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        // 存储设备已挂载
                        checkStorageState(path, type);
                    } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                        // 存储设备未挂载或已被拔出
                        statusTextView.append(type + " 未挂载或已被拔出\n");
                    }
                }
            }
        };
        IntentFilter storageFilter = new IntentFilter();
        storageFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        storageFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        storageFilter.addDataScheme("file");
        registerReceiver(storageStateReceiver, storageFilter);

        // 初始化TextView
        wifiStatusTextView = findViewById(R.id.wifi_status_text_view);

        // 初始化"开始"按钮并设置点击事件
        Button wifiStatusButton = findViewById(R.id.wifi_status_test_button);
        wifiStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiStatusButton.setEnabled(false);
                if (!isRunning) {
                    isRunning = true;

                    // 获取IP地址并开始计时
                    final String ip = NetworkUtils.getWifiIp();
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
                                    final String ip = NetworkUtils.getWifiIp();
                                    final String pingResult = NetworkUtils.ping("www.qq.com");

                                    // 在主线程中更新TextView
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            long currentTime = System.currentTimeMillis();
                                            long duration = currentTime - startTime;
                                            double durationInSeconds = duration / 1000.0;
                                            wifiStatusTextView.setText("IP: " + ip + "\nPing result: " + pingResult + "\nDuration: " + durationInSeconds + " s");
                                        }
                                    });
                                }
                            }).start();

                            // 每秒执行一次
                            handler.postDelayed(this, 1000);
                        }
                    };


                    handler.post(runnable);
                }
            }
        });

        // 初始化NetworkChangeReceiver
        networkChangeReceiver = new NetworkChangeReceiver();

        // 注册NetworkChangeReceiver
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        registerReceiver(networkChangeReceiver, networkFilter);


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

                if (isRunning) {
                    isRunning = false;
                    handler.removeCallbacks(runnable);
                    wifiStatusTextView.setText("");
                }
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
                statusTextView.setText("");
            }
        });





    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiStateReceiver);
        unregisterReceiver(bluetoothStateReceiver);
        unregisterReceiver(storageStateReceiver);
        unregisterReceiver(networkChangeReceiver);
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

    public static String execShellCmd(String command) {
        StringBuilder output = new StringBuilder();
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Error executing shell command", e);
        }
        return output.toString();
    }

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

    /* private BroadcastReceiver storageStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Uri data = intent.getData();
            String path = data == null ? null : data.getPath();
            if (action != null && path != null) {
                String type = getStorageType(context, path);
                if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                    // 存储设备已挂载
                    checkStorageState(path, type);
                } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                    // 存储设备未挂载或已被拔出
                    statusTextView.append(type + " 未挂载或已被拔出\n");
                }
            }
        }
    }; */


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
            // 只有在状态发生变化时才更新TextView的文本
            if (!newStatus.equals(statusTextView.getText().toString())) {
                statusTextView.setText(newStatus);
            }
            // 1秒后再次执行此任务
            handler.postDelayed(() -> checkStorageState(path, deviceName), 1000);
        } else {
            // 存储设备未挂载或已被拔出
            String newStatus = deviceName + " 未挂载或已被拔出\n";
            // 只有在状态发生变化时才更新TextView的文本
            if (!newStatus.equals(statusTextView.getText().toString())) {
                statusTextView.setText(newStatus);
            }
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



    public static class NetworkUtils {
     //   public static int disconnectCount = 0;
        public static String getWifiIp() {
            try {
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf : interfaces) {
                    if (!intf.getName().equalsIgnoreCase("wlan0")) continue;

                    List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                    for (InetAddress addr : addrs) {
                        if (!addr.isLoopbackAddress()) {
                            String sAddr = addr.getHostAddress();

                            // Check if IPv4 address
                            boolean isIPv4 = sAddr.indexOf(':') < 0;

                            if (isIPv4) {
                                return sAddr;
                            }
                        }
                    }
                }
            } catch (Exception ignored) { } // for now eat exceptions
            return null;
        }

        public static String ping(String url) {
            String pingResult = "";

            try {
                Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 " + url);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    pingResult += line + "\n";
                }

                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                pingResult = "Ping failed";
              //  disconnectCount++;
            }

           // return "Ping output:\n" + pingResult + "\nDisconnect count: " + disconnectCount;
            return "Ping output:\n" + pingResult ;
        }

    }

    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                boolean noConnectivity = intent.getBooleanExtra(
                        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false
                );

                if (noConnectivity) {
                    MainActivity.isRunning = false;
                 //   NetworkUtils.disconnectCount++;
                } else {
                    MainActivity.isRunning = true;
                }
            }
        }
    }


}

