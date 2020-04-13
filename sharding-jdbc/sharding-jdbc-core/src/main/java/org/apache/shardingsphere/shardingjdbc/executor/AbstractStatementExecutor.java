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

package org.apache.shardingsphere.shardingjdbc.executor;

import lombok.Getter;
import org.apache.shardingsphere.sharding.execute.sql.execute.SQLExecuteTemplate;
import org.apache.shardingsphere.sharding.execute.sql.execute.SQLExecutorCallback;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.executor.StatementExecuteUnit;
import org.apache.shardingsphere.underlying.executor.connection.StatementOption;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.kernel.InputGroup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract statement executor.
 */
@Getter
public abstract class AbstractStatementExecutor {
    
    private final ShardingConnection connection;
    
    private final List<Statement> statements;
    
    private final List<ResultSet> resultSets;
    
    private final Collection<InputGroup<StatementExecuteUnit>> inputGroups;
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    public AbstractStatementExecutor(final ShardingConnection shardingConnection) {
        this.connection = shardingConnection;
        statements = new LinkedList<>();
        resultSets = new CopyOnWriteArrayList<>();
        inputGroups = new LinkedList<>();
        sqlExecuteTemplate = new SQLExecuteTemplate(connection.getRuntimeContext().getExecutorKernel(), connection.isHoldTransaction());
    }
    
    /**
     * Initialize executor.
     *
     * @param executionContext execution context
     * @param statementOption statement option
     * @throws SQLException SQL exception
     */
    public abstract void init(ExecutionContext executionContext, StatementOption statementOption) throws SQLException;
    
    /**
     * To make sure SkyWalking will be available at the next release of ShardingSphere,
     * a new plugin should be provided to SkyWalking project if this API changed.
     * 
     * @see <a href="https://github.com/apache/skywalking/blob/master/docs/en/guides/Java-Plugin-Development-Guide.md#user-content-plugin-development-guide">Plugin Development Guide</a>
     * 
     * @param executeCallback execute callback
     * @param <T> class type of return value 
     * @return result
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    protected final <T> List<T> executeCallback(final SQLExecutorCallback<T> executeCallback) throws SQLException {
        return sqlExecuteTemplate.execute((Collection) inputGroups, executeCallback);
    }
    
    /**
     * Is accumulate or not.
     *
     * @param sqlStatementContext SQL statement context
     * @return accumulate or not
     */
    public final boolean isAccumulate(final SQLStatementContext sqlStatementContext) {
        return !connection.getRuntimeContext().getRule().isAllBroadcastTables(sqlStatementContext.getTablesContext().getTableNames());
    }
    
    /**
     * Clear.
     *
     * @throws SQLException SQL exception
     */
    public void clear() throws SQLException {
        closeStatements();
        statements.clear();
        resultSets.clear();
        inputGroups.clear();
    }
    
    private void closeStatements() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
    }
}
