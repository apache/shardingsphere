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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.sqlfederation.engine.impl.StandardSQLFederationEngine;

/**
 * SQL federation engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationEngineFactory {
    
    private static final SQLFederationEngineFactory INSTANCE = new SQLFederationEngineFactory();
    
    /**
     * Get backend handler factory instance.
     *
     * @return backend handler factory
     */
    public static SQLFederationEngineFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Create new instance of {@link SQLFederationEngine}.
     *
     * @param currentDatabaseName current database name
     * @param currentSchemaName current schema name
     * @param metaData shardingSphere meta data
     * @param statistics shardingSphere statistics
     * @param jdbcExecutor JDBC executor
     * @return created instance
     */
    public SQLFederationEngine newInstance(final String currentDatabaseName, final String currentSchemaName, final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics,
                                           final JDBCExecutor jdbcExecutor) {
        return new StandardSQLFederationEngine(currentDatabaseName, currentSchemaName, metaData, statistics, jdbcExecutor);
    }
}
