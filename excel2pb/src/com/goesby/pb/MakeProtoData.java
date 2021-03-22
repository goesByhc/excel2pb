package com.goesby.pb;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.math.BigDecimal;

/**
 * @Author goesby
 * @create 2020/7/9 15:51
 */
public class MakeProtoData {
//D:\work\pbtest>protoc Config*.proto --proto_path=./ --java_out=./src/main/java/
    public static byte[] genProtoData(File file) throws Exception {
        FileInputStream excelFIS = new FileInputStream(file);
        Workbook excelBook=null;
        try{
            excelBook = WorkbookFactory.create(excelFIS);
        }catch (Exception e){
            excelBook = new HSSFWorkbook(excelFIS);
        }
        Sheet sheet= excelBook.getSheetAt(0);
        String tableName=file.getName();
        tableName=tableName.split("[.]")[0];
        if(tableName.startsWith("#")){
            return null;
        }
        int rowNum=sheet.getLastRowNum();
        if(rowNum<2){
            return null;
        }
        TableInfo info=TableInfoFactory.createTable(file);
        if(info.isEmpty()){
          return null;
        }
        if(isConstants(tableName)){
            return genConsProtoData(info,sheet);
        }
        //整体数据build
        String listClassName=info.getListClassName();
        Class listClass=info.getClass(info.getInnerClassName(listClassName));
        Message.Builder listBuild=info.createBuilder(listClass);
        Descriptors.FieldDescriptor listFiled = listBuild.getDescriptorForType().findFieldByName("list");
        //数据行build
        String rowClassName=info.getMainClassName(false,false);
        Class rowClass=info.getClass(info.getInnerClassName(rowClassName));

        String[] names=getColumnStringValue(sheet.getRow(info.getColumnNameIndex()));
        for(int i=info.getDataRowIndex();i<rowNum;i++){
            Row row=sheet.getRow(i);
            String firstCell = getCellContent(row.getCell(0));
            if(firstCell.equals("END")){
               break;
            }
            Message.Builder dataRowBuild=info.createBuilder(rowClass);
            info.readDataRow(names,dataRowBuild,row);
            listBuild.addRepeatedField(listFiled,dataRowBuild.build());
        }

        Message build = listBuild.build();
        System.out.println(file.getName() + "--------------------");
        System.out.println(build.toString());

        return build.toByteArray();
    }

    /**
     * 常量表处理
     * @param info
     * @param sheet
     * @return
     * @throws Exception
     */
    public static byte[] genConsProtoData(TableInfo info,Sheet sheet) throws Exception {
        //数据行build

        String listClassName=info.getListClassName();

        String rowClassName=info.getMainClassName(false,false);
        Class rowClass=info.getClass(info.getInnerClassName(rowClassName));
        Message.Builder dataRowBuild=info.createBuilder(rowClass);
        int rowNum=sheet.getLastRowNum();
        for(int i=0;i<rowNum;i++){
            Row row=sheet.getRow(i);
            String name = getCellContent(row.getCell(0));
            if(name.equals("END")){
                break;
            }
            String value = getCellContent(row.getCell(1));
            setField(dataRowBuild,name,value);
        }
        return dataRowBuild.build().toByteArray();
    }

    public static void setField(Message.Builder builder,String key,String subValue) {
        Descriptors.FieldDescriptor field = builder.getDescriptorForType().findFieldByName(key);
        if (field == null) System.err.println("cant not find field by " + key);
        if (field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.STRING)) {
            builder.setField(field, subValue);
        } else {
            if(subValue.equals("")){
                subValue = "0";
            }
            if (subValue.endsWith(".0")) {
                subValue = subValue.substring(0, subValue.length() - 2);
            }
            if (field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.INT)) {
                try {
                    builder.setField(field, new BigDecimal(subValue).intValueExact());
                } catch (NumberFormatException e) {
                    System.out.printf("key : %s, value : %s\n", key, subValue);
                    System.err.printf("key : %s, value : %s\n", key, subValue);
                    e.printStackTrace();
                    System.exit(1);
                }
            } else if (field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.LONG)) {
                try {
                    builder.setField(field, new BigDecimal(subValue).longValueExact());
                } catch (NumberFormatException e) {
                    System.err.printf("key : %s, value : %s\n", key, subValue);
                    e.printStackTrace();
                    System.exit(1);
                }
            } else if (field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.DOUBLE)) {
                builder.setField(field, Double.valueOf(subValue));
            } else if(field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.BOOLEAN)) {
                builder.setField(field, Boolean.valueOf(subValue));
            } else if(field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.FLOAT)) {
                builder.setField(field, Float.valueOf(subValue));
            } else
            {
                System.err.println("type not support for: " + field.getJavaType());
            }
        }
    }

    public static void addRepeatedField(Message.Builder builder,String key,String subValue) {
        Descriptors.FieldDescriptor field = builder.getDescriptorForType().findFieldByName(key);
        if (field == null) System.err.println("cant not find field by " + key);
        if (field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.STRING)) {
            builder.addRepeatedField(field, subValue);
        } else {
            if(subValue.equals("")){
                subValue = "0";
            }
            if (subValue.endsWith(".0")) {
                subValue = subValue.substring(0, subValue.length() - 2);
            }
            if (field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.INT)) {
                try {
                    builder.addRepeatedField(field, new BigDecimal(subValue).intValueExact());
                } catch (NumberFormatException e) {
                    System.out.printf("key : %s, value : %s\n", key, subValue);
                    System.err.printf("key : %s, value : %s\n", key, subValue);
                    e.printStackTrace();
                    System.exit(1);
                }
            } else if (field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.LONG)) {
                try {
                    builder.addRepeatedField(field, new BigDecimal(subValue).longValueExact());
                } catch (NumberFormatException e) {
                    System.err.printf("key : %s, value : %s\n", key, subValue);
                    e.printStackTrace();
                    System.exit(1);
                }
            } else if (field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.DOUBLE)) {
                builder.addRepeatedField(field, Double.valueOf(subValue));
            } else if(field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.BOOLEAN)) {
                builder.addRepeatedField(field, Boolean.valueOf(subValue));
            } else if(field.getJavaType().equals(Descriptors.FieldDescriptor.JavaType.FLOAT)) {
                builder.addRepeatedField(field, Float.valueOf(subValue));
            } else
            {
                System.err.println("type not support for: " + field.getJavaType());
            }
        }
    }
    public static String getCellContent(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue() ? "true" : "false";
            case Cell.CELL_TYPE_FORMULA:
                return "";
            case Cell.CELL_TYPE_NUMERIC:
                return "" + cell.getNumericCellValue();
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default:
                return "";
        }
    }
    public static String[] getColumnStringValue(Row types){
        int columnNum=types.getLastCellNum();
        String column[]=new String[columnNum];
        for (int i=0;i<columnNum;i++){
            column[i]=types.getCell(i).getStringCellValue();
        }
        return column;
    }

    public static Class getClass(String name){
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isConstants(String value){
        return value.equals(TableInfo.Constants);
    }

    public static boolean isEnums(String value){
        return value.equals(TableInfo.Enums);
    }

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
            if(file.getName().startsWith("enums")) {
                continue;
            }
            System.out.println("start "+file.getName());
            byte[] datas=genProtoData(file);
            try {
                String dataFileName=file.getName().split("[.]")[0] + ".bin";
                IoUtil.wirteFile(outPath,dataFileName,datas);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            System.out.println("end "+file.getName());
        }
    }
}
