package com.goesby.pb;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.apache.poi.ss.usermodel.Row;

import java.lang.reflect.Method;
import java.util.*;

/**
 *  表信息
 *  auth:goesby
 */
public class TableInfo {
    /**
     * 常量表文件
     */
    public final static String Constants="constants";
    /**
     * 枚举表文件
     */
    public final static String Enums="enums";
    /**
     * 导出的包
     */
    public final static String outPKName="com.inke.conf";
    /**
     * 导出的包
     */
    public final static String goPackage=".;game";
    /**
     * 前缀
     */
    public final static String prefix="Config";
    /**
     * 表名
     */
    private String tableName;
    /**
     * 数据开始的行索引
     */
    private int dataRowIndex;
    /**
     * 列名行索引
     */
    private int columnNameIndex;
    /**
     * 列类型索引
     */
    private int columnTypeIndex;
    /**
     * 列信息 包含分组的列
     */
    private List<ColumnInfo> columnInfos=new ArrayList<>();
    /**
     * 列分组信息
     */
    private Map<String, Map<String,ColumnInfo>> groupColumns=new HashMap<>();
    /**
     * 基础类型分组
     */
    private Map<String, ColumnInfo> baseTypeGroupColumns=new HashMap<>();

    private Map<String, Map<String, String>> columnEnums =new LinkedHashMap<>();

    public TableInfo(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void addColumn(ColumnInfo columnInfo){
        columnInfos.add(columnInfo);
    }

    public void addEnums(String enumName, Map<String, String> enums) {
        columnEnums.put(enumName, enums);
    }

    public void addGroupColumn(String group,ColumnInfo columnInfo){
        Map<String,ColumnInfo> map=groupColumns.get(group);
        if(map==null){
            map=new LinkedHashMap<>();
            groupColumns.put(group,map);
        }
        map.put(columnInfo.getColumnName(),columnInfo);
    }

    public void addBaseTypeGroupColumn(ColumnInfo columnInfo){
        baseTypeGroupColumns.put(columnInfo.getColumnName(),columnInfo);
    }

    public boolean isConstants(){
       return tableName.equals(Constants);
    }

    public boolean isEnums() {return tableName.equals(Enums); }

    public Map<String, List<Message.Builder>> getGroupBuildMap(){
        Map<String,List<Message.Builder>> groupBuildMap=new HashMap<>();
        for(Map.Entry<String,Map<String,ColumnInfo>> kv:groupColumns.entrySet()){
            groupBuildMap.put(kv.getKey(),new ArrayList<Message.Builder>(50));
        }
        return groupBuildMap;
    }

    public boolean isEmpty(){
        if(columnInfos.size()==0&&groupColumns.isEmpty() && columnEnums.size() == 0){
            return  true;
        }
        return false;
    }
    /**
     * 读取execl中的一行数据
     * @param mainBuilder
     * @param row1
     */
    public void readDataRow(String [] names,Message.Builder mainBuilder, Row row1){
        Map<String,List<Message.Builder>> groupMap=getGroupBuildMap();
        for (int k = 0; k < names.length; ++k) {
            String key =names[k];
            String subValue = MakeProtoData.getCellContent(row1.getCell(k));
            if (key.equals("")) {
                continue;
            }
            if(key.startsWith("#")) {
                continue;
            }
            String keys[]=key.split("-");
            if (keys.length==1) {
                MakeProtoData.setField(mainBuilder, key, subValue);
            }else if (keys.length==2) {//基础类型数组
                if (subValue.equals("")) {
                    continue;
                }
                MakeProtoData.addRepeatedField(mainBuilder,keys[0],subValue);
            }else{
                if (subValue.equals("")) {
                    continue;
                }
                List<Message.Builder> builderList=groupMap.get(keys[0]);
                int index= Integer.valueOf(keys[1]).intValue();
                if(builderList.size()<=index||builderList.get(index)==null){
                    String innerClassName=getInnerClassName(getFirstNameUpperCase(keys[0]));
                    Class innerClazz=getClass(innerClassName);
                    Message.Builder builder=createBuilder(innerClazz);
                    builderList.add(builder);
                }
                MakeProtoData.setField(builderList.get(index),keys[2],subValue);
            }
        }
        for(Map.Entry<String,List<Message.Builder>> kv:groupMap.entrySet()){
            Descriptors.FieldDescriptor field = mainBuilder.getDescriptorForType().findFieldByName(kv.getKey());
            if (field == null) System.err.println("cant not find field by " + kv.getKey());
            for(Message.Builder builder:kv.getValue()){
                mainBuilder.addRepeatedField(field,builder.build());
            }
        }
    }

    public String [] createProtoBufferFile(){
        if(columnInfos.isEmpty()&&this.groupColumns.isEmpty()){
            System.out.println(tableName+" is empty");
        }
        String javaOutName=getMainClassName(false,true);
        String fileName=getProtoFileName();

        StringBuilder sb=new StringBuilder();
        sb.append("// Generated by tool. DO NOT EDIT!\n");
        sb.append("//source: "+this.getTableName()+ ".exel\n");
        sb.append("syntax = \"proto3\";\n");
        sb.append("option java_package = \""+outPKName+"\";\n");
        sb.append("option java_outer_classname = \""+javaOutName+"\";\n");
        sb.append("option go_package = \""+goPackage+"\";\n");

        if (this.isEnums() && !columnEnums.isEmpty()) {
            this.addEnums(sb);
            return new String[]{fileName,sb.toString()};
        }

        if(!this.isConstants()){
            addDataListProtoDesc(sb);
        }

        if(!groupColumns.isEmpty()){
            for(Map.Entry<String,Map<String,ColumnInfo>> kv:groupColumns.entrySet()){
                String groupName=getFirstNameUpperCase(kv.getKey());
                sb.append("\nmessage " + groupName + "\n{\n");
                int i=1;
                for(ColumnInfo columnInfo:kv.getValue().values()){
                    sb.append(columnInfo.getColumnType()).append(" ").append(columnInfo.getColumnName()).append(" = ").append(i).append(";").append("\n");
                    i++;
                }
                sb.append("}");
                this.addColumn(new ColumnInfo(groupName,kv.getKey(),true));
            }
        }
        if(!this.baseTypeGroupColumns.isEmpty()){
            for(Map.Entry<String,ColumnInfo> kv:baseTypeGroupColumns.entrySet()){
                this.addColumn(new ColumnInfo(kv.getValue().getColumnType(),kv.getValue().getColumnName(),true));
            }
        }
        sb.append("\nmessage " + fileName + "\n{\n");
        int i=1;
        for(ColumnInfo cl:columnInfos){
            if(cl.isRepeated()){
                sb.append("repeated ");
            }else{
//                sb.append("required ");
            }
            sb.append(cl.getColumnType()).append(" ").append(cl.getColumnName()).append(" = ").append(i).append(";").append("\n");
            i++;
        }
        sb.append("}");
        return new String[]{fileName,sb.toString()};
    }

    private void addDataListProtoDesc(StringBuilder sb){
        String dataListName=this.getListClassName();
        String protoFileName=this.getProtoFileName();
        sb.append("\nmessage " + dataListName + "\n{\n");
        sb.append("repeated ").append(protoFileName).append(" list=").append("1;\n");
        sb.append("}");
    }

    private void addEnums(StringBuilder sb){
        for(Map.Entry<String,Map<String,String >> kv:columnEnums.entrySet()) {
            sb.append("\n");
            sb.append("message " + kv.getKey() + " {\n");
            sb.append("    enum Enums {\n");
            for (Map.Entry<String, String> enums: kv.getValue().entrySet()) {
                sb.append("        " + enums.getKey() + " = " + enums.getValue() + ";\n");
            }
            sb.append("    }\n");
            sb.append("}\n");
        }
    }

    public  String getProtoFileName(){
        StringBuilder protoFileName=new StringBuilder();
        protoFileName.append(prefix).append(getFirstNameUpperCase(tableName));
        return protoFileName.toString();
    }

    public  String getMainClassName(boolean addpk,boolean addData){
        StringBuilder sb=new StringBuilder();
        if(addpk){
            sb.append(outPKName).append(".");
        }
        sb.append(prefix).append(getFirstNameUpperCase(tableName));
        if(addData){
            sb.append("All");
        }
       return sb.toString();
    }

    public  String getListClassName(){
        StringBuilder sb=new StringBuilder();
        sb.append(prefix).append(getFirstNameUpperCase(tableName));
        sb.append("List");
        return sb.toString();
    }

    public  String getInnerClassName(String innerClassName){
        StringBuilder sb=new StringBuilder();
        sb.append(getMainClassName(true,true));
        sb.append("$");
        sb.append(innerClassName);
        return sb.toString();
    }

    public static String getFirstNameUpperCase(String name){
        String first = name.substring(0, 1);
        StringBuilder sb = new StringBuilder();
        sb.append(first.toUpperCase());
        sb.append(name.substring(1));
        String methodName = sb.toString();
        return methodName;
    }

    public Class getClass(String name){
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Message.Builder createBuilder(Class clazz)  {
        Method buildMethod= null;
        Message.Builder builder=null;
        try {
            buildMethod = clazz.getMethod("newBuilder");
            builder = (Message.Builder) buildMethod.invoke(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  builder;
    }

    public int getDataRowIndex() {
        return dataRowIndex;
    }

    public void setDataRowIndex(int dataRowIndex) {
        this.dataRowIndex = dataRowIndex;
    }

    public int getColumnNameIndex() {
        return columnNameIndex;
    }

    public void setColumnNameIndex(int columnNameIndex) {
        this.columnNameIndex = columnNameIndex;
    }

    public int getColumnTypeIndex() {
        return columnTypeIndex;
    }

    public void setColumnTypeIndex(int columnTypeIndex) {
        this.columnTypeIndex = columnTypeIndex;
    }

    public static void main(String[] args) {

    }
}
