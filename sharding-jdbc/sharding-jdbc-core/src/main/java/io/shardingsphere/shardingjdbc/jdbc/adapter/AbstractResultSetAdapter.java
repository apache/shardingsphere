/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.adapter;

import com.google.common.base.Preconditions;
import io.shardingsphere.shardingjdbc.jdbc.adapter.executor.ForceExecuteCallback;
import io.shardingsphere.shardingjdbc.jdbc.adapter.executor.ForceExecuteTemplate;
import io.shardingsphere.shardingjdbc.jdbc.core.resultset.ShardingResultSetMetaData;
import io.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationResultSet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
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
    
    @Getter
    private final Statement statement;
    
    private boolean closed;
    
    private final ForceExecuteTemplate<ResultSet> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    public AbstractResultSetAdapter(final List<ResultSet> resultSets, final Statement statement) {
        Preconditions.checkArgument(!resultSets.isEmpty());
        this.resultSets = resultSets;
        this.statement = statement;
    }
    
    @Override
    public final ResultSetMetaData getMetaData() throws SQLException {
        return new ShardingResultSetMetaData(resultSets.get(0).getMetaData());
    }
    
    @Override
    public final int findColumn(final String columnLabel) throws SQLException {
        return resultSets.get(0).findColumn(columnLabel);
    }
    
    @Override
    public final void close() throws SQLException {
        closed = true;
        forceExecuteTemplate.execute(resultSets, new ForceExecuteCallback<ResultSet>() {
            
            @Override
            public void execute(final ResultSet resultSet) throws SQLException {
                resultSet.close();
            }
        });
    }
    
    @Override
    public final boolean isClosed() {
        return closed;
    }
    
    @Override
    public final void setFetchDirection(final int direction) throws SQLException {
        forceExecuteTemplate.execute(resultSets, new ForceExecuteCallback<ResultSet>() {
            
            @Override
            public void execute(final ResultSet resultSet) throws SQLException {
                resultSet.setFetchDirection(direction);
            }
        });
    }
    
    @Override
    public final int getFetchDirection() throws SQLException {
        return resultSets.get(0).getFetchDirection();
    }
    
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        forceExecuteTemplate.execute(resultSets, new ForceExecuteCallback<ResultSet>() {
            
            @Override
            public void execute(final ResultSet resultSet) throws SQLException {
                resultSet.setFetchSize(rows);
            }
        });
    }
    
    @Override
    public final int getFetchSize() throws SQLException {
        return resultSets.get(0).getFetchSize();
    }
    
    @Override
    public final int getType() throws SQLException {
        return resultSets.get(0).getType();
    }
    
    @Override
    public final int getConcurrency() throws SQLException {
        return resultSets.get(0).getConcurrency();
    }
    
    @Override
    public final SQLWarning getWarnings() throws SQLException {
        return resultSets.get(0).getWarnings();
    }
    
    @Override
    public final void clearWarnings() throws SQLException {
        forceExecuteTemplate.execute(resultSets, new ForceExecuteCallback<ResultSet>() {
            
            @Override
            public void execute(final ResultSet resultSet) throws SQLException {
                resultSet.clearWarnings();
            }
        });
    }
}
