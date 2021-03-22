package com.goesby.pb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @Author goesby
 * @create 2020/7/10 18:09
 */
public class SysCmdUtil {

    public static void exec(String cmd) throws IOException {
        Process process = Runtime.getRuntime().exec(cmd);
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String content = br.readLine();
        while (content != null) {
            System.out.println(content);
            content = br.readLine();
        }
    }
}
