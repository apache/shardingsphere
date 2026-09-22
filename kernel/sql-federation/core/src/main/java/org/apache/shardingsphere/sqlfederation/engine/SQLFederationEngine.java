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

package org.apache.shardingsphere.sqlfederation.engine;

import lombok.Getter;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dal.ExplainStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationDecider;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecution;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL federation engine.
 */
@Getter
public final class SQLFederationEngine implements AutoCloseable {
    
    private final ProcessEngine processEngine = new ProcessEngine();
    
    @SuppressWarnings("rawtypes")
    private volatile Map<ShardingSphereRule, SQLFederationDecider> deciders;
    
    private final ShardingSphereMetaData metaData;
    
    private final String currentDatabaseName;
    
    private final String currentSchemaName;
    
    private final SQLFederationRule sqlFederationRule;
    
    private final SQLFederationExecution execution;
    
    private QueryContext queryContext;
    
    private ResultSet resultSet;
    
    @HighFrequencyInvocation
    public SQLFederationEngine(final String currentDatabaseName, final String currentSchemaName, final ShardingSphereMetaData metaData,
                               final ShardingSphereStatistics statistics, final JDBCExecutor jdbcExecutor) {
        this.metaData = metaData;
        this.currentDatabaseName = currentDatabaseName;
        this.currentSchemaName = currentSchemaName;
        sqlFederationRule = metaData.getGlobalRuleMetaData().getSingleRule(SQLFederationRule.class);
        execution = isSQLFederationEnabled() ? sqlFederationRule.getProvider().createExecution(currentDatabaseName, currentSchemaName, statistics, jdbcExecutor, processEngine) : null;
    }
    
    /**
     * Judge whether SQL federation enabled.
     *
     * @return SQL federation enabled or disabled
     */
    public boolean isSQLFederationEnabled() {
        return sqlFederationRule.getConfiguration().isSqlFederationEnabled();
    }
    
    @SuppressWarnings("rawtypes")
    private Map<ShardingSphereRule, SQLFederationDecider> getDeciders() {
        if (null == deciders) {
            synchronized (this) {
                if (null == deciders) {
                    deciders = OrderedSPILoader.getServices(SQLFederationDecider.class, metaData.getDatabase(currentDatabaseName).getRuleMetaData().getRules());
                }
            }
        }
        return deciders;
    }
    
    /**
     * Decide use SQL federation or not.
     *
     * @param queryContext query context
     * @param globalRuleMetaData global rule meta data
     * @return use SQL federation or not
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean decide(final QueryContext queryContext, final RuleMetaData globalRuleMetaData) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        if (!isSQLFederationEnabled() || !isSupportedSQLStatementContext(sqlStatementContext)) {
            return false;
        }
        boolean allQueryUseSQLFederation = sqlFederationRule.getConfiguration().isAllQueryUseSQLFederation();
        if (allQueryUseSQLFederation) {
            return true;
        }
        Collection<String> databaseNames = sqlStatementContext.getTablesContext().getDatabaseNames();
        if (databaseNames.size() > 1) {
            return true;
        }
        ShardingSphereDatabase usedDatabase = queryContext.getUsedDatabase();
        Collection<DataNode> includedDataNodes = new HashSet<>();
        for (Entry<ShardingSphereRule, SQLFederationDecider> entry : getDeciders().entrySet()) {
            boolean isUseSQLFederation = entry.getValue().decide(sqlStatementContext, queryContext.getParameters(), globalRuleMetaData, usedDatabase, entry.getKey(), includedDataNodes);
            if (isUseSQLFederation) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSupportedSQLStatementContext(final SQLStatementContext sqlStatementContext) {
        return isSupportedSQLStatement(sqlStatementContext instanceof ExplainStatementContext
                ? ((ExplainStatementContext) sqlStatementContext).getSqlStatement().getExplainableSQLStatement()
                : sqlStatementContext.getSqlStatement());
    }
    
    private boolean isSupportedSQLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement;
    }
    
    /**
     * Execute query.
     *
     * @param prepareEngine prepare engine
     * @param callback callback
     * @param federationContext federation context
     * @return result set
     * @throws IllegalStateException if SQL Federation is disabled
     */
    @HighFrequencyInvocation
    public ResultSet executeQuery(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                  final JDBCExecutorCallback<? extends ExecuteResult> callback, final SQLFederationContext federationContext) {
        if (!isSQLFederationEnabled()) {
            throw new IllegalStateException("SQL Federation is disabled.");
        }
        queryContext = federationContext.getQueryContext();
        resultSet = execution.executeQuery(prepareEngine, callback, federationContext);
        return resultSet;
    }
    
    /**
     * Get result set.
     *
     * @return result set
     */
    public ResultSet getResultSet() {
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        return sqlStatement instanceof SelectStatement || sqlStatement instanceof ExplainStatement ? resultSet : null;
    }
    
    @Override
    public void close() throws SQLException {
        if (null != execution) {
            execution.close();
        }
    }
}
