package com.goesby.pb;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author goesby
 * @create 2020/7/10 11:53
 */
public class TableInfoFactory {
    public static TableInfo createTable(File file) throws Exception {
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
        TableInfo tableInfo=new TableInfo(tableName);
        if(tableName.startsWith("#")||sheet.getLastRowNum()<2){
            return null;
        }
        if(tableInfo.isConstants()){
            return createConstantsTable(tableInfo,sheet);
        }
        if(tableInfo.isEnums()) {
            return createEnumsTable(tableInfo,sheet);
        }

        System.out.println("create table "+tableName);
        Row types=sheet.getRow(0);
        Row names=sheet.getRow(1);
        String notes=types.getCell(0).getStringCellValue();
        if(notes.startsWith("#")){
            tableInfo.setColumnTypeIndex(1);
            tableInfo.setColumnNameIndex(2);
            tableInfo.setDataRowIndex(3);
            types=sheet.getRow(1);
            names=sheet.getRow(2);
        }else{
            tableInfo.setColumnTypeIndex(0);
            tableInfo.setColumnNameIndex(1);
            tableInfo.setDataRowIndex(2);
        }

        System.out.println("LastCellNum:"+ types.getLastCellNum());
        for (int i = 0; i < types.getLastCellNum(); i++) {
            String type=types.getCell(i).getStringCellValue();
            if (type.equals("END")) {
                break;
            }
            String name=names.getCell(i).getStringCellValue();
            if (name.equals("END")) {
                break;
            }
            if(type==null||type.length()==0){
                break;
            }
            if(name.startsWith("#")){
                continue;
            }
            String groups[]=name.split("-");
            if(groups.length==1) {
                ColumnInfo column=new ColumnInfo(type,name);
                tableInfo.addColumn(column);
                continue;
            }
            if(groups.length==2){
                String groupName=groups[0];
                ColumnInfo column=new ColumnInfo(type,groupName,true);
                tableInfo.addBaseTypeGroupColumn(column);
                continue;
            }
            String groupName=groups[0];
            String columnName=groups[2];
            ColumnInfo column=new ColumnInfo(type,columnName);
            tableInfo.addGroupColumn(groupName,column);
        }
        return tableInfo;
    }

    private static TableInfo createConstantsTable(TableInfo tableInfo,Sheet sheet) {
        int row=sheet.getLastRowNum();
        if(row==0){
            return tableInfo;
        }
        for(int i=0;i<row;i++){
            String name=sheet.getRow(i).getCell(0).getStringCellValue();
            Cell cell=sheet.getRow(i).getCell(2);
            String type="int";
            if(cell!=null){
                type=cell.getStringCellValue();
            }
            type= type==null?"int":type;
            ColumnInfo column=new ColumnInfo(type,name);
            if (column.getColumnName().equals("END")) {
                break;
            }
            tableInfo.addColumn(column);
        }
        return tableInfo;
    }

    private static TableInfo createEnumsTable(TableInfo tableInfo,Sheet sheet) {
        String enumName = "";
        Map<String, String> enums = null;

        int i = -1;
        while (true) {
            i++;
            Row row = sheet.getRow(i);
            String key = row.getCell(0).getStringCellValue();

            if (key.equals("enum")) {
                enumName = row.getCell(1).getStringCellValue();
                enums = new LinkedHashMap<>();
                System.out.println("enum Name:" + enumName);
                continue;
            }

            if (key.length() == 0 || key.equals("END")) {
                tableInfo.addEnums(enumName, enums);
                System.out.println("table info add Name:" + enumName);
                enumName = "";
                if (key.equals("END")) {
                    break;
                }
                continue;
            }

            System.out.println("key:" + key + " v:" + ((int)row.getCell(1).getNumericCellValue()));
            enums.put(key, ((int)row.getCell(1).getNumericCellValue()) + "");

        }
        return tableInfo;
    }


    public static void main(String[] args) throws Exception {
        String path="D:\\xiuzhendashi\\client\\ascentmaster-config\\Design\\language.xlsx";
        File file=new File(path);
        TableInfoFactory.createTable(file);
    }
}
