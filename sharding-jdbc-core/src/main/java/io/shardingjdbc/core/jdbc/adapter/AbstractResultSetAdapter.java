/*
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

package io.shardingjdbc.core.jdbc.adapter;

import io.shardingjdbc.core.jdbc.unsupported.AbstractUnsupportedOperationResultSet;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Adapter for {@code ResultSet}.
 * 
 * @author zhangliang
 */
@Slf4j
public abstract class AbstractResultSetAdapter extends AbstractUnsupportedOperationResultSet {
    
    @Getter
    private final List<ResultSet> resultSets;

    private boolean closed;
    
    public AbstractResultSetAdapter(final List<ResultSet> resultSets) {
        Preconditions.checkArgument(!resultSets.isEmpty());
        this.resultSets = resultSets;
    }
    
    @Override
    // TODO should return sharding statement in future
    public final Statement getStatement() throws SQLException {
        return getResultSets().get(0).getStatement();
    }
    
    @Override
    public final ResultSetMetaData getMetaData() throws SQLException {
        return getResultSets().get(0).getMetaData();
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        return getResultSets().get(0).findColumn(columnLabel);
    }
    
    @Override
    public final void close() throws SQLException {
        closed = true;
        Collection<SQLException> exceptions = new LinkedList<>();
        for (ResultSet each : resultSets) {
            try {
                each.close();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    @Override
    public final boolean isClosed() throws SQLException {
        return closed;
    }
    
    @Override
    public final void setFetchDirection(final int direction) throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (ResultSet each : resultSets) {
            try {
                each.setFetchDirection(direction);
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    @Override
    public final int getFetchDirection() throws SQLException {
        return getResultSets().get(0).getFetchDirection();
    }
    
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (ResultSet each : resultSets) {
            try {
                each.setFetchSize(rows);
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    @Override
    public final int getFetchSize() throws SQLException {
        return getResultSets().get(0).getFetchSize();
    }
    
    @Override
    public final int getType() throws SQLException {
        return getResultSets().get(0).getType();
    }
    
    @Override
    public final int getConcurrency() throws SQLException {
        return getResultSets().get(0).getConcurrency();
    }
    
    @Override
    public final SQLWarning getWarnings() throws SQLException {
        return getResultSets().get(0).getWarnings();
    }
    
    @Override
    public final void clearWarnings() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (ResultSet each : getResultSets()) {
            try {
                each.clearWarnings();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
}
