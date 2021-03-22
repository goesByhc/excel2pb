package com.goesby.pb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Author goesby
 * @create 2020/7/10 16:22
 */
public class IoUtil {

    public static void wirteFile(String path,String fileName,byte[] data) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append(path).append(File.separator).append(fileName);
        File file=new File(sb.toString());
        if(file.exists()){
            file.delete();
        }
        file.createNewFile();
        FileOutputStream outputStream=new FileOutputStream(file);
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();
    }
}
