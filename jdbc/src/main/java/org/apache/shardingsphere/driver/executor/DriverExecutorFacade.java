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

package org.apache.shardingsphere.driver.executor;

import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Driver executor facade.
 */
@Getter
public final class DriverExecutorFacade implements AutoCloseable {
    
    private final ShardingSphereConnection connection;
    
    private final DriverJDBCExecutor regularExecutor;
    
    private final RawExecutor rawExecutor;
    
    private final TrafficExecutor trafficExecutor;
    
    private final SQLFederationEngine sqlFederationEngine;
    
    private final List<Statement> statements = new ArrayList<>();
    
    private final List<List<Object>> parameterSets = new ArrayList<>();
    
    public DriverExecutorFacade(final ShardingSphereConnection connection) {
        this.connection = connection;
        ExecutorEngine executorEngine = connection.getContextManager().getExecutorEngine();
        JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, connection.getDatabaseConnectionManager().getConnectionContext());
        regularExecutor = new DriverJDBCExecutor(connection.getDatabaseName(), connection.getContextManager(), jdbcExecutor);
        rawExecutor = new RawExecutor(executorEngine, connection.getDatabaseConnectionManager().getConnectionContext());
        trafficExecutor = new TrafficExecutor();
        ShardingSphereMetaData metaData = connection.getContextManager().getMetaDataContexts().getMetaData();
        String schemaName = new DatabaseTypeRegistry(metaData.getDatabase(connection.getDatabaseName()).getProtocolType()).getDefaultSchemaName(connection.getDatabaseName());
        sqlFederationEngine = new SQLFederationEngine(connection.getDatabaseName(), schemaName, metaData, connection.getContextManager().getMetaDataContexts().getStatistics(), jdbcExecutor);
    }
    
    /**
     * Clear statements.
     *
     * @throws SQLException SQL exception
     */
    public void clearStatements() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
        statements.clear();
    }
    
    /**
     * Clear.
     */
    public void clear() {
        statements.clear();
        parameterSets.clear();
    }
    
    @Override
    public void close() throws SQLException {
        sqlFederationEngine.close();
        trafficExecutor.close();
    }
}
