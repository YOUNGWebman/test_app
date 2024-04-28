package com.example.rpdzkj_test;
import java.util.concurrent.CountDownLatch;


import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import java.lang.Thread;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Button;

import java.util.Iterator;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.TypedValue;

import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import android.view.View;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;








import androidx.appcompat.app.AppCompatActivity;

public class UartTestActivity extends AppCompatActivity {

    ArrayList<File> uartsToTest = null;
    GridLayout uartTestResultLayout = null;
    GridLayout textViewLayout = null;
    Runnable runnable = null;
    Handler resultHandler = null;
    ArrayList<CheckBox> checkBoxArrayList = null;
    ArrayList<TextView> sendTextViewList = null;
    ArrayList<TextView> receiveTextViewList = null;
    ArrayList<TextView> countTextViewList = null;
    List<AtomicBoolean> shouldStops = null;
    int successCount =0;
    int failureCount =0;
    int lastCount = 0;
    ExecutorService executor = null;
   // private final AtomicBoolean shouldStop = new AtomicBoolean(false);


    private ArrayList<File> getUsableUART() {
        File file = new File("/dev/");
        FilenameFilter filenameFilter = (file1, s) -> (s.contains("ttyS") || s.contains("ttyAS"));
        File[] files = file.listFiles(filenameFilter);

        if (files != null)
            return new ArrayList<>(Arrays.asList(files));

        return null;
    }

    public ArrayList<File> filterUart(ArrayList<File> list) {
        String vendorName = runCmd("getprop ro.hardware", 1).output;
        String deviceName = runCmd("getprop ro.product.device", 1).output;
        String modelName = runCmd("echo `cat /sys/firmware/devicetree/base/model`", 1).output;

        if(vendorName.equals("") || deviceName.equals("") || modelName.equals(""))
            return null;
        Log.d("BoardInfo", String.format("vendor [%s]\ndevice [%s]\nmodel  [%s]", vendorName, deviceName, modelName));

   //     String ignored = parseIgnoreValues(vendorName, deviceName, modelName);

        Iterator<File> iterator = list.iterator();
        while (iterator.hasNext()){
            File file = iterator.next();
           // if(ignored.contains(file.getName()))
             //   iterator.remove();
        }

        return list;
    }

    public boolean initTest() {
        uartsToTest = filterUart(getUsableUART());

        if(uartsToTest == null)
            return false;

        checkBoxArrayList = new ArrayList<>();
        sendTextViewList = new ArrayList<>();
        receiveTextViewList = new ArrayList<>();
        countTextViewList = new ArrayList<>();
        shouldStops = new ArrayList<>();

        // 新增的代码
      //  GridLayout textViewLayout = null;
        textViewLayout = (GridLayout)findViewById(R.id.textViewLayout);

        final CountDownLatch latch = new CountDownLatch(1);
        final int w = width;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(UartTestActivity.this);

                for (int i = 0; i < uartsToTest.size(); i ++) {
                    // 创建和初始化params变量
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = w;
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.setMargins(margin, margin *2, margin, margin);

                    // 使用params变量
                    CheckBox checkBox = new CheckBox(UartTestActivity.this);
                    checkBox.setText(uartsToTest.get(i).getName());
                    uartTestResultLayout.addView(checkBox, params);
                    checkBoxArrayList.add(checkBox);

                    // 创建和初始化view变量
                    View view = inflater.inflate(R.layout.activity_uart_item_test, null);

                    TextView sendTextView = view.findViewById(R.id.sendTextView);
                    sendTextViewList.add(sendTextView);

                    TextView receiveTextView = view.findViewById(R.id.receiveTextView);
                    receiveTextViewList.add(receiveTextView);

                    TextView countTextView = view.findViewById(R.id.countTextView);
                    countTextViewList.add(countTextView);

                    AtomicBoolean shouldStop = new AtomicBoolean(false);
                    shouldStops.add(shouldStop);

                    // 将整个view添加到textViewLayout
                    textViewLayout.addView(view, params);
                }

                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }


   /* public boolean initTest() {
        uartsToTest = filterUart(getUsableUART());

        if(uartsToTest == null)
            return false;

        checkBoxArrayList = new ArrayList<>();
        sendTextViewList = new ArrayList<>();
        receiveTextViewList = new ArrayList<>();
        countTextViewList = new ArrayList<>();

        final CountDownLatch latch = new CountDownLatch(1);
        final int w = width;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


               LayoutInflater inflater = LayoutInflater.from(UartTestActivity.this);
                for (int i = 0; i < uartsToTest.size(); i ++) {
                    // 创建和初始化params变量
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = w;
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.setMargins(margin, margin *4, margin, margin);

                    // 使用params变量
                    CheckBox checkBox = new CheckBox(UartTestActivity.this);
                    checkBox.setText(uartsToTest.get(i).getName());
                    uartTestResultLayout.addView(checkBox, params);
                    checkBoxArrayList.add(checkBox);

                    // 创建和初始化view变量
                    View view = inflater.inflate(R.layout.activity_uart_item_test, null);

                    TextView sendTextView = view.findViewById(R.id.sendTextView);
                    sendTextViewList.add(sendTextView);

                    TextView receiveTextView = view.findViewById(R.id.receiveTextView);
                    receiveTextViewList.add(receiveTextView);

                    TextView countTextView = view.findViewById(R.id.countTextView);
                    countTextViewList.add(countTextView);

                    // 将整个view添加到uartTestResultLayout
                    uartTestResultLayout.addView(view, params);
                }

                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    } */



    public CmdResult runCmd(String cmd, int timeout) {
        java.lang.Process process = null;
        int exitVal = -1;
        DataOutputStream os = null;
        BufferedReader reader = null;
        StringBuilder output = new StringBuilder();

        try {
            process = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(process.getOutputStream());
            Log.d("RRRRR cmd ", cmd);
            os.writeBytes(cmd + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();

            boolean processFinished = process.waitFor(timeout, TimeUnit.SECONDS);

            exitVal = process.exitValue();
            if (processFinished) {
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            } else {
                process.destroyForcibly();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) os.close();
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) process.destroy();
        }

        return new CmdResult(exitVal, output.toString());
    }

public void doTest() {
        executor = Executors.newFixedThreadPool(10);
        String str = "012345678";
        final String testString =  str + str;
        String uartSettings = "fffffff4:4:1cb2:a30:3:1c:7f:15:4:0:1:0:11:13:1a:0:12:f:17:16:0:0:0";

        for (int i = 0; i < checkBoxArrayList.size(); i ++) {
            CheckBox checkBox = checkBoxArrayList.get(i);
            if (checkBox.isChecked()) {
                File uart = uartsToTest.get(i);
                Log.d("RRRRR do uart", uart.getName());
                final int finalI = i;


                Future<?> future = executor.submit(new Runnable() {
                    @Override
                    public void run() {

                        CmdResult result;
                        String testFilePath0 =  "/data/" ;
                        upgradeRootPermission(testFilePath0);
                        String testFilePath = testFilePath0 + uart.getName() + ".txt";
                        upgradeRootPermission(testFilePath);
                        String cmdTouch = String.format("touch %s \n", testFilePath);
                        String cmd0 = String.format("stty -F %s %s \n", uart.getAbsolutePath(), uartSettings);
                        String cmd1 = String.format("cat %s > %s & \n", uart.getAbsolutePath(), testFilePath);
                        String cmdW = String.format("echo \"%s\" > %s \n", testString, uart.getAbsolutePath());
                        String cmdR = String.format("cat %s \n", testFilePath);
                        String cmdRM = String.format("rm %s \n", testFilePath);
                        String cmdClean = "killall cat\n";

                        runCmd(cmdTouch, 1);
                        runCmd(cmd0, 1);
                        runCmd(cmd1, 1);
                        runCmd(cmdW, 1);
                        result = runCmd(cmdR, 1);
                        runCmd(cmdRM, 1);
                        runCmd(cmdClean, 1);
                        Log.d("RRRRRR result ", uart.getName() + " " + result.output);

                        boolean isSuccess = testString.equals(result.output);
                      //  final AtomicBoolean shouldStop = new AtomicBoolean(false);


                        runOnUiThread(() -> {
                           if (shouldStops.get(finalI).get()) {
                               // 测试已经失败，不再更新UI
                               return;
                           }
                            if (isSuccess && !shouldStops.get(finalI).get()) {
                                //if (!shouldStop.get()) {
                                    successCount++;
                                    sendTextViewList.get(finalI).setText("发送内容：" + testString);
                                    receiveTextViewList.get(finalI).setText("接收内容：" + result.output);
                                    countTextViewList.get(finalI).setText( "测试次数：" + successCount );
                                //}
                            } else {
                                // 测试失败，停止测试
                                shouldStops.get(finalI).set(true);
                                sendTextViewList.get(finalI).setText("测试失败");
                                receiveTextViewList.get(finalI).setText("测试失败");
                                // 停止增加successCount
                                executor.shutdownNow();
                                Thread.currentThread().interrupt();
                            }
                        });

                    }
                });
            }
        }

        executor.shutdown();
    }












    public class CmdResult {
        CmdResult(int exitVal, String output) {
            this.exitVal = exitVal;
            this.output = output;
        }
        public int exitVal;
        public String output;
    }

    final int margin = 7;
    final int col = 4;
    int width = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uart_test);
        // 初始化checkBoxArrayList
       // checkBoxArrayList = new ArrayList<>();
        resultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                CheckBox checkBox = (CheckBox) msg.obj;
                checkBox.setTextColor(msg.arg1);
            //    Button btn = (Button) msg.obj;
              //  btn.setBackgroundTintList(ColorStateList.valueOf(msg.arg1));
            }
        };

        //ControlButtonUtil.initControlButtonView(this);

        uartTestResultLayout = (GridLayout)findViewById(R.id.uartTestLayout);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        width = (screenWidth / col - 2 * margin);

     /*  new Thread(() -> {
            if(initTest())
                doTest();
        }).start(); */

        initTest();
     /*   Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里开始测试
                doTest();
            }
        }); */

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setEnabled(false);
                // 创建一个新的ExecutorService
                executor = Executors.newSingleThreadExecutor();
                // 在新的线程中循环执行doTest()
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        while (!Thread.currentThread().isInterrupted()) {
                            doTest();
                            try {
                                Thread.sleep(1500); // 2 秒延迟
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                });
            }
        });

    /*   Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setEnabled(true);
                // 停止所有正在进行的测试
                if (executor != null) {
                    executor.shutdownNow();
                }
            }
        }); */

      /*  Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setEnabled(true);
                // 停止所有正在进行的测试
                if (executor != null) {
                    executor.shutdownNow();
                }
                // 设置shouldStop为true
                shouldStop.set(true);
            }
        }); */



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
          //  Log.e(TAG, "Error upgrading root permission", e);
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            //    Log.e(TAG, "Error closing process", e);
            }
        }
        return true;
    }
}
