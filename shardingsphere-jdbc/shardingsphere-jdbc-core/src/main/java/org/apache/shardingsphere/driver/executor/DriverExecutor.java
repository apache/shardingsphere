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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutor;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutorFactory;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;

import java.sql.SQLException;

/**
 * Driver executor.
 */
@Getter
public final class DriverExecutor implements AutoCloseable {
    
    private final DriverJDBCExecutor regularExecutor;
    
    private final RawExecutor rawExecutor;
    
    private final FederationExecutor federationExecutor;
    
    private final TrafficExecutor trafficExecutor;
    
    public DriverExecutor(final ShardingSphereConnection connection) {
        MetaDataContexts metaDataContexts = connection.getContextManager().getMetaDataContexts();
        ExecutorEngine executorEngine = connection.getContextManager().getExecutorEngine();
        EventBusContext eventBusContext = connection.getContextManager().getInstanceContext().getEventBusContext();
        JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, connection.isHoldTransaction());
        regularExecutor = new DriverJDBCExecutor(connection.getDatabaseName(), connection.getContextManager(), jdbcExecutor);
        rawExecutor = new RawExecutor(executorEngine, connection.isHoldTransaction(), metaDataContexts.getMetaData().getProps(), eventBusContext);
        DatabaseType databaseType = metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResource().getDatabaseType();
        String schemaName = DatabaseTypeEngine.getDefaultSchemaName(databaseType, connection.getDatabaseName());
        OptimizerContext optimizerContext = OptimizerContextFactory.create(metaDataContexts.getMetaData().getDatabases(), metaDataContexts.getMetaData().getGlobalRuleMetaData());
        federationExecutor = FederationExecutorFactory.newInstance(connection.getDatabaseName(), schemaName,
                optimizerContext, metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps(), jdbcExecutor, eventBusContext);
        trafficExecutor = new TrafficExecutor();
    }
    
    /**
     * Close.
     *
     * @throws SQLException SQL exception
     */
    @Override
    public void close() throws SQLException {
        federationExecutor.close();
        trafficExecutor.close();
    }
}
