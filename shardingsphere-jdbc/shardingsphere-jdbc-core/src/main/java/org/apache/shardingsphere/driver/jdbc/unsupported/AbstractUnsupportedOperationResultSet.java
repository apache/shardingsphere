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

package org.apache.shardingsphere.driver.jdbc.unsupported;

import java.io.Reader;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;

/**
 * Unsupported {@code ResultSet} methods.
 */
public abstract class AbstractUnsupportedOperationResultSet extends AbstractUnsupportedUpdateOperationResultSet {
    
    @Override
    public boolean previous() throws SQLException {
        throw new SQLFeatureNotSupportedException("previous");
    }
    
    @Override
    public boolean isBeforeFirst() throws SQLException {
        throw new SQLFeatureNotSupportedException("isBeforeFirst");
    }
    
    @Override
    public boolean isAfterLast() throws SQLException {
        throw new SQLFeatureNotSupportedException("isAfterLast");
    }
    
    @Override
    public boolean isFirst() throws SQLException {
        throw new SQLFeatureNotSupportedException("isFirst");
    }
    
    @Override
    public boolean isLast() throws SQLException {
        throw new SQLFeatureNotSupportedException("isLast");
    }
    
    @Override
    public void beforeFirst() throws SQLException {
        throw new SQLFeatureNotSupportedException("beforeFirst");
    }
    
    @Override
    public void afterLast() throws SQLException {
        throw new SQLFeatureNotSupportedException("afterLast");
    }
    
    @Override
    public boolean first() throws SQLException {
        throw new SQLFeatureNotSupportedException("first");
    }
    
    @Override
    public boolean last() throws SQLException {
        throw new SQLFeatureNotSupportedException("last");
    }
    
    @Override
    public boolean absolute(final int row) throws SQLException {
        throw new SQLFeatureNotSupportedException("absolute");
    }
    
    @Override
    public boolean relative(final int rows) throws SQLException {
        throw new SQLFeatureNotSupportedException("relative");
    }
    
    @Override
    public int getRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("getRow");
    }
    
    @Override
    public final void insertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("insertRow");
    }
    
    @Override
    public final void updateRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("updateRow");
    }
    
    @Override
    public final void deleteRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("deleteRow");
    }
    
    @Override
    public final void refreshRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("refreshRow");
    }
    
    @Override
    public final void cancelRowUpdates() throws SQLException {
        throw new SQLFeatureNotSupportedException("cancelRowUpdates");
    }
    
    @Override
    public final void moveToInsertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("moveToInsertRow");
    }
    
    @Override
    public final void moveToCurrentRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("moveToCurrentRow");
    }
    
    @Override
    public final boolean rowInserted() throws SQLException {
        throw new SQLFeatureNotSupportedException("rowInserted");
    }
    
    @Override
    public final boolean rowUpdated() throws SQLException {
        throw new SQLFeatureNotSupportedException("rowUpdated");
    }
    
    @Override
    public final boolean rowDeleted() throws SQLException {
        throw new SQLFeatureNotSupportedException("rowDeleted");
    }
    
    @Override
    public final String getCursorName() throws SQLException {
        throw new SQLFeatureNotSupportedException("getCursorName");
    }
    
    @Override
    public final int getHoldability() throws SQLException {
        throw new SQLFeatureNotSupportedException("getHoldability");
    }
    
    @Override
    public final NClob getNClob(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getNClob");
    }
    
    @Override
    public final NClob getNClob(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getNClob");
    }
    
    @Override
    public final Reader getNCharacterStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getNCharacterStream");
    }
    
    @Override
    public final Reader getNCharacterStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getNCharacterStream");
    }
    
    @Override
    public final Ref getRef(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getRef");
    }
    
    @Override
    public final Ref getRef(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getRef");
    }
    
    @Override
    public final RowId getRowId(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("getRowId");
    }
    
    @Override
    public final RowId getRowId(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("getRowId");
    }
    
    @Override
    public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException("getObject with type");
    }
    
    @Override
    public <T> T getObject(final String columnLabel, final Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException("getObject with type");
    }
    
    @Override
    public final Object getObject(final String columnLabel, final Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("getObject with map");
    }
    
    @Override
    public final Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("getObject with map");
    }
}
