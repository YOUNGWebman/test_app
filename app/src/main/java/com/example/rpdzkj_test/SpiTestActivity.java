package com.example.rpdzkj_test;

import com.example.rpdzkj_test.TimeDisplay;
import java.io.File;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;



import android.view.View;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.Gravity;

import android.app.ProgressDialog;
import android.os.Process;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.*;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.io.*;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.GridLayout;
import android.util.DisplayMetrics;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.nio.ByteBuffer;
import com.example.rpdzkj_test.TestInfo;




import androidx.appcompat.app.AppCompatActivity;

public class SpiTestActivity extends AppCompatActivity {
     private  TextView sumTextView;
     private  int sum = 0;

    final String tx = "RPDZKJTEST";
    String targetDev;
    ProgressDialog progressDialog;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    //private static final String SPI_DEVICE_PATH = "/dev/spidev0.0";
    private static final int TEST_COUNT = 100;

    private TextView mTextViewSendContent;
    private TextView mTextViewReceiveContent;
    private TextView mTextViewTestCount;
    private TextView mText1ViewSendContent;
    private TextView mText1ViewReceiveContent;
    private TextView mText1ViewTestCount;
    private TextView mText2ViewSendContent;
    private TextView mText2ViewReceiveContent;
    private TextView mText2ViewTestCount;
    private boolean timerEnabled;
    private int timeInSeconds;

    private Handler handler;
    private Runnable runnable;


    private AtomicBoolean  shouldPause = new AtomicBoolean(false); // 标志是否应暂停


    private static final String TAG = "CanTestActivity";

    private int  successCount = 0;

    private SpiTest spitest0, spitest1, spitest2;
    private TimeDisplay timeDisplay;
    private  long startTime = 0;
    private TextView getTimeTextView;
    private TestInfo testInfo;




    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_spi_test);
        getTimeTextView = findViewById(R.id.spiTime);
        Button startButton = findViewById(R.id.start_spi_test_button);
        Intent intent = getIntent();
        timerEnabled = intent.getBooleanExtra("TIMER", false);
        timeInSeconds = intent.getIntExtra("TIME_IN_SECONDS", 0);
        testInfo = new TestInfo(this);
        File saveFile = new File(getFilesDir(), "saved_ids.html");
        testInfo.setSavedFile(saveFile);

        File f00= new File("/sys/class/spidev/spidev0.0");
        if (f00.exists()){
            sum++;
        }

        File f01= new File("/sys/class/spidev/spidev1.0");
        if (f01.exists()){
            sum++;
        }

        File f02= new File("/sys/class/spidev/spidev2.0");
        if (f02.exists()){
            sum++;
        }
        // 初始化isRunning
        AtomicBoolean isRunning0 = new AtomicBoolean(true);
        AtomicBoolean isRunning1 = new AtomicBoolean(true);
        AtomicBoolean isRunning2 = new AtomicBoolean(true);

        sumTextView = findViewById(R.id.spiString);
        sumTextView.setText("spi总数为： " + sum);

        mTextViewSendContent = findViewById(R.id.spi0Send);
        mTextViewReceiveContent = findViewById(R.id.spi0Recive);
        mTextViewTestCount = findViewById(R.id.spi0String);

        mText1ViewSendContent = findViewById(R.id.spi1Send);
        mText1ViewReceiveContent = findViewById(R.id.spi1Recive);
        mText1ViewTestCount = findViewById(R.id.spi1String);

        mText2ViewSendContent = findViewById(R.id.spi2Send);
        mText2ViewReceiveContent = findViewById(R.id.spi2Recive);
        mText2ViewTestCount = findViewById(R.id.spi2String);

        progressDialog = new ProgressDialog(this);
        spitest0 = new SpiTest();
        spitest1 = new SpiTest();
        spitest2 = new SpiTest();

        timeDisplay = new TimeDisplay(getTimeTextView);


            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timeDisplay.start();
                    if(!f00.exists() && !f01.exists() && !f02.exists() )
                    {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("TIMER", true);
                        resultIntent.putExtra("NODE_NOT_EXIST", true);
                        resultIntent.putExtra("TIME_IN_SECONDS", timeInSeconds);

                        setResult(RESULT_OK, resultIntent);
                        { finish();}
                    }
                    if (f00.exists()) {
                        String cmd = "sc16is752 -d " + "/dev/spidev0.0" + " -s " + tx + tx;
                        // mTextViewSendContent.setText(cmd);
                        spitest0.runCmd(cmd, 5, mTextViewSendContent, mTextViewReceiveContent, mTextViewTestCount, isRunning0);
                    } else {
                        mTextViewSendContent.setText("未识别到节点");
                        mTextViewReceiveContent.setText("未识别到节点");
                        mTextViewTestCount.setText("测试失败");
                    }

                    if (f01.exists()) {
                        String cmd1 = "sc16is752 -d " + "/dev/spidev1.0" + " -s " + tx + tx;
                        // mText1ViewSendContent.setText(cmd1);
                        spitest1.runCmd(cmd1, 5, mText1ViewSendContent, mText1ViewReceiveContent, mText1ViewTestCount, isRunning1);

                    } else {
                        mText1ViewSendContent.setText("未识别到节点");
                        mText1ViewReceiveContent.setText("未识别到节点");
                        mText1ViewTestCount.setText("测试失败");
                    }

                    if (f02.exists()) {
                        String cmd2 = "sc16is752 -d " + "/dev/spidev2.0" + " -s " + tx + tx;
                        // mText2ViewSendContent.setText(cmd2);
                        spitest2.runCmd(cmd2, 5, mText2ViewSendContent, mText2ViewReceiveContent, mText2ViewTestCount, isRunning2);
                    } else {
                        mText2ViewSendContent.setText("未识别到节点");
                        mText2ViewReceiveContent.setText("未识别到节点");
                        mText2ViewTestCount.setText("测试失败");
                    }

                }
            });

        if (timerEnabled) {
            int timeInMillis = timeInSeconds * 1000;

            startButton.performClick();
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
                    String spiCounts = sumTextView.getText().toString();
                    String spi0Rc = mTextViewReceiveContent.getText().toString();
                    String spi1Rc = mText1ViewReceiveContent.getText().toString();
                    String spi2Rc = mText2ViewReceiveContent.getText().toString();
                    String spi0Times = mTextViewTestCount.getText().toString();
                    String spi1Times = mText1ViewTestCount.getText().toString();
                    String spi2Times = mText2ViewTestCount.getText().toString();
                    testInfo.appendAdditionalSpiTestContent(spiCounts, spi0Rc, spi0Times, spi1Rc, spi1Times, spi2Rc, spi2Times);
                    finish();
                }
            }, timeInMillis);
        }

        }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent(SpiTestActivity.this, MainActivity.class);
            setResult(RESULT_OK, intent); // 返回结果
            finish(); // 确保调用 finish() 方法
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeDisplay.stop();
    }

    public class SpiTest {
        private AtomicBoolean flag = new AtomicBoolean(true);
        private Handler handler = new Handler(Looper.getMainLooper());

        public void runCmd(String cmd, long timeout, TextView sendTextView, TextView receiveTextView, TextView countTextView, AtomicBoolean flag) {


            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (flag.get())
                    {
                        java.lang.Process process = null;
                        int exitVal = -1;
                        DataOutputStream os = null;
                        BufferedReader reader = null;
                        StringBuilder output = new StringBuilder();
                        try {
                            process = Runtime.getRuntime().exec("su");
                            os = new DataOutputStream(process.getOutputStream());

                            os.writeBytes(cmd + "\n");
                            os.writeBytes("exit\n");
                            os.flush();
                            try {
                                Thread.sleep(1000);  // 延迟1秒
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            boolean processFinished = process.waitFor(timeout, TimeUnit.SECONDS);

                            if (processFinished) {
                                exitVal = process.exitValue();
                                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    output.append(line).append("\n");
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

                            final int finalExitVal = exitVal;
                            final String finalOutput = output.toString();

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            android.util.Log.d("RRRRRR", "cmd put \n" + finalOutput);
                                            if (finalOutput.contains(tx)) {
                                                sendTextView.setText("测试成功");
                                                receiveTextView.setText(finalOutput); // 显示接收的内容
                                                successCount++;
                                                countTextView.setText("成功次数: " + successCount); // 更新成功次数
                                            } else if (finalExitVal == -1) {
                                                shouldPause.set(true);
                                                timeDisplay.pause();
                                                sendTextView.setText("测试超时");
                                                receiveTextView.setText(finalOutput); // 显示接收的内容
                                                android.util.Log.d("RRRRRR", "超时");
                                                flag.set(false);
                                            } else {
                                                shouldPause.set(true);
                                                timeDisplay.pause();
                                                sendTextView.setText("测试失败");
                                                receiveTextView.setText(finalOutput); // 显示接收的内容
                                                android.util.Log.d("RRRRRR", "失败");
                                                flag.set(false);
                                            }
                                        }
                                    });
                                }
                            }, 3000);  // 延迟1秒
                        }
                    }
                }
            }).start();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }






}
