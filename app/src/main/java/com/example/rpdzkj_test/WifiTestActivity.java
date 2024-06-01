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



public class WifiTestActivity extends AppCompatActivity {

    private TextView networkStatusTextView;

    private Runnable runnable;
    private Handler handler = new Handler();
    private NetworkTest networkTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_test);
        networkStatusTextView = findViewById(R.id.wifi_network_status_text_view);

        // 初始化isRunning
        AtomicBoolean isRunning0 = new AtomicBoolean(false);

        // 为每个网络接口创建NetworkTest对象
        networkTest = new NetworkTest("wlan0", handler, networkStatusTextView, this,  isRunning0);

        // 注册NetworkCallback
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)  // wlan
                .build();
        connectivityManager.registerNetworkCallback(networkRequest, networkTest.networkCallback);

        // 开始测试
        networkTest.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消注册NetworkCallback
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(networkTest.networkCallback);
    }

    public class NetworkTest {
        private String interfaceName;
        private Handler handler;
        private TextView networkStatusTextView;
        private long startTime;
        private AtomicBoolean shouldStop = new AtomicBoolean(false);
        private AtomicBoolean isRunning;
        private double lastRecordedTime = 0;
        private Context context;
        private ConnectivityManager.NetworkCallback networkCallback;
        private Network currentNetwork;
        private AtomicBoolean networkLost = new AtomicBoolean(false);


        public NetworkTest(String interfaceName, Handler handler, TextView networkStatusTextView, Context context, AtomicBoolean isRunning) {
            this.interfaceName = interfaceName;
            this.handler = handler;
            this.networkStatusTextView = networkStatusTextView;
            this.startTime = System.currentTimeMillis();
            this.context = context;
            this.isRunning = isRunning;
            this.networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onLost(Network network) {
                    if (network.equals(currentNetwork)) {
                        NetworkInterface networkInterface = getNetworkInterface(network);
                        if (networkInterface != null && networkInterface.getName().equals(interfaceName)) {
                            isRunning.set(false);
                            handler.removeCallbacks(runnable);
                            shouldStop.set(true);
                            networkLost.set(true);
                            networkStatusTextView.setText("测试失败" + "\nDuration:" + lastRecordedTime + " s");
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
                                   // networkStatusTextView.setText("测试失败" + "\nDuration:" + lastRecordedTime + " s");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            networkStatusTextView.setText("测试失败" + "\nDuration:" + lastRecordedTime + " s");
                                        }
                                    });

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
                                            networkStatusTextView.setText("测试失败1" + "\nDuration:" + lastRecordedTime + " s");
                                            //  isStopped.set(true);
                                            shouldStop.set(true);
                                            isRunning.set(false);
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

                        handler.postDelayed(this, 1000);
                    }
                };
                handler.post(runnable);
            }
        }
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
