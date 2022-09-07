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

package org.apache.shardingsphere.sqlfederation.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPIRegistry;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutor;

/**
 * SQL federation executor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationExecutorFactory {
    
    static {
        ShardingSphereServiceLoader.register(SQLFederationExecutor.class);
    }
    
    /**
     * Get instance of SQL federation executor.
     *
     * @param type federation executor type
     * @return got instance
     */
    public static SQLFederationExecutor getInstance(final String type) {
        return TypedSPIRegistry.findRegisteredService(SQLFederationExecutor.class, type).orElse(RequiredSPIRegistry.getRegisteredService(SQLFederationExecutor.class));
    }
    
    /**
     * Create new instance of federation executor factory.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param metaData ShardingSphere meta data
     * @param jdbcExecutor jdbc executor
     * @param eventBusContext event bus context
     * @return created instance
     */
    public static SQLFederationExecutor newInstance(final String databaseName, final String schemaName, final ShardingSphereMetaData metaData, final JDBCExecutor jdbcExecutor,
                                                    final EventBusContext eventBusContext) {
        // TODO getInstance by sql-federation-type
        SQLFederationExecutor result = getInstance("ORIGINAL");
        result.init(databaseName, schemaName, metaData, jdbcExecutor, eventBusContext);
        return result;
    }
}
