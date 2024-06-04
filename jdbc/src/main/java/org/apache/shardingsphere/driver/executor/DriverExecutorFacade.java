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
import org.apache.shardingsphere.driver.executor.batch.BatchPreparedStatementExecutor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;

import java.sql.SQLException;

/**
 * Driver executor facade.
 */
public final class DriverExecutorFacade implements AutoCloseable {
    
    private final TrafficExecutor trafficExecutor;
    
    private final SQLFederationEngine sqlFederationEngine;
    
    @Getter
    private final DriverExecuteQueryExecutor queryExecutor;
    
    @Getter
    private final DriverExecuteUpdateExecutor updateExecutor;
    
    @Getter
    private final DriverExecuteExecutor executeExecutor;
    
    @Getter
    private final DriverExecuteBatchExecutor executeBatchExecutor;
    
    public DriverExecutorFacade(final ShardingSphereConnection connection, final BatchPreparedStatementExecutor batchPreparedStatementExecutor) {
        JDBCExecutor jdbcExecutor = new JDBCExecutor(connection.getContextManager().getExecutorEngine(), connection.getDatabaseConnectionManager().getConnectionContext());
        DriverJDBCExecutor regularExecutor = new DriverJDBCExecutor(connection.getDatabaseName(), connection.getContextManager(), jdbcExecutor);
        RawExecutor rawExecutor = new RawExecutor(connection.getContextManager().getExecutorEngine(), connection.getDatabaseConnectionManager().getConnectionContext());
        trafficExecutor = new TrafficExecutor();
        ShardingSphereMetaData metaData = connection.getContextManager().getMetaDataContexts().getMetaData();
        String schemaName = new DatabaseTypeRegistry(metaData.getDatabase(connection.getDatabaseName()).getProtocolType()).getDefaultSchemaName(connection.getDatabaseName());
        sqlFederationEngine = new SQLFederationEngine(connection.getDatabaseName(), schemaName, metaData, connection.getContextManager().getMetaDataContexts().getStatistics(), jdbcExecutor);
        queryExecutor = new DriverExecuteQueryExecutor(connection, metaData, regularExecutor, rawExecutor, trafficExecutor, sqlFederationEngine);
        updateExecutor = new DriverExecuteUpdateExecutor(connection, metaData, regularExecutor, rawExecutor, trafficExecutor);
        executeExecutor = new DriverExecuteExecutor(connection, metaData, regularExecutor, rawExecutor, trafficExecutor, sqlFederationEngine);
        executeBatchExecutor = new DriverExecuteBatchExecutor(connection, metaData, batchPreparedStatementExecutor);
    }
    
    @Override
    public void close() throws SQLException {
        trafficExecutor.close();
        sqlFederationEngine.close();
    }
}
