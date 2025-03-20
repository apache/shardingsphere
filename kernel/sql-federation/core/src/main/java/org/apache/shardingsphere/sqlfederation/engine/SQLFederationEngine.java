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

import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.optimizer.exception.SQLFederationUnsupportedSQLException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQL federation engine.
 */
public interface SQLFederationEngine extends AutoCloseable {
    
    /**
     * Judge whether SQL federation enabled or not.
     *
     * @return whether SQL federation enabled or not
     */
    boolean isSqlFederationEnabled();
    
    /**
     * Decide use SQL federation or not.
     *
     * @param queryContext query context
     * @param globalRuleMetaData global rule meta data
     * @return use SQL federation or not
     */
    boolean decide(QueryContext queryContext, RuleMetaData globalRuleMetaData);
    
    /**
     * Execute query.
     *
     * @param prepareEngine prepare engine
     * @param callback callback
     * @param federationContext federation context
     * @return result set
     * @throws SQLFederationUnsupportedSQLException SQL federation unsupported SQL exception
     */
    ResultSet executeQuery(DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                           JDBCExecutorCallback<? extends ExecuteResult> callback, SQLFederationContext federationContext);
    
    /**
     * Get result set.
     *
     * @return result set
     */
    ResultSet getResultSet();
    
    /**
     * Close.
     *
     * @throws SQLException SQL exception
     */
    void close() throws SQLException;
}
