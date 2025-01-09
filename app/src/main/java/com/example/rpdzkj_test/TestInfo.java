package com.example.rpdzkj_test;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import android.util.Log;
import java.io.File;

public class TestInfo {
    private Context context;
    private File savedFile;

    public TestInfo(Context context) {
        this.context = context;
    }

    public void setSavedFile(File file) {
        this.savedFile = file;
    }

   /* public void saveIdsAsHtml(String board, String cpuId, String pcbId, String snId, String testTime) {
        // 生成HTML内容
        String content = generateHtmlContent(board, cpuId, pcbId, snId, testTime);

        // 获取目标文件路径
        File directory = context.getFilesDir();
        File file = new File(directory, "saved_ids.html");

        // 如果文件存在，则删除旧文件
        if (file.exists()) {
            if (file.delete()) {
                Log.d("FileOperation", "旧文件已成功删除: " + file.getAbsolutePath());
            } else {
                Log.e("FileOperation", "删除旧文件失败: " + file.getAbsolutePath());
            }
        }

        // 保存新文件
        saveToFile(content, directory, "saved_ids.html");
    } */

    public void saveIdsAsHtml(String board, String cpuId, String pcbId, String snId, String testTime) {
        // 生成HTML内容
        String content = generateHtmlContent(board, cpuId, pcbId, snId, testTime);

        // 获取目标文件路径
        File directory = context.getFilesDir();

        // 删除当前路径下所有的.html文件
        deleteHtmlFiles(directory);

        // 生成新文件名
        String fileName = generateFileName(pcbId, snId);
        File file = new File(directory, fileName);

        // 保存新文件
        saveToFile(content, directory, fileName);
    }

    private void deleteHtmlFiles(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".html"));
        if (files != null) {
            for (File file : files) {
                if (file.delete()) {
                    Log.d("FileOperation", "已成功删除文件: " + file.getAbsolutePath());
                } else {
                    Log.e("FileOperation", "删除文件失败: " + file.getAbsolutePath());
                }
            }
        }
    }

    public String generateFileName(String pcbId, String snId) {
        String pcbIdPart = (pcbId != null && !pcbId.isEmpty()) ? pcbId.substring(0, 6) : "pcb_id_null";
        return pcbIdPart + "_" + snId.substring(0, 6) + "_rpdzkj.html";
    }

  /* public void saveIdsAsHtml(String board, String cpuId, String pcbId, String snId, String testTime) {
       // 生成HTML内容
       String content = generateHtmlContent(board, cpuId, pcbId, snId, testTime);

       // 获取目标文件路径
       File directory = context.getFilesDir();
       String pcbIdPart = (pcbId != null && !pcbId.isEmpty()) ? pcbId.substring(0, 6) : "pcb_id_null";
       String fileName = pcbIdPart + "_" + snId.substring(0, 6) + ".html";
       File file = new File(directory, fileName);

       // 如果文件存在，则删除旧文件
       if (file.exists()) {
           if (file.delete()) {
               Log.d("FileOperation", "旧文件已成功删除: " + file.getAbsolutePath());
           } else {
               Log.e("FileOperation", "删除旧文件失败: " + file.getAbsolutePath());
           }
       }

       // 保存新文件
       saveToFile(content, directory, fileName);
   } */



    public void viewSavedFile() {
        if (savedFile != null && savedFile.exists()) {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(savedFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "读取失败", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(context, content.toString(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "没有可查看的文件", Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadFileToDownload() {
        if (savedFile != null && savedFile.exists()) {
            File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File destinationFile = new File(downloadDirectory, savedFile.getName());
            File udiskDirectory = new File("/mnt/udisk");
            File udiskDestinationFile = new File(udiskDirectory, savedFile.getName());
            File sdDirectory = new File("/mnt/ex_sd");
            File sdDestinationFile = new File(sdDirectory, savedFile.getName());

            try (InputStream in = new FileInputStream(savedFile);
                 OutputStream outDownload = new FileOutputStream(destinationFile);
                 OutputStream outUdisk = new FileOutputStream(udiskDestinationFile) ;
                 OutputStream outSd = new FileOutputStream(sdDestinationFile)){

                // 复制文件内容到下载目录
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    outDownload.write(buffer, 0, length);
                    outUdisk.write(buffer, 0, length); // 同时复制到/mnt/udisk
                    outSd.write(buffer, 0, length);// 同时复制到/mnt/ex_sd
                }

                Toast.makeText(context, "文件已复制到 " + destinationFile.getAbsolutePath() + " 、 " + udiskDestinationFile.getAbsolutePath() + " 和 " + sdDestinationFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "文件复制失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "没有可复制的文件", Toast.LENGTH_SHORT).show();
        }
    }


    public void appendAdditionalContent(String uartInfo, int[] testCounts, String selectedTtys) {
        if (savedFile != null && savedFile.exists()) {
            try {
                System.out.println("文件存在: " + savedFile.getAbsolutePath());

                // 读取已有文件内容
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(savedFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                System.out.println("已有文件内容:\n" + content);
                // 添加选中的 ttyS 注释内容

                // 提取 ttyS 内容
                String[] uartLines = uartInfo.split("\\s+");
                StringBuilder extractedContent = new StringBuilder();
                extractedContent.append("<p><font size=\"+1\"><strong>Uart Test:</strong></font></p>\n");
                extractedContent.append("<p>当前选中的 ttyS: ").append(selectedTtys).append("</p>\n");
                // 构建表格内容
                extractedContent.append("<table border='1'><tr><th>UART</th><th>测试次数</th></tr>");
                for (int i = 0; i < uartLines.length; i++) {
                    String line = uartLines[i];
                    if (line.startsWith("/dev/ttyS")) {
                        extractedContent.append("<tr><td>").append(line.replace("/dev/", "")).append("</td>")
                                .append("<td>").append(testCounts[i]).append("</td></tr>");
                    }
                }
                extractedContent.append("</table>");
                System.out.println("提取后的内容:\n" + extractedContent);

                // 在 </body> 标签前插入新内容
                int bodyEndIndex = content.lastIndexOf("</body>");
                if (bodyEndIndex != -1) {
                    content.insert(bodyEndIndex, extractedContent.toString());
                    System.out.println("更新后的文件内容:\n" + content);
                }

                // 将更新后的内容写回文件
                try (FileWriter writer = new FileWriter(savedFile)) {
                    writer.write(content.toString());
                }

                Toast.makeText(context, "内容已追加到 HTML 文件", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "追加内容失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "没有可追加的文件", Toast.LENGTH_SHORT).show();
        }
    }

    public void appendAdditionalWifiIcContent(String wifiIc1) {
        if (savedFile != null && savedFile.exists()) {
            try {
                System.out.println("文件存在: " + savedFile.getAbsolutePath());

                // 读取已有文件内容
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(savedFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                System.out.println("已有文件内容:\n" + content);

                // 构建新的内容
                StringBuilder newContent = new StringBuilder();
                newContent.append("<p><font size=\"+1\"><strong>WIFI Connect Test:</strong></font></p>\n");
                newContent.append("<p>WiFi IC 1: ").append(wifiIc1).append("</p>\n");
              //  newContent.append("<p>WiFi IC 2: ").append(wifiIc2).append("</p>\n");

                // 在 </body> 标签前插入新内容
                int bodyEndIndex = content.lastIndexOf("</body>");
                if (bodyEndIndex != -1) {
                    content.insert(bodyEndIndex, newContent.toString());
                    System.out.println("更新后的文件内容:\n" + content);
                }

                // 将更新后的内容写回文件
                try (FileWriter writer = new FileWriter(savedFile)) {
                    writer.write(content.toString());
                }

                Toast.makeText(context, "WiFi IC 内容已追加到 HTML 文件", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "追加 WiFi IC 内容失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "没有可追加的文件", Toast.LENGTH_SHORT).show();
        }
    }

    public void appendAdditionalStorageContent(String storage1, String storage2, String storage3, String storage4, String storage5, String storage6, String storage7, String storage8) {
        if (savedFile != null && savedFile.exists()) {
            try {
                System.out.println("文件存在: " + savedFile.getAbsolutePath());

                // 读取已有文件内容
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(savedFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                System.out.println("已有文件内容:\n" + content);

                // 构建新的内容
                StringBuilder newContent = new StringBuilder();
                newContent.append("<p><font size=\"+1\"><strong>Storage Test:</strong></font></p>\n");
                newContent.append("<table border='1'><tr><th>Status</th><th>Times</th></tr>");
                newContent.append("<tr><td>").append(storage1).append("</td><td>").append(storage5).append("</td></tr>");
                newContent.append("<tr><td>").append(storage2).append("</td><td>").append(storage6).append("</td></tr>");
                newContent.append("<tr><td>").append(storage3).append("</td><td>").append(storage7).append("</td></tr>");
                newContent.append("<tr><td>").append(storage4).append("</td><td>").append(storage8).append("</td></tr>");
                newContent.append("</table>");

                // 在 </body> 标签前插入新内容
                int bodyEndIndex = content.lastIndexOf("</body>");
                if (bodyEndIndex != -1) {
                    content.insert(bodyEndIndex, newContent.toString());
                    System.out.println("更新后的文件内容:\n" + content);
                }

                // 将更新后的内容写回文件
                try (FileWriter writer = new FileWriter(savedFile)) {
                    writer.write(content.toString());
                }

                Toast.makeText(context, "Storage 内容已追加到 HTML 文件", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "追加 Storage 内容失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "没有可追加的文件", Toast.LENGTH_SHORT).show();
        }
    }


    private String generateHtmlContent(String board, String cpuId, String pcbId, String snId, String testTime) {
        return "<html><body>"
                + "<h1>硬件信息</h1>"
                + "<p><strong>版型信息:</strong> " + board + "</p>"
                + "<p><strong>CPU_ID:</strong> " + cpuId + "</p>"
                + "<p><strong>PCB_ID:</strong> " + pcbId + "</p>"
                + "<p><strong>SN_ID:</strong> " + snId + "</p>"
                + "<p><strong>单项测试时长:</strong> " + testTime + "</p>"
                + "</body></html>";
    }

    public void appendAdditionalWifiBtSwContent(String WifiSwitch, String BtSwitch) {
        if (savedFile != null && savedFile.exists()) {
            try {
                System.out.println("文件存在: " + savedFile.getAbsolutePath());

                // 读取已有文件内容
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(savedFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                System.out.println("已有文件内容:\n" + content);

                // 构建新的内容
                StringBuilder newContent = new StringBuilder();
                newContent.append("<p><font size=\"+1\"><strong>WIFI BT Switch Test:</strong></font></p>\n");
                newContent.append(WifiSwitch).append("</p>\n");
                newContent.append(BtSwitch).append("</p>\n");
                //  newContent.append("<p>WiFi IC 2: ").append(wifiIc2).append("</p>\n");

                // 在 </body> 标签前插入新内容
                int bodyEndIndex = content.lastIndexOf("</body>");
                if (bodyEndIndex != -1) {
                    content.insert(bodyEndIndex, newContent.toString());
                    System.out.println("更新后的文件内容:\n" + content);
                }

                // 将更新后的内容写回文件
                try (FileWriter writer = new FileWriter(savedFile)) {
                    writer.write(content.toString());
                }

                Toast.makeText(context, "内容已追加到 HTML 文件", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "追加内容失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "没有可追加的文件", Toast.LENGTH_SHORT).show();
        }
    }

    public void appendAdditionalNetWorkIcContent(String eth0Status, String eth1Status, String eth2Status) {
        if (savedFile != null && savedFile.exists()) {
            try {
                System.out.println("文件存在: " + savedFile.getAbsolutePath());

                // 读取已有文件内容
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(savedFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                System.out.println("已有文件内容:\n" + content);

                // 构建新的内容
                StringBuilder newContent = new StringBuilder();
                newContent.append("<p><font size=\"+1\"><strong>NetWork Connect Test:</strong></font></p>\n");
                newContent.append("ETH0: ").append(eth0Status).append("</p>\n");
                newContent.append("ETH1: ").append(eth1Status).append("</p>\n");
                newContent.append("ETH2: ").append(eth2Status).append("</p>\n");
                //  newContent.append("<p>WiFi IC 2: ").append(wifiIc2).append("</p>\n");

                // 在 </body> 标签前插入新内容
                int bodyEndIndex = content.lastIndexOf("</body>");
                if (bodyEndIndex != -1) {
                    content.insert(bodyEndIndex, newContent.toString());
                    System.out.println("更新后的文件内容:\n" + content);
                }

                // 将更新后的内容写回文件
                try (FileWriter writer = new FileWriter(savedFile)) {
                    writer.write(content.toString());
                }

                Toast.makeText(context, "内容已追加到 HTML 文件", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "追加内容失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "没有可追加的文件", Toast.LENGTH_SHORT).show();
        }
    }

    public void appendAdditionalSpiTestContent(String SpiCounts, String Spi0, String spi0Times,  String Spi1, String spi1Times, String Spi2, String spi2Times) {
        if (savedFile != null && savedFile.exists()) {
            try {
                System.out.println("文件存在: " + savedFile.getAbsolutePath());

                // 读取已有文件内容
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(savedFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                System.out.println("已有文件内容:\n" + content);

                // 构建新的内容
                StringBuilder newContent = new StringBuilder();
                newContent.append("<p><font size=\"+1\"><strong>Spi Test:</strong></font></p>\n");
                newContent.append(SpiCounts).append("</p>\n");
                newContent.append("SPI0: ").append(Spi0).append(" ").append(spi0Times).append("</p>\n");
                newContent.append("SPI1: ").append(Spi1).append(" ").append(spi1Times).append("</p>\n");
                newContent.append("SPI2: ").append(Spi2).append(" ").append(spi2Times).append("</p>\n");
                //  newContent.append("<p>WiFi IC 2: ").append(wifiIc2).append("</p>\n");

                // 在 </body> 标签前插入新内容
                int bodyEndIndex = content.lastIndexOf("</body>");
                if (bodyEndIndex != -1) {
                    content.insert(bodyEndIndex, newContent.toString());
                    System.out.println("更新后的文件内容:\n" + content);
                }

                // 将更新后的内容写回文件
                try (FileWriter writer = new FileWriter(savedFile)) {
                    writer.write(content.toString());
                }

                Toast.makeText(context, "内容已追加到 HTML 文件", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "追加内容失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "没有可追加的文件", Toast.LENGTH_SHORT).show();
        }
    }

    public void appendAdditionalCanTestContent(String CanCounts, String Can0, String Can0Times, String Can1, String Can1Times, String Can2, String Can2Times) {
        if (savedFile != null && savedFile.exists()) {
            try {
                System.out.println("文件存在: " + savedFile.getAbsolutePath());

                // 读取已有文件内容
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(savedFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                System.out.println("已有文件内容:\n" + content);

                // 构建新的内容
                StringBuilder newContent = new StringBuilder();
                newContent.append("<p><font size=\"+1\"><strong>Can Test:</strong></font></p>\n");
                newContent.append(CanCounts).append("</p>\n");
                newContent.append("CAN0: ").append(Can0).append(" ").append(Can0Times).append("</p>\n");
                newContent.append("CAN1: ").append(Can1).append(" ").append(Can1Times).append("</p>\n");
                newContent.append("CAN2: ").append(Can2).append(" ").append(Can2Times).append("</p>\n");
                //  newContent.append("<p>WiFi IC 2: ").append(wifiIc2).append("</p>\n");

                // 在 </body> 标签前插入新内容
                int bodyEndIndex = content.lastIndexOf("</body>");
                if (bodyEndIndex != -1) {
                    content.insert(bodyEndIndex, newContent.toString());
                    System.out.println("更新后的文件内容:\n" + content);
                }

                // 将更新后的内容写回文件
                try (FileWriter writer = new FileWriter(savedFile)) {
                    writer.write(content.toString());
                }

                Toast.makeText(context, "内容已追加到 HTML 文件", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "追加内容失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "没有可追加的文件", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToFile(String content, File directory, String fileName) {
        try {
            savedFile = new File(directory, fileName);
            try (FileWriter writer = new FileWriter(savedFile)) {
                writer.write(content);
            }
            Toast.makeText(context, "文件已保存到 " + savedFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    public String getCpuId() {
        StringBuilder cpuId = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/cpuid");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                cpuId.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "无法获取 CPU ID", Toast.LENGTH_SHORT).show();
        }
        return cpuId.toString();
    }

    public String getBoard() {
        StringBuilder boardInfo = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/device-tree/model");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                boardInfo.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "无法获取版型信息", Toast.LENGTH_SHORT).show();
        }
        return boardInfo.toString();
    }

    public File findFileBySuffix(String suffix) {
        File directory = context.getFilesDir();
        File[] files = directory.listFiles((dir, name) -> name.endsWith(suffix));

        if (files != null && files.length > 0) {
            return files[0]; // 返回第一个匹配的文件
        } else {
            return null; // 没有找到匹配的文件
        }
    }

}
