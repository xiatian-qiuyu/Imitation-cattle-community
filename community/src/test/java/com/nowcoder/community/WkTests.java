package com.nowcoder.community;

import java.io.IOException;

public class WkTests {

    public static void main(String[] args) {
        String cmd = "d:/work/wkhtmltopdf/bin/wkhtmltoimage --quality 75  https://www.nowcoder.com d:/work/data/wk-images/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            //把命令提交给本地操作系统，剩余的事情就交给操作系统了，java程序不会等待命令执行完毕
            //因为操作系统和这个主程序是并发的，异步的，所以这个程序会继续往下执行，会先打印ok，然后才生成图片。
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
