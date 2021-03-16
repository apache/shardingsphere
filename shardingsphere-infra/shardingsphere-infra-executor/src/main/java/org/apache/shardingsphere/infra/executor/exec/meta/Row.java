package org.apache.shardingsphere.infra.executor.exec.meta;

import java.util.Arrays;
import java.util.Objects;

public class Row {
    
    private Object[] columnValues;
    
    public Row() {
        columnValues = new Object[0];
    }
    
    public Row(Object[] columnValues) {
        this.columnValues = Arrays.copyOf(Objects.requireNonNull(columnValues), columnValues.length);
    }
    
    /**
     * return the column value
     * @param column 1-based value
     * @param <T>
     * @return
     */
    public <T> T getColumnValue(int column) {
        if(column > length()) {
            throw new IllegalArgumentException("illegal column index " + column + ", max length is " + length());
        }
        return getValueByColumn(column);
    }
    
    protected <T> T getValueByColumn(int column) {
        return (T)columnValues[column-1];
    }
    
    public Object[] getColumnValues() {
        return columnValues;
    }
    
    public int length() {
        return columnValues.length;
    }
}
