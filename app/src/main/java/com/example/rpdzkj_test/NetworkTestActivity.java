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
import android.net.NetworkInfo;
import java.net.SocketException;
import android.util.Log;
import java.util.concurrent.atomic.AtomicBoolean;
import android.net.NetworkRequest;
import android.net.NetworkCapabilities;
import android.net.Network;



public class NetworkTestActivity extends AppCompatActivity {

    private TextView networkStatusTextView, networkStatusTextView1, networkStatusTextView2;

    private Runnable runnable, runnable1, runnable2;
    private Handler handler = new Handler(), handler1 = new Handler(), handler2 = new Handler();
    private NetworkTest networkTest, networkTest1, networkTest2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_test);
        networkStatusTextView = findViewById(R.id.ethnetwork_status_text_view);
        networkStatusTextView1 = findViewById(R.id.ethnetwork1_status_text_view);
        networkStatusTextView2 = findViewById(R.id.ethnetwork2_status_text_view);

        // 初始化isRunning
        AtomicBoolean isRunning0 = new AtomicBoolean(false);
        AtomicBoolean isRunning1 = new AtomicBoolean(false);
        AtomicBoolean isRunning2 = new AtomicBoolean(false);

        // 初始化isStoped
 /*       AtomicBoolean isStoped0 = new AtomicBoolean(false);
        AtomicBoolean isStoped1= new AtomicBoolean(false);
        AtomicBoolean isStoped2 = new AtomicBoolean(false);

  */

        // 为每个网络接口创建NetworkTest对象
        networkTest = new NetworkTest("eth0", handler, networkStatusTextView, this,  isRunning0);
        networkTest1 = new NetworkTest("eth1", handler1, networkStatusTextView1, this,  isRunning1);
        networkTest2 = new NetworkTest("eth2", handler2, networkStatusTextView2, this,  isRunning2);

        // 注册NetworkCallback
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)  // 以太网
                .build();
        connectivityManager.registerNetworkCallback(networkRequest, networkTest.networkCallback);
        connectivityManager.registerNetworkCallback(networkRequest, networkTest1.networkCallback);
        connectivityManager.registerNetworkCallback(networkRequest, networkTest2.networkCallback);

        // 开始测试
        networkTest.start();
        networkTest1.start();
        networkTest2.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消注册NetworkCallback
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(networkTest.networkCallback);
        connectivityManager.unregisterNetworkCallback(networkTest1.networkCallback);
        connectivityManager.unregisterNetworkCallback(networkTest2.networkCallback);
    }

    public class NetworkTest {
        private String interfaceName;
        private Handler handler;
        private TextView networkStatusTextView;
        private long startTime;
        private AtomicBoolean shouldStop = new AtomicBoolean(false);
        private AtomicBoolean isRunning;
        private double lastRecordedTime = 0;
        private String lastRecord = "00:00:00";
        private Context context;
        private ConnectivityManager.NetworkCallback networkCallback;
        private Network currentNetwork;
        private AtomicBoolean networkLost = new AtomicBoolean(false);
       // private Runnable runnable;
       // private AtomicBoolean isStopped = new AtomicBoolean(false);


        public NetworkTest(String interfaceName, Handler handler, TextView networkStatusTextView, Context context, AtomicBoolean isRunning) {
            this.interfaceName = interfaceName;
            this.handler = handler;
            this.networkStatusTextView = networkStatusTextView;
            this.startTime = System.currentTimeMillis();
            this.context = context;
            this.isRunning = isRunning;
      //      this.isStopped = isStopped;
            this.networkCallback = new ConnectivityManager.NetworkCallback() {
           /*     @Override
                public void onAvailable(Network network) {
                    NetworkInterface networkInterface = getNetworkInterface(network);
                    if (networkInterface != null && networkInterface.getName().equals(interfaceName)) {
                        if(isStopped.get())
                        {
                            return;
                        }
                        currentNetwork = network;
                        if (!isRunning.get()) {
                            isRunning.set(true);
                            shouldStop.set(false);
                            startTime = System.currentTimeMillis();

                            handler.post(runnable);
                        }
                    }
                } */

                @Override
                public void onLost(Network network) {
                    if (network.equals(currentNetwork)) {
                        NetworkInterface networkInterface = getNetworkInterface(network);
                        if (networkInterface != null && networkInterface.getName().equals(interfaceName)) {
                            isRunning.set(false);
                            handler.removeCallbacks(runnable);
                            shouldStop.set(true);
                            networkLost.set(true);
                          //  isStopped.set(true);
                            networkStatusTextView.setText("测试失败" + "\nDuration:" + lastRecord);
                            Log.d("NetworkTest", "Interface disconnected: " + interfaceName);
                        }
                    }
                }
            };
        }

        private NetworkInterface getNetworkInterface(Network network) {
            try {
                for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    if (networkInterface.getName().equals(interfaceName)) {
                        return networkInterface;
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void start() {
            if (!isRunning.get()) {
                isRunning.set(true);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!isRunning.get()) {
                            Log.d("NetworkTest", "Not running" + "ipname" + interfaceName );
                            return;
                        }

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (shouldStop.get()) {
                                    return;
                                }
                                Log.d("NetworkTestIP0", "Interface " + interfaceName );
                           //     if (!NetworkUtils.isInterfaceConnected(interfaceName, context))
                                    if (NetworkUtils.getNetWorkIp(interfaceName) == null)
                                {
                                    networkStatusTextView.setText("测试失败" + "\nDuration:" + lastRecord);
                                    isRunning.set(false);
                                    Log.d("NetworkTestIP", "Interface " + interfaceName + " is not connected");
                                    return;
                                }

                                final String ip = NetworkUtils.getNetWorkIp(interfaceName);
                                Log.d("NetworkTest", "Interface: " + interfaceName + ", IP: " + ip);
                                final String pingResult = NetworkUtils.ping("www.qq.com");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //if (!isRunning.get() || shouldStop.get() || networkLost.get())
                                            if (!isRunning.get() )
                                            {
                                                return;
                                            }
                                            if(shouldStop.get() )

                                        {
                                            networkStatusTextView.setText("测试失败1" + "\nDuration:" + lastRecord);
                                          //  isStopped.set(true);
                                            shouldStop.set(true);
                                            isRunning.set(false);
                                            return;
                                        }
                                        long currentTime = System.currentTimeMillis();
                                        long duration = currentTime - startTime;
                                        double durationInSeconds = duration / 1000.0;
                                        int hours = (int) (durationInSeconds / 3600);
                                        int minutes = (int) ((durationInSeconds % 3600) / 60);
                                        int seconds = (int) (durationInSeconds % 60);
                                        String formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                                        lastRecord = formattedDuration;
                                        networkStatusTextView.setText("IP: " + ip + "\nPing result: " + pingResult + "\nDuration: " + formattedDuration);
                                    }
                                });
                            }
                        }).start();

                        handler.postDelayed(this, 1000);
                    }
                };
                handler.post(runnable);
            }
        }
    }






  /*  public class NetworkTest {
        private String interfaceName;
        private Handler handler;
        private TextView networkStatusTextView;
        private long startTime;
        private AtomicBoolean shouldStop = new AtomicBoolean(false);
        private boolean isRunning = false;
        private double lastRecordedTime = 0;
        private Context context;
        private ConnectivityManager.NetworkCallback networkCallback;  // 新增

        public NetworkTest(String interfaceName, Handler handler, TextView networkStatusTextView, Context context) {
            this.interfaceName = interfaceName;
            this.handler = handler;
            this.networkStatusTextView = networkStatusTextView;
            this.startTime = System.currentTimeMillis();
            this.context = context;
            this.networkCallback = new ConnectivityManager.NetworkCallback() {  // 新增
                @Override
                public void onAvailable(Network network) {
                    // 网络连接可用时调用
                    Log.d("NetworkTest", "Network is available");
                    if (!isRunning) {
                        isRunning = true;
                        shouldStop.set(false);  // 允许新线程
                        startTime = System.currentTimeMillis();  // 重新开始计时
                        handler.post(runnable);  // 重新开始Runnable
                    }
                }

                @Override
                public void onLost(Network network) {
                    // 网络连接丢失时调用
                    Log.d("NetworkTest", "Network is lost");
                    isRunning = false;
                    handler.removeCallbacks(runnable);  // 停止Runnable
                    shouldStop.set(true);  // 停止新线程
                    // 更新UI
                    networkStatusTextView.setText("测试失败" + "\nDuration:" + lastRecordedTime + " s");
                    // 打印当前的interfaceName
                    Log.d("NetworkTest", "Interface disconnected: " + interfaceName);
                }
            };
        }

        public void start() {
            if (!isRunning) {
                isRunning = true;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!isRunning) {
                            Log.d("NetworkTest", "Not running");
                            return;
                        }

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (shouldStop.get()) {
                                    return;
                                }

                                // 检查网络接口是否已经启动
                                if (!NetworkUtils.isInterfaceConnected(interfaceName, context)) {
                                    Log.d("NetworkTest", "Interface " + interfaceName + " is not connected");
                                    return;
                                }

                                final String ip = NetworkUtils.getNetWorkIp(interfaceName);
                                Log.d("NetworkTest", "ip" + interfaceName);
                                final String pingResult = NetworkUtils.ping("www.qq.com");

                                // 在主线程中更新TextView
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isRunning || shouldStop.get()) {
                                            networkStatusTextView.setText("测试失败" + "\nDuration:" + lastRecordedTime + " s");
                                            isRunning = false;
                                            return;
                                        }
                                        long currentTime = System.currentTimeMillis();
                                        long duration = currentTime - startTime;
                                        double durationInSeconds = duration / 1000.0;
                                        lastRecordedTime = durationInSeconds;  // 更新lastRecordedTime的值
                                        networkStatusTextView.setText("IP: " + ip + "\nPing result: " + pingResult + "\nDuration: " + durationInSeconds + " s");
                                    }
                                });
                            }
                        }).start();

                        // 每0.5秒执行一次
                        handler.postDelayed(this, 500);
                    }
                };
                handler.post(runnable);
            }
        }
    }  */

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
        public static boolean isNetworkConnected(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        public static boolean isInterfaceConnected(String interfaceName, Context context) {
            try {
                NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
                return networkInterface != null && networkInterface.isUp();
            } catch (SocketException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}



