package com.example.rpdzkj_test;

import android.content.BroadcastReceiver;
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
import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.util.concurrent.atomic.AtomicBoolean;



public class WifiTestActivity extends AppCompatActivity {

    private TextView networkStatusTextView;


    public static boolean isRunning = false;
    private  Runnable runnable;
    private Handler handler = new Handler();
    private MyNetworkChangeReceiver wifiNetworkChangeReceiver;
    private static AtomicBoolean shouldStop = new AtomicBoolean(false);
    private static double lastRecordedTime = 0;
    private static long startTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_test);

        networkStatusTextView = findViewById(R.id.wifi_network_status_text_view);



         if (!isRunning) {
                    isRunning = true;

                    // 获取IP地址并开始计时

                 final    String ip = NetworkUtils.getNetWorkIp("wlan0");


          runnable = new Runnable() {
                 @Override
                 public void run() {
                     if (!isRunning) {
                         return;
                     }

                     new Thread(new Runnable() {
                         @Override
                         public void run() {
                             if (shouldStop.get()) {

                                 return;
                             }

                             final String ip = NetworkUtils.getNetWorkIp("wlan0");
                             final String pingResult = NetworkUtils.ping("www.qq.com");


                             // 在主线程中更新TextView
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     if (!isRunning) {
                                         return;
                                     }
                                     if (shouldStop.get()) {

                                         networkStatusTextView.setText("测试失败" + "\nDuration:" + lastRecordedTime + " s" );
                                         isRunning = false;
                                         return;
                                     }
                                        long currentTime = System.currentTimeMillis();
                                      long duration = currentTime - startTime;
                                     double durationInSeconds = duration / 1000.0;
                                     lastRecordedTime = durationInSeconds;
                                     networkStatusTextView.setText("IP: " + ip + "\nPing result: " + pingResult + "\nDuration: " + durationInSeconds + " s");
                                 }
                             });
                         }
                     }).start();

                     // 每0.7秒执行一次
                     handler.postDelayed(this, 700);
                 }
             };

             handler.post(runnable);
                }


        wifiNetworkChangeReceiver = new MyNetworkChangeReceiver(handler, runnable, networkStatusTextView);


        // 创建意图过滤器，并设置它只接收网络状态变化的广播
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        // 注册广播接收器
        registerReceiver(wifiNetworkChangeReceiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 取消注册广播接收器
        unregisterReceiver(wifiNetworkChangeReceiver);
    }


    public static class NetworkUtils {
        //   public static int disconnectCount = 0;
        public static String getNetWorkIp(String interfaceName) {
            try {
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf : interfaces) {
                    //   if (!intf.getName().equalsIgnoreCase("wlan0") || !intf.getName().equalsIgnoreCase("eth0") || !intf.getName().equalsIgnoreCase("eth1"))
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
                //  disconnectCount++;
            }

            // return "Ping output:\n" + pingResult + "\nDisconnect count: " + disconnectCount;
            return "Ping output:\n" + pingResult ;
        }

        public static boolean isNetworkConnected(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
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
                    shouldStop.set(true);  // 停止新线程
                    // 不更新UI
                } else {
                    MainActivity.isRunning = true;
                    shouldStop.set(false);  // 允许新线程
                    startTime = System.currentTimeMillis();  // 重新开始计时
                    handler.post(runnable);  // 重新开始Runnable
                }
            }


        }

    }



}


