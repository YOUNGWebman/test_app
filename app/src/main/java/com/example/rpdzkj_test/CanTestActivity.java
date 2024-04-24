package com.example.rpdzkj_test;

import java.io.File;


import android.view.View;

import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;




import androidx.appcompat.app.AppCompatActivity;

public class CanTestActivity extends AppCompatActivity {
    int[] final_flag = {0,0,0};
    int canTotal = 0;
    int canOK = 0;
    int can0_count = 0;
    boolean can0_flag = false;

    int can1_count = 0;
    boolean can1_flag = false;
    int can2_count = 0;
    boolean can2_flag = false;
    String cmd0Touch = String.format("touch /data/can0.txt");
    String cmd01="ip link set can0 down";
    String cmd02="ip link set can0 type can bitrate 100000";
    String cmd03="ip link set can0 up";
    String cmd004="candump can0 &";
    String cmd04="candump can0 > /data/can0.txt &";
    String cmd05="cansend can0 123#11111111";
    String cmd0RM = String.format("rm /data/can0.txt");

    String cmd1Touch = String.format("touch /data/can1.txt");
    String cmd11="ip link set can1 down";
    String cmd12="ip link set can1 type can bitrate 100000";
    String cmd13="ip link set can1 up";
    String cmd114="candump can1&";
    String cmd14="candump can1 > /data/can1.txt &";
    String cmd15="cansend can1 123#11111111";
    String cmd1RM = String.format("rm /data/can1.txt");

    String cmd2Touch = String.format("touch /data/can2.txt");
    String cmd21="ip link set can2 down";
    String cmd22="ip link set can2 type can bitrate 100000";
    String cmd23="ip link set can2 up";
    String cmd224="candump can2 &";
    String cmd24="candump can2 > /data/can2.txt &";
    String cmd25="cansend can2 123#11111111";
    String cmd2RM = String.format("rm /data/can2.txt");




    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_can_test);
        upgradeRootPermission("/data");
        String[] fileNames = {"can0.txt", "can1.txt", "can2.txt"};
       // FileManager.deleteAndCreateFiles(fileNames);

        Button startCanButton = findViewById(R.id.start_can_test_button);
        startCanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] canFiles = {"/sys/class/net/can0", "/sys/class/net/can1", "/sys/class/net/can2"};
                for (String canFile : canFiles) {
                    checkCanFile(canFile);
                }
                ((TextView) findViewById(R.id.canString)).setText(canTotal == 0 ? "检测CAN数为0,退出测试" : "当前CAN设备数量为： " + canTotal);
            }
        });
    }



     /*  public void Can0Test() {
           String testFilePath0 =  "/data/" ;
           UpgradeRootPermission(testFilePath0);
           File f0 = new File("/data/can0.txt");
           if (can0_flag){
               if (!f0.exists()) {
                   runCmd(cmd0Touch, 1);
               }
               runCmd(cmd01, 1);
               runCmd(cmd02, 1);
               runCmd(cmd03, 1);
               runCmd(cmd04, 1);
               runCmd(cmd05, 1);
               ((TextView) findViewById(R.id.can0Send)).setText("发送内容：" + "123#11111111");
           runOnUiThread(() -> {
               if (f0.length() > 0) {
                   can0_count++;
                   System.out.println("java.txt文件大小为: " + f0.length());
                   runCmd(cmd0RM, 1);
                   ((TextView) findViewById(R.id.can0Recive)).setText("接收内容：" + "123#11111111");
                   ((TextView) findViewById(R.id.can0String)).setText("测试次数" + can0_count);
                   final_flag[0] = 0;
                   canOK++;
               } else {
                   System.out.println("can0测试失败");
                   ((TextView) findViewById(R.id.can0Send)).setText("测试失败");
                   ((TextView) findViewById(R.id.can0Recive)).setText("测试失败");
                   can0_flag = false;
               }
           });
       }

       } */


 /*   public boolean Can1Test() {
        Process process = null;
        DataOutputStream os = null;

        try {
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());

                System.out.println("can1测试");
                CmdResult1 result1;
                os.writeBytes(cmd11 + "\n");
                os.writeBytes(cmd12 + "\n");
                os.writeBytes(cmd13 + "\n");
                os.writeBytes(cmd14 + "\n");
                os.writeBytes(cmd15 + "\n");
                os.writeBytes(cmd15 + "\n");
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();
            ((TextView) findViewById(R.id.can1Send)).setText("发送内容：" + "123#11111111");
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

            File f1= new File("/data/can1.txt");
            if (f1.exists() && f1.length()>0){
                System.out.println("java.txt文件大小为: " + f1.length());
                ((TextView) findViewById(R.id.can1String)).setText("CAN1测试成功" + f1.length());
                final_flag[1] = 0;
                canOK++;
            }
            else {
                ((TextView) findViewById(R.id.can1String)).setText("CAN1测试失败");
            }


        return true;
    }
    public boolean Can2Test() {
        Process process = null;
        DataOutputStream os = null;

        try {
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
                CmdResult1 result2;
                System.out.println("can2测试");
                os.writeBytes(cmd21 + "\n");
                os.writeBytes(cmd22 + "\n");
                os.writeBytes(cmd23 + "\n");
                os.writeBytes(cmd24 + "\n");
                os.writeBytes(cmd25 + "\n");
                os.writeBytes(cmd25 + "\n");
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();

              ((TextView) findViewById(R.id.can2Send)).setText("发送内容：" + "123#11111111");
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
            File f2= new File("/data/can2.txt");
            if (f2.exists() && f2.length()>0){
                System.out.println("java.txt文件大小为: " + f2.length());
                ((TextView) findViewById(R.id.can2String)).setText("CAN2测试成功" + f2.length());
                final_flag[2] = 0;
                canOK++;
            }
            else {
                ((TextView) findViewById(R.id.can2String)).setText("CAN2测试失败");
            }



        return true;
    } */




    @Override
    protected void onResume() {
        super.onResume();
    }



   /* public static class FileManager {

        public static void deleteAndCreateFiles(String[] fileNames) {
            for (String fileName : fileNames) {
                UpgradeRootPermission("/data/");
               // UpgradeRootPermission("/storage/");
                File file = new File("/data/", fileName);

                // 删除文件
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        System.out.println(fileName + " 文件已被删除");
                    } else {
                        System.out.println(fileName + " 文件删除失败");
                    }
                }

                // 创建文件
                try {
                    boolean created = file.createNewFile();
                    if (created) {
                        System.out.println(fileName + " 文件已被创建");
                    } else {
                        System.out.println(fileName + " 文件创建失败");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    } */


    public class CmdResult1 {
        CmdResult1(int exitVal, String output) {
            this.exitVal = exitVal;
            this.output = output;
        }
        public int exitVal;
        public String output;


    }

    public CmdResult1 runCmd(String cmd, int timeout) {
        java.lang.Process process = null;
        int exitVal = -1;
        DataOutputStream os = null;
        BufferedReader reader = null;
        StringBuilder output = new StringBuilder();

        try {
            process = Runtime.getRuntime().exec("su root sh");
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

        return new CmdResult1(exitVal, output.toString());
    }

    private void checkCanFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            canTotal++;
            System.out.println(filePath);
            if (filePath.endsWith("can0")) {
                can0_flag = true;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 在这里执行耗时的操作
                        String can0path = "/data/can0.txt";
                        File f0 = new File(can0path);
                        upgradeRootPermission("data");
                        while (can0_flag){
                            if (!f0.exists()) {
                                runCmd(cmd0Touch, 1);
                            }
                            upgradeRootPermission(can0path);
                            runCmd(cmd01, 1);
                            runCmd(cmd02, 1);
                            runCmd(cmd03, 1);
                            runCmd(cmd04, 1);
                            runCmd(cmd05, 1);
                            try {
                                Thread.sleep(2000);  // 延迟1秒
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            // 操作完成后，将结果传回主线程
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 在这里更新UI
                                    ((TextView) findViewById(R.id.can0Send)).setText("发送内容：" + "123#11111111");
                                    if (f0.length() > 0) {
                                        try {

                                            BufferedReader br = new BufferedReader(new FileReader(f0));
                                            StringBuilder sb = new StringBuilder();
                                            String line;
                                            while ((line = br.readLine()) != null) {
                                                sb.append(line);
                                                sb.append('\n');
                                            }
                                            br.close();

                                            // 获取到TextView的引用
                                            TextView textView = findViewById(R.id.can0Recive);
                                            // 将读取到的文件内容设置到TextView上
                                            textView.setText("接收内容：" + sb.toString());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        can0_count++;
                                        System.out.println("java.txt文件大小为: " + f0.length());
                                        runCmd(cmd0RM, 1);
                                       // ((TextView) findViewById(R.id.can0Recive)).setText("接收内容：" + "123#rpdzkj");
                                        ((TextView) findViewById(R.id.can0String)).setText("测试次数" + can0_count);
                                        final_flag[0] = 0;
                                        canOK++;
                                    } else {
                                        System.out.println("can0测试失败");
                                        ((TextView) findViewById(R.id.can0Send)).setText("测试失败");
                                        ((TextView) findViewById(R.id.can0Recive)).setText("测试失败");
                                        can0_flag = false;
                                    }
                                }
                            });
                        }
                    }
                }).start();


            }
            else if (filePath.endsWith("can1")) {
                can1_flag = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 在这里执行耗时的操作
                        upgradeRootPermission("data");
                        String can1Path = "/data/can1.txt";
                        File f1 = new File(can1Path);

                        while (can1_flag){
                            if (!f1.exists()) {
                                runCmd(cmd1Touch, 1);
                            }
                            upgradeRootPermission(can1Path);
                            runCmd(cmd11, 1);
                            runCmd(cmd12, 1);
                            runCmd(cmd13, 1);
                            runCmd(cmd14, 1);
                            runCmd(cmd15, 1);


                            try {
                                Thread.sleep(2000);  // 延迟1秒
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // 操作完成后，将结果传回主线程
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 在这里更新UI
                                    ((TextView) findViewById(R.id.can1Send)).setText("发送内容：" + "123#11111111");
                                    if (f1.length() > 0) {
                                        try {
                                            BufferedReader br = new BufferedReader(new FileReader(f1));
                                            StringBuilder sb = new StringBuilder();
                                            String line;
                                            while ((line = br.readLine()) != null) {
                                                sb.append(line);
                                                sb.append('\n');
                                            }
                                            br.close();
                                            // 获取到TextView的引用
                                            TextView textView = findViewById(R.id.can1Recive);
                                            // 将读取到的文件内容设置到TextView上
                                            textView.setText("接收内容：" + sb.toString());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        can1_count++;
                                        System.out.println("java.txt文件大小为: " + f1.length());
                                        runCmd(cmd1RM, 1);
                                       // ((TextView) findViewById(R.id.can1Recive)).setText("接收内容：" + "123#rpdzkj");
                                        ((TextView) findViewById(R.id.can1String)).setText("测试次数" + can1_count);
                                        final_flag[1] = 0;
                                        canOK++;
                                    } else {
                                        System.out.println("can1测试失败");
                                        ((TextView) findViewById(R.id.can1Send)).setText("测试失败");
                                        ((TextView) findViewById(R.id.can1Recive)).setText("测试失败");
                                        can1_flag = false;
                                    }
                                }
                            });
                        }
                    }
                }).start();
            }
            else if (filePath.endsWith("can2")) {
                can2_flag = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 在这里执行耗时的操作
                        upgradeRootPermission("/data");
                        String can2Path = "/data/can2.txt";
                        File f2 = new File(can2Path);

                        while (can2_flag){
                            if (!f2.exists()) {
                                runCmd(cmd2Touch, 1);
                            }
                            runCmd(cmd21, 1);
                            runCmd(cmd22, 1);
                            runCmd(cmd23, 1);
                            runCmd(cmd24, 1);
                            runCmd(cmd25, 1);


                            try {
                                Thread.sleep(2000);  // 延迟1秒
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // 操作完成后，将结果传回主线程
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 在这里更新UI
                                    ((TextView) findViewById(R.id.can1Send)).setText("发送内容：" + "123#11111111");
                                    if (f2.length() > 0) {
                                        try {

                                            BufferedReader br = new BufferedReader(new FileReader(f2));
                                            StringBuilder sb = new StringBuilder();
                                            String line;
                                            while ((line = br.readLine()) != null) {
                                                sb.append(line);
                                                sb.append('\n');
                                            }
                                            br.close();

                                            // 获取到TextView的引用
                                            TextView textView = findViewById(R.id.can1Recive);
                                            // 将读取到的文件内容设置到TextView上
                                            textView.setText("接收内容：" + sb.toString());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        can1_count++;
                                        System.out.println("java.txt文件大小为: " + f2.length());
                                        runCmd(cmd2RM, 1);
                                        // ((TextView) findViewById(R.id.can1Recive)).setText("接收内容：" + "123#rpdzkj");
                                        ((TextView) findViewById(R.id.can1String)).setText("测试次数" + can2_count);
                                        final_flag[2] = 0;
                                        canOK++;
                                    } else {
                                        System.out.println("can2测试失败");
                                        ((TextView) findViewById(R.id.can2Send)).setText("测试失败");
                                        ((TextView) findViewById(R.id.can2Recive)).setText("测试失败");
                                        can2_flag = false;
                                    }
                                }
                            });
                        }
                    }
                }).start();
            }
        }
    }

    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su root"); //切换到root帐号
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
