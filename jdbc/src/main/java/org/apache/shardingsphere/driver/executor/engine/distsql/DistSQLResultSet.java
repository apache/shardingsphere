/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.driver.executor.engine.distsql;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedGeneratedKeysResultSet;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * DistSQL result set.
 */
public final class DistSQLResultSet extends AbstractUnsupportedGeneratedKeysResultSet {
    
    private final List<String> columnNames;
    
    private final Iterator<LocalDataQueryResultRow> rows;
    
    private final Statement statement;
    
    private LocalDataQueryResultRow currentRow;
    
    private boolean closed;
    
    private boolean lastReadWasNull;
    
    public DistSQLResultSet(final Collection<String> columnNames, final Collection<LocalDataQueryResultRow> rows, final Statement statement) {
        this.columnNames = new ArrayList<>(columnNames);
        this.rows = rows.iterator();
        this.statement = statement;
    }
    
    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public boolean next() {
        if (closed || !rows.hasNext()) {
            currentRow = null;
            return false;
        }
        currentRow = rows.next();
        return true;
    }
    
    @Override
    public void close() {
        closed = true;
    }
    
    @Override
    public ResultSetMetaData getMetaData() {
        checkState();
        return new DistSQLResultSetMetaData(columnNames);
    }
    
    @Override
    public boolean wasNull() {
        checkState();
        return lastReadWasNull;
    }
    
    @Override
    public String getString(final int columnIndex) {
        checkStateForGetData();
        Object value = currentRow.getCell(columnIndex);
        lastReadWasNull = null == value;
        return null == value ? null : value.toString();
    }
    
    @Override
    public String getString(final String columnLabel) {
        return getString(findColumn(columnLabel));
    }
    
    @Override
    public String getNString(final int columnIndex) {
        return getString(columnIndex);
    }
    
    @Override
    public String getNString(final String columnLabel) {
        return getString(columnLabel);
    }
    
    @Override
    public byte getByte(final int columnIndex) {
        String value = getString(columnIndex);
        return null == value ? 0 : Byte.parseByte(value);
    }
    
    @Override
    public byte getByte(final String columnLabel) {
        return getByte(findColumn(columnLabel));
    }
    
    @Override
    public short getShort(final int columnIndex) {
        String value = getString(columnIndex);
        return null == value ? 0 : Short.parseShort(value);
    }
    
    @Override
    public short getShort(final String columnLabel) {
        return getShort(findColumn(columnLabel));
    }
    
    @Override
    public int getInt(final int columnIndex) {
        String value = getString(columnIndex);
        return null == value ? 0 : Integer.parseInt(value);
    }
    
    @Override
    public int getInt(final String columnLabel) {
        return getInt(findColumn(columnLabel));
    }
    
    @Override
    public long getLong(final int columnIndex) {
        String value = getString(columnIndex);
        return null == value ? 0L : Long.parseLong(value);
    }
    
    @Override
    public long getLong(final String columnLabel) {
        return getLong(findColumn(columnLabel));
    }
    
    @Override
    public float getFloat(final int columnIndex) {
        String value = getString(columnIndex);
        return null == value ? 0F : Float.parseFloat(value);
    }
    
    @Override
    public float getFloat(final String columnLabel) {
        return getFloat(findColumn(columnLabel));
    }
    
    @Override
    public double getDouble(final int columnIndex) {
        String value = getString(columnIndex);
        return null == value ? 0D : Double.parseDouble(value);
    }
    
    @Override
    public double getDouble(final String columnLabel) {
        return getDouble(findColumn(columnLabel));
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) {
        String value = getString(columnIndex);
        return null == value ? null : new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) {
        return getBigDecimal(findColumn(columnLabel), scale);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) {
        String value = getString(columnIndex);
        return null == value ? null : new BigDecimal(value);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) {
        return getBigDecimal(findColumn(columnLabel));
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) {
        String value = getString(columnIndex);
        return null == value ? null : value.getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) {
        return getBytes(findColumn(columnLabel));
    }
    
    @Override
    public Object getObject(final int columnIndex) {
        checkStateForGetData();
        Object value = currentRow.getCell(columnIndex);
        lastReadWasNull = null == value;
        return value;
    }
    
    @Override
    public Object getObject(final String columnLabel) {
        return getObject(findColumn(columnLabel));
    }
    
    @Override
    public int findColumn(final String columnLabel) {
        checkState();
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i).equalsIgnoreCase(columnLabel)) {
                return i + 1;
            }
        }
        throw new IllegalArgumentException("Column '" + columnLabel + "' not found");
    }
    
    @Override
    public int getType() {
        checkState();
        return TYPE_FORWARD_ONLY;
    }
    
    @Override
    public int getConcurrency() {
        checkState();
        return CONCUR_READ_ONLY;
    }
    
    @Override
    public Statement getStatement() {
        checkState();
        return statement;
    }
    
    private void checkStateForGetData() {
        checkState();
        Preconditions.checkNotNull(currentRow, "ResultSet should call next or has no more data.");
    }
    
    private void checkState() {
        Preconditions.checkState(!closed, "ResultSet has closed.");
    }
}
