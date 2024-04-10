package com.example.rpdzkj_test;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

import android.net.ConnectivityManager;


import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkTestActivity extends AppCompatActivity {
    private TextView networkStatusTextView;
    private TextView networkStatusTextView1;
    private long startTime;
    public static AtomicBoolean isRunning = new AtomicBoolean(false);
    private Runnable runnable;
    private Handler handler = new Handler();
    private MainActivity.MyNetworkChangeReceiver ethnetworkChangeReceiver;
    private MainActivity.MyNetworkChangeReceiver ethnetworkChangeReceiver1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_test);

        networkStatusTextView = findViewById(R.id.ethnetwork_status_text_view);
        networkStatusTextView1 = findViewById(R.id.ethnetwork1_status_text_view);
        startTime = System.currentTimeMillis();

        if (!isRunning.get()) {
            isRunning.set(true);

            // 获取IP地址并开始计时
            final String ipEth0 = NetworkUtils.getNetWorkIp("eth0");
            final String ipEth1 = NetworkUtils.getNetWorkIp("eth1");
            final long startTimeEth0 = System.currentTimeMillis();  // 在这里初始化startTimeEth0
            final long startTimeEth1 = System.currentTimeMillis();  // 在这里初始化startTimeEth1

            runnable = new Runnable() {
                @Override
                public void run() {
                    if (!isRunning.get()) {
                        return;
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String lastIpEth0 = null;
                            String lastPingResultEth0 = null;
                            while (isRunning.get()) {
                                final String ipEth0 = NetworkUtils.getNetWorkIp("eth0");
                                if (ipEth0 == null) {
                                    final long disconnectTimeEth0 = System.currentTimeMillis();
                                    runOnUiThread(() -> {
                                        long durationEth0 = disconnectTimeEth0 - startTimeEth0;
                                        networkStatusTextView.setText("\nIP: null\nDuration for eth0: " + durationEth0 / 1000.0 + " s");
                                    });
                                    return;
                                }
                                final String pingResultEth0 = NetworkUtils.ping("www.qq.com");
                                final long durationEth0 = System.currentTimeMillis() - startTimeEth0;
                                if (!ipEth0.equals(lastIpEth0) || !pingResultEth0.equals(lastPingResultEth0)) {
                                    lastIpEth0 = ipEth0;
                                    lastPingResultEth0 = pingResultEth0;
                                    runOnUiThread(() -> networkStatusTextView.setText("\nIP: " + ipEth0 + "\nDuration for eth0: "+ "\nPing result for eth0:\n" + pingResultEth0 + durationEth0 / 1000.0 + " s"));
                                }
                            }
                        }
                    }).start();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String lastIpEth1 = null;
                            String lastPingResultEth1 = null;
                            while (isRunning.get()) {
                                final String ipEth1 = NetworkUtils.getNetWorkIp("eth1");
                                if (ipEth1 == null) {
                                    final long disconnectTimeEth1 = System.currentTimeMillis();
                                    runOnUiThread(() -> {
                                        long durationEth1 = disconnectTimeEth1 - startTimeEth1;
                                        networkStatusTextView1.setText("\nIP: null\nDuration for eth1: " + durationEth1 / 1000.0 + " s");
                                    });
                                    return;
                                }
                                final String pingResultEth1 = NetworkUtils.ping("www.qq.com");
                                final long durationEth1 = System.currentTimeMillis() - startTimeEth1;
                                if (!ipEth1.equals(lastIpEth1) || !pingResultEth1.equals(lastPingResultEth1)) {
                                    lastIpEth1 = ipEth1;
                                    lastPingResultEth1 = pingResultEth1;
                                    runOnUiThread(() -> networkStatusTextView1.setText("\nIP: " + ipEth1 +"\nDuration for eth1: " + "\nPing result for eth1:\n" + pingResultEth1 + durationEth1 / 1000.0 + " s"));
                                }
                            }
                        }
                    }).start();

                    ethnetworkChangeReceiver = new MainActivity.MyNetworkChangeReceiver(handler, runnable, networkStatusTextView);
                    ethnetworkChangeReceiver1 = new MainActivity.MyNetworkChangeReceiver(handler, runnable, networkStatusTextView1);
                    // 创建意图过滤器，并设置它只接收网络状态变化的广播
                    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

                    // 注册广播接收器
                    registerReceiver(ethnetworkChangeReceiver, filter);
                    registerReceiver(ethnetworkChangeReceiver1, filter);
                }
            };
            handler.post(runnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 取消注册广播接收器
        unregisterReceiver(ethnetworkChangeReceiver);
        unregisterReceiver(ethnetworkChangeReceiver1);
        isRunning.set(false);
    }

    public static class NetworkUtils {
        public static String getNetWorkIp(String interfaceName) {
            try {
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf : interfaces) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName))
                        continue;

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
            }

            return "Ping output:\n" + pingResult ;
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
                    NetworkTestActivity.isRunning.set(false);
                    handler.removeCallbacks(runnable);  // 停止Runnable
                    // 不更新UI
                } else {
                    NetworkTestActivity.isRunning.set(true);
                }
            }
        }
    }
}

