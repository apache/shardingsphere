/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter;

import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * 代理结果集.
 * 
 * @author gaohongtao
 */
@Slf4j
public abstract class AbstractDelegateResultSetAdapter extends AbstractResultSetGetterAdapter {
    
    private int offset;
    
    protected void setDelegatedResultSet(final ResultSet resultSet) {
        setCurrentResultSet(resultSet);
    }
    
    protected void increaseStat() {
        offset++;
        log.trace(toString());
    }
    
    @Override
    public boolean next() throws SQLException {
        boolean result = getCurrentResultSet().next();
        if (result) {
            increaseStat();
        }
        return result;
    }
    
    @Override
    public final void close() throws SQLException {
        getCurrentResultSet().close();
    }
    
    @Override
    public final boolean isClosed() throws SQLException {
        return getCurrentResultSet().isClosed();
    }
    
    @Override
    public final boolean wasNull() throws SQLException {
        return getCurrentResultSet().wasNull();
    }
    
    @Override
    public final int getFetchDirection() throws SQLException {
        return getCurrentResultSet().getFetchDirection();
    }
    
    @Override
    public final void setFetchDirection(final int direction) throws SQLException {
        getCurrentResultSet().setFetchDirection(direction);
    }
    
    @Override
    public final int getFetchSize() throws SQLException {
        return getCurrentResultSet().getFetchSize();
    }
    
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        getCurrentResultSet().setFetchSize(rows);
    }
    
    @Override
    public final int getType() throws SQLException {
        return getCurrentResultSet().getType();
    }
    
    @Override
    public final int getConcurrency() throws SQLException {
        return getCurrentResultSet().getConcurrency();
    }
    
    @Override
    public final Statement getStatement() throws SQLException {
        return getCurrentResultSet().getStatement();
    }
    
    @Override
    public final SQLWarning getWarnings() throws SQLException {
        return getCurrentResultSet().getWarnings();
    }
    
    @Override
    public final void clearWarnings() throws SQLException {
        getCurrentResultSet().clearWarnings();
    }
    
    @Override
    public final ResultSetMetaData getMetaData() throws SQLException {
        return getCurrentResultSet().getMetaData();
    }
    
    @Override
    public final int findColumn(final String columnLabel) throws SQLException {
        return getCurrentResultSet().findColumn(columnLabel);
    }
    
    @Override
    public String toString() {
        return String.format("%s(%d)'s offset is %d", this.getClass().getSimpleName(), hashCode(), offset);
    }
}
