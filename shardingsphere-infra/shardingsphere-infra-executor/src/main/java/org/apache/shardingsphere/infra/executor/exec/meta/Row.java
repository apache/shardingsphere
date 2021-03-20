package org.apache.shardingsphere.infra.executor.exec.meta;

import java.util.Arrays;
import java.util.Objects;

public class Row {
    
    private Object[] columnValues;
    
    public Row() {
        columnValues = new Object[0];
    }
    
    public Row(final Object[] columnValues) {
        this.columnValues = Arrays.copyOf(Objects.requireNonNull(columnValues), columnValues.length);
    }
    
    /**
     * return the column value.
     * @param column 1-based value
     * @param <T> result data type
     * @return column value
     */
    public <T> T getColumnValue(final int column) {
        if (column > length()) {
            throw new IllegalArgumentException("illegal column index " + column + ", max length is " + length());
        }
        return getValueByColumn(column);
    }
    
    /**
     * Get column values.
     * @param column column index
     * @param <T> result data type
     * @return column values
     */
    protected <T> T getValueByColumn(final int column) {
        return (T) columnValues[column - 1];
    }
    
    /**
     * Get column values.
     * @return column values
     */
    public Object[] getColumnValues() {
        return columnValues;
    }
    
    /**
     * column number of this <code>Row</code>.
     * @return column number
     */
    public int length() {
        return columnValues.length;
    }
}
