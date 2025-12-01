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

package org.apache.shardingsphere.sqlfederation.engine.processor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.sqlfederation.engine.processor.impl.StandardSQLFederationProcessor;

/**
 * SQL federation processor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationProcessorFactory {
    
    private static final SQLFederationProcessorFactory INSTANCE = new SQLFederationProcessorFactory();
    
    /**
     * Get sql federation processor factory instance.
     *
     * @return sql federation processor factory instance
     */
    public static SQLFederationProcessorFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Create new instance of {@link SQLFederationProcessor}.
     *
     * @param statistics shardingSphere statistics
     * @param jdbcExecutor JDBC executor
     * @return created instance
     */
    public SQLFederationProcessor newInstance(final ShardingSphereStatistics statistics, final JDBCExecutor jdbcExecutor) {
        return new StandardSQLFederationProcessor(statistics, jdbcExecutor);
    }
}
