package com.goesby.pb;

import java.io.*;

/**
 * @Author goesby
 * @create 2020/7/7 15:59
 */
public class MakeProtoFile {
    public static void main(String[] args) throws Exception {
        String excelPath = "D:\\xiuzhendashi\\client\\ascentmaster-config\\Design";
        String outPath = "./";
        if (args.length > 0){
            excelPath = args[0];
        }
        if(args.length > 1){
            outPath = args[1];
        }
        File dir=new File(excelPath);
        if(!dir.isDirectory()){
            throw  new Exception(excelPath +" not Directory");
        }
        for (File file:dir.listFiles()){
            if(file.isDirectory()){
                continue;
            }
            if(!file.getName().contains(".xlsx")){
                continue;
            }
            if(file.getName().startsWith(".~")){
                continue;
            }
            TableInfo tableInfo=TableInfoFactory.createTable(file);
            if(tableInfo.isEmpty()){
                System.out.println(tableInfo.getTableName()+" isEmpty");
                continue;
            }
            System.out.println("start "+file.getName());
            String protoInfo[]=tableInfo.createProtoBufferFile();
            try {
                IoUtil.wirteFile(outPath,protoInfo[0]+".proto",protoInfo[1].getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            System.out.println("end "+file.getName());
        }
    }
}
