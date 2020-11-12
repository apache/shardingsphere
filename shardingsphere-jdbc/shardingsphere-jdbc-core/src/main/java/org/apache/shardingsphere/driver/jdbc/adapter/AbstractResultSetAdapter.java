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

package org.apache.shardingsphere.driver.jdbc.adapter;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationResultSet;
import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteTemplate;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

/**
 * Adapter for {@code ResultSet}.
 */
public abstract class AbstractResultSetAdapter extends AbstractUnsupportedOperationResultSet {
    
    @Getter
    private final List<ResultSet> resultSets;
    
    @Getter
    private final Statement statement;
    
    private boolean closed;
    
    private final ForceExecuteTemplate<ResultSet> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    @Getter
    private final ExecutionContext executionContext;
    
    protected AbstractResultSetAdapter(final List<ResultSet> resultSets, final Statement statement, final ExecutionContext executionContext) {
        Preconditions.checkArgument(!resultSets.isEmpty());
        this.resultSets = resultSets;
        this.statement = statement;
        this.executionContext = executionContext;
    }
    
    @Override
    public final ResultSetMetaData getMetaData() throws SQLException {
        return new ShardingSphereResultSetMetaData(resultSets.get(0).getMetaData(), getRules(), executionContext.getSqlStatementContext());
    }
    
    private Collection<ShardingSphereRule> getRules() {
        ShardingSphereConnection connection = statement instanceof ShardingSpherePreparedStatement
                ? ((ShardingSpherePreparedStatement) statement).getConnection() : ((ShardingSphereStatement) statement).getConnection();
        return connection.getMetaDataContexts().getDefaultMetaData().getRuleMetaData().getRules();
    }
    
    @Override
    public final int findColumn(final String columnLabel) throws SQLException {
        return resultSets.get(0).findColumn(columnLabel);
    }
    
    @Override
    public final void close() throws SQLException {
        closed = true;
        forceExecuteTemplate.execute(resultSets, ResultSet::close);
    }
    
    @Override
    public final boolean isClosed() {
        return closed;
    }
    
    @Override
    public final void setFetchDirection(final int direction) throws SQLException {
        forceExecuteTemplate.execute(resultSets, resultSet -> resultSet.setFetchDirection(direction));
    }
    
    @Override
    public final int getFetchDirection() throws SQLException {
        return resultSets.get(0).getFetchDirection();
    }
    
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        forceExecuteTemplate.execute(resultSets, resultSet -> resultSet.setFetchSize(rows));
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
        forceExecuteTemplate.execute(resultSets, ResultSet::clearWarnings);
    }
}
