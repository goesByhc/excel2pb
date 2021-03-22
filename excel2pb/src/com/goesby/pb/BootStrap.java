package com.goesby.pb;

/**
 * @Author goesby
 * @create 2020/7/10 17:04
 */
public class BootStrap {
    public static void main(String[] args) throws Exception {
        String isMakeProtoFile="data";
        String excelPath = "D:\\xiuzhendashi\\client\\ascentmaster-config\\Design";
        String outPath = "./";
        if (args.length > 0){
            isMakeProtoFile = args[0];
        }
        if (args.length > 1){
            excelPath = args[1];
        }
        if(args.length > 2){
            outPath = args[2];
        }
        if(!isMakeProtoFile.equals("file")){
            MakeProtoData.main(new String[]{excelPath,outPath});
            String cmd="protoc ./../Proto/Config*.proto --proto_path=./../Proto --java_out=./javac/";
        }else{
            MakeProtoFile.main(new String[]{excelPath,outPath});
        }

    }
}
