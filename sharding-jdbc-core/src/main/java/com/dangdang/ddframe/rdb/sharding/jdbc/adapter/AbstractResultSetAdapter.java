/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 处理多结果集的适配器.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractResultSetAdapter extends AbstractResultSetGetterAdapter {
    
    @Getter
    private final List<ResultSet> resultSets;
    
    private boolean closed;
    
    @Override
    public final void close() throws SQLException {
        for (ResultSet each : resultSets) {
            each.close();
        }
        closed = true;
    }
    
    @Override
    public final boolean isClosed() throws SQLException {
        return closed;
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
        for (ResultSet each : resultSets) {
            each.setFetchDirection(direction);
        }
    }
    
    @Override
    public final int getFetchSize() throws SQLException {
        return getCurrentResultSet().getFetchSize();
    }
    
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        for (ResultSet each : resultSets) {
            each.setFetchSize(rows);
        }
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
}
