package com.example.rpdzkj_test;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import com.example.rpdzkj_test.TimeDisplay;
import android.content.SharedPreferences;
import android.content.Context;
import com.example.rpdzkj_test.TestInfo;

import android.content.Intent;
import android.os.Bundle;
import java.lang.Thread;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
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

import android.widget.CheckBox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import android.view.View;
import android.view.LayoutInflater;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleObserver;


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
    private AtomicBoolean  shouldPause = new AtomicBoolean(false); // 标志是否应暂停
    private boolean timerEnabled;
    private int timeInSeconds;
    private Button startButton;
    private TestInfo testInfo;
    int[] array = new int[10]; // Java中的数组默认初始化为0
    private TimeDisplay timeDisplay;
    // private  long startTime = 0;
    private TextView getTimeTextView;
    ExecutorService executor = null;
    private int[] testCounts;
    List<String> selectedTtys = new ArrayList<>();
    List<Integer> selectedCounts = new ArrayList<>();


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

        Iterator<File> iterator = list.iterator();
        while (iterator.hasNext()){
            File file = iterator.next();
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
        textViewLayout = (GridLayout)findViewById(R.id.textViewLayout);

        final CountDownLatch latch = new CountDownLatch(1);
        final int w = width;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = LayoutInflater.from(UartTestActivity.this);
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                for (int i = 0; i < uartsToTest.size(); i ++) {
                    // 创建和初始化params变量
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = w;
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.setMargins(margin, margin *2, margin, margin);

                    // 使用params变量
                    CheckBox checkBox = new CheckBox(UartTestActivity.this);
                    checkBox.setText(uartsToTest.get(i).getName());
                    checkBox.setChecked(prefs.getBoolean("checkbox_state_" + i, false));
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

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
        for (int i = 0; i < checkBoxArrayList.size(); i++) {
            editor.putBoolean("checkbox_state_" + i, checkBoxArrayList.get(i).isChecked());
        }
        editor.apply();
    }


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

   /* public void doTest() {
        executor = Executors.newFixedThreadPool(10);
        String str = "0123";
        final String testString = str + str;
        String uartSettings = "fffffff4:4:1cb2:a30:3:1c:7f:15:4:0:1:0:11:13:1a:0:12:f:17:16:0:0:0";
        selectedTtys.clear();
        //  selectedCounts.clear();

        for (int i = 0; i < checkBoxArrayList.size(); i++) {
            CheckBox checkBox = checkBoxArrayList.get(i);
            if (checkBox.isChecked()) {
                File uart = uartsToTest.get(i);
                if (!selectedTtys.contains(uart.getName())) {
                    selectedTtys.add(uart.getName());
                   // selectedCounts.add(0); // 初始测试次数为0
                }
                Log.d("RRRRR do uart", uart.getName());
                final int finalI = i;

                Future<?> future = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        CmdResult result;
                        String testFilePath0 = "/data/";
                        upgradeRootPermission(testFilePath0);
                        String testFilePath = testFilePath0 + uart.getName() + ".txt";
                        upgradeRootPermission(testFilePath);
                        String cmdTouch = String.format("touch %s \n", testFilePath);
                        String cmd0 = String.format("stty -F %s %s \n", uart.getAbsolutePath(), uartSettings);
                        String cmd1 = String.format("cat %s > %s  &\n", uart.getAbsolutePath(), testFilePath);
                        String cmdW = String.format("echo \"%s\" > %s  \n", testString, uart.getAbsolutePath());
                        String cmdR = String.format("cat %s \n", testFilePath);
                        String cmdRM = String.format("rm %s \n", testFilePath);
                        String cmdClean = "killall cat\n";
                        runCmd(cmdTouch, 1);
                        runCmd(cmd0, 1);
                        runCmd(cmd1, 1);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runCmd(cmdW, 1);

                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        result = runCmd(cmdR, 1);
                        boolean isSuccess = testString.equals(result.output);
                        runCmd(cmdRM, 1);
                        runCmd(cmdClean, 1);
                        Log.d("RRRRRR result ", uart.getName() + " " + result.output);
                        runOnUiThread(() -> {
                            if (shouldStops.get(finalI).get()) {
                                return;
                            }

                            if (isSuccess) {
                                array[finalI]++;
                                testCounts[finalI]++;
                                sendTextViewList.get(finalI).setText("发送内容：" + testString);
                                receiveTextViewList.get(finalI).setText("接收内容：" + result.output);
                                countTextViewList.get(finalI).setText("测试次数：" + array[finalI]);
                            } else {
                                // 测试失败，停止测试
                                timeDisplay.pause();
                                shouldStops.get(finalI).set(true);
                                shouldPause.set(true);
                                sendTextViewList.get(finalI).setText("测试失败");
                                receiveTextViewList.get(finalI).setText("测试失败");
                                executor.shutdownNow();
                                Thread.currentThread().interrupt();
                            }
                        });
                    }
                });
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

    } */

    public void doTest() {
        executor = Executors.newFixedThreadPool(10);
        String str = "0123";
        final String testString = str + str;
        String uartSettings = "fffffff4:4:1cb2:a30:3:1c:7f:15:4:0:1:0:11:13:1a:0:12:f:17:16:0:0:0";
        selectedTtys.clear();

        for (int i = 0; i < checkBoxArrayList.size(); i++) {
            CheckBox checkBox = checkBoxArrayList.get(i);
            if (checkBox.isChecked()) {
                File uart = uartsToTest.get(i);
                if (!selectedTtys.contains(uart.getName())) {
                    selectedTtys.add(uart.getName());
                }
                Log.d("RRRRR do uart", uart.getName());
                final int finalI = i;

               /* Future<?> future = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            CmdResult result;
                            String testFilePath0 = "/data/";
                            upgradeRootPermission(testFilePath0);
                            String testFilePath = testFilePath0 + uart.getName() + ".txt";
                            upgradeRootPermission(testFilePath);
                            String cmdTouch = String.format("touch %s \n", testFilePath);
                            String cmd0 = String.format("stty -F %s %s \n", uart.getAbsolutePath(), uartSettings);
                            String cmd1 = String.format("cat %s > %s  &\n", uart.getAbsolutePath(), testFilePath);
                            String cmdW = String.format("echo \"%s\" > %s  \n", testString, uart.getAbsolutePath());
                            String cmdR = String.format("cat %s \n", testFilePath);
                            String cmdRM = String.format("rm %s \n", testFilePath);
                            String cmdClean = "killall cat\n";
                            runCmd(cmdTouch, 1);
                            runCmd(cmd0, 1);
                            runCmd(cmd1, 1);
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                            runCmd(cmdW, 1);
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                            result = runCmd(cmdR, 1);
                            boolean isSuccess = testString.equals(result.output);
                            runCmd(cmdRM, 1);
                            runCmd(cmdClean, 1);
                            Log.d("RRRRRR result ", uart.getName() + " " + result.output);
                            runOnUiThread(() -> {
                                if (shouldStops.get(finalI).get()) {
                                    return;
                                }
                                if (isSuccess) {
                                    array[finalI]++;
                                    testCounts[finalI]++;
                                    sendTextViewList.get(finalI).setText("发送内容：" + testString);
                                    receiveTextViewList.get(finalI).setText("接收内容：" + result.output);
                                    countTextViewList.get(finalI).setText("测试次数：" + array[finalI]);
                                } else {
                                    // 测试失败，停止测试
                                    timeDisplay.pause();
                                    shouldStops.get(finalI).set(true);
                                    shouldPause.set(true);
                                    sendTextViewList.get(finalI).setText("测试失败");
                                    receiveTextViewList.get(finalI).setText("测试失败");
                                    executor.shutdownNow();
                                    Thread.currentThread().interrupt();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (executor != null && executor.isTerminated()) {
                                executor.shutdown();
                            }
                        }
                    }
                }); */

                Future<?> future = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            CmdResult result;
                            String testFilePath0 = "/data/";
                            upgradeRootPermission(testFilePath0);
                            String testFilePath = testFilePath0 + uart.getName() + ".txt";
                            upgradeRootPermission(testFilePath);
                            String cmdTouch = String.format("touch %s \n", testFilePath);
                            String cmd0 = String.format("stty -F %s %s \n", uart.getAbsolutePath(), uartSettings);
                            String cmd1 = String.format("cat %s > %s  &\n", uart.getAbsolutePath(), testFilePath);
                            String cmdW = String.format("echo \"%s\" > %s  \n", testString, uart.getAbsolutePath());
                            String cmdR = String.format("cat %s \n", testFilePath);
                            String cmdRM = String.format("rm %s \n", testFilePath);
                            String cmdClean = "killall cat\n";
                            runCmd(cmdTouch, 1);
                            runCmd(cmd0, 1);
                            runCmd(cmd1, 1);
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                            runCmd(cmdW, 1);
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                            result = runCmd(cmdR, 1);
                            boolean isSuccess = testString.equals(result.output);
                            runCmd(cmdRM, 1);
                            runCmd(cmdClean, 1);
                            Log.d("RRRRRR result ", uart.getName() + " " + result.output);
                            runOnUiThread(() -> {
                                if (shouldStops.get(finalI).get()) {
                                    return;
                                }
                                if (isSuccess) {
                                    array[finalI]++;
                                    testCounts[finalI]++;
                                    sendTextViewList.get(finalI).setText("发送内容：" + testString);
                                    receiveTextViewList.get(finalI).setText("接收内容：" + result.output);
                                    countTextViewList.get(finalI).setText("测试次数：" + array[finalI]);
                                } else {
                                    timeDisplay.pause();
                                    shouldStops.get(finalI).set(true);
                                    shouldPause.set(true);
                                    sendTextViewList.get(finalI).setText("测试失败");
                                    receiveTextViewList.get(finalI).setText("测试失败");
                                    executor.shutdownNow();
                                    Thread.currentThread().interrupt();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (executor != null && !executor.isShutdown()) {
                                executor.shutdown();
                            }
                        }
                    }
                });


            }
        }

        try {
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }


    public class CmdResult {
        CmdResult(int exitVal, String output) {
            this.exitVal = exitVal;
            this.output = output;
        }
        public int exitVal;
        public String output;
    }

    final int margin = 2;
    final int col = 4;
    int width = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uart_test);
        Intent intent = getIntent();
        timerEnabled = intent.getBooleanExtra("TIMER", false);
        timeInSeconds = intent.getIntExtra("TIME_IN_SECONDS", 0);
        getTimeTextView = findViewById(R.id.uartTimeText);
        checkBoxArrayList = new ArrayList<>();
        testCounts = new int[10];


        resultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                CheckBox checkBox = (CheckBox) msg.obj;
                checkBox.setTextColor(msg.arg1);

            }
        };
        uartTestResultLayout = (GridLayout)findViewById(R.id.uartTestLayout);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        timeDisplay = new TimeDisplay(getTimeTextView);
        int screenWidth = displayMetrics.widthPixels;
        width = (screenWidth / col - 2 * margin);
        initTest();
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setEnabled(false);
                timeDisplay.start();
                // 创建一个新的ExecutorService
                executor = Executors.newSingleThreadExecutor();
                TestLifecycleObserver lifecycleObserver = new TestLifecycleObserver(executor, shouldPause, shouldStops);
                getLifecycle().addObserver(lifecycleObserver);
                // 在新的线程中循环执行doTest()
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (!Thread.currentThread().isInterrupted()) {
                                doTest();
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (executor != null && !executor.isShutdown()) {
                                executor.shutdown();
                            }
                        }
                    }
                });
            }
        });


        testInfo = new TestInfo(this);

        if (timerEnabled) {
            int timeInMillis = timeInSeconds * 1000;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                startButton.performClick();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }, 1000); //
                }
            });

            // 启动倒计时器，定时退出
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("TIMER", true);
                    resultIntent.putExtra("TIME_IN_SECONDS", timeInSeconds);
                    if( shouldPause.get())
                    {resultIntent.putExtra("TEST_PASS", false);}
                    else
                    {resultIntent.putExtra("TEST_PASS", true);}
                    setResult(RESULT_OK, resultIntent);
                  /*  if (!shouldPause.get() && !isFinishing()) {

                        finish();
                    } */
                    // 获取 UART 信息并追加到 HTML 文件中

                    File saveFile = new File(getFilesDir(), "saved_ids.html");
                    testInfo.setSavedFile(saveFile);
                    String uartInfo = getUsableUARTAsString();
                    System.out.println(uartInfo);
                    // 构建选中的 ttyS 字符串
                    String selectedTtysString = String.join(" ", selectedTtys);
                    // 保存测试信息
                    testInfo.appendAdditionalContent(uartInfo, testCounts, selectedTtysString);
                    finish();
                }
            }, timeInMillis);
        }
    }



    public String getUsableUARTAsString() {
        ArrayList<File> uartFiles = getUsableUART();
        if (uartFiles != null) {
            StringBuilder uartString = new StringBuilder();
            for (File file : uartFiles) {
                uartString.append(file.getAbsolutePath()).append("\n");
            }
            return uartString.toString();
        } else {
            return "No usable UART found";
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent(UartTestActivity.this, MainActivity.class);
            setResult(RESULT_OK, intent); // 返回结果
            finish(); // 确保调用 finish() 方法
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

/*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止后台任务
        stopBackgroundTask();
        // 检查并关闭线程池
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }

        // 等待所有任务终止
        try {
            if (executor != null && !executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("UART Test", "Activity 销毁，进入 onDestroy 方法");
        // 停止timeDisplay
        if (timeDisplay != null) {
            timeDisplay.pause();
        }
        // 停止执行器并尝试中断所有任务
        if (executor != null || !executor.isShutdown()) {
            Log.e("UART Test", "开始线程回收");
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    Log.e("UART Test", "线程终止");
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        Log.e("UART Test", "线程池未能终止");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        // 停止所有其他正在进行的任务
        shouldPause.set(true);
        for (AtomicBoolean stopFlag : shouldStops) {
            stopFlag.set(true);
        }


    }




    private void stopBackgroundTask() {
        // 检查是否有未完成的任务并取消
        terminateAllCatProcesses();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }



    public void terminateAllCatProcesses() {
        try {
            Runtime.getRuntime().exec("killall cat");
        } catch (IOException e) {
            e.printStackTrace();
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
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    /*public class TestLifecycleObserver implements LifecycleObserver {
        private final ExecutorService executor;
        private final AtomicBoolean shouldPause;
        private final List<AtomicBoolean> shouldStops;

        public TestLifecycleObserver(ExecutorService executor, AtomicBoolean shouldPause, List<AtomicBoolean> shouldStops) {
            this.executor = executor;
            this.shouldPause = shouldPause;
            this.shouldStops = shouldStops;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy() {
            Log.e("UART Test", "LifecycleObserver - onDestroy triggered");
            if (executor != null && !executor.isShutdown()) {
                Log.e("UART Test", "开始线程回收");
                executor.shutdownNow();
                try {
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                        Log.e("UART Test", "线程终止");
                        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                            Log.e("UART Test", "线程池未能终止");
                        }
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            shouldPause.set(true);
            for (AtomicBoolean stopFlag : shouldStops) {
                stopFlag.set(true);
            }
            owner.getLifecycle().removeObserver(this);
        }
    } */
    public class TestLifecycleObserver implements LifecycleObserver {
        private final ExecutorService executor;
        private final AtomicBoolean shouldPause;
        private final List<AtomicBoolean> shouldStops;

        public TestLifecycleObserver(ExecutorService executor, AtomicBoolean shouldPause, List<AtomicBoolean> shouldStops) {
            this.executor = executor;
            this.shouldPause = shouldPause;
            this.shouldStops = shouldStops;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy(LifecycleOwner owner) {
            Log.e("UART Test", "LifecycleObserver - onDestroy triggered");
            if (executor != null && !executor.isShutdown()) {
                Log.e("UART Test", "开始线程回收");
                executor.shutdownNow();
                try {
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                        Log.e("UART Test", "线程终止");
                        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                            Log.e("UART Test", "线程池未能终止");
                        }
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            shouldPause.set(true);
            for (AtomicBoolean stopFlag : shouldStops) {
                stopFlag.set(true);
            }
            // 在调用移除观察者前添加日志
            Log.e("UART Test", "尝试移除观察者");
            // 取消观察者，以避免内存泄漏
            owner.getLifecycle().removeObserver(this);
            // 在调用移除观察者后添加日志
            Log.e("UART Test", "观察者已移除");
        }
    }


}
