package org.apache.shardingsphere.infra.executor.exec.meta;

import java.util.Objects;

public final class JoinRow extends Row {
    
    private Row left;
    
    private Row right;
    
    public JoinRow(final Row left, final Row right) {
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
    }
    
    @Override
    protected <T> T getValueByColumn(final int column) {
        if (column <= left.length()) {
            return left.getValueByColumn(column);
        }
        return right.getValueByColumn(column - left.length());
    }
    
    @Override
    public Object[] getColumnValues() {
        Object[] row = new Object[left.length() + right.length()];
        int idx = 0;
        for (Object val : left.getColumnValues()) {
            row[idx++] = val;
        }
        for (Object val : right.getColumnValues()) {
            row[idx++] = val;
        }
        return row;
    }
    
    @Override
    public int length() {
        return left.length() + right.length();
    }
}
