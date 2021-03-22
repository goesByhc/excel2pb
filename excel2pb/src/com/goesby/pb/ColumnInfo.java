package com.goesby.pb;

/**
 *  列信息
 *  auth:goesby
 */
public class ColumnInfo {
    private String columnType;
    private String columnName;
    private boolean isRepeated;
    public ColumnInfo(String columnType, String columnName) {
        setColumnType(columnType);
        this.columnName = columnName;
    }

    public ColumnInfo(String columnType, String columnName,boolean isRepeated) {
        setColumnType(columnType);
        this.columnName = columnName;
        this.isRepeated=isRepeated;
    }

    public String getColumnType() {
        return columnType;
    }
    public void setColumnType(String columnType) {
        if(columnType==null||columnType.length()==0||columnType.equals("int")){
            this.columnType = "int32";
        }
        else if (columnType.equals("long")) {
            this.columnType = "int64";
        }
        else{
            this.columnType = columnType;
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isRepeated() {
        return isRepeated;
    }

    public void setRepeated(boolean repeated) {
        isRepeated = repeated;
    }
}
