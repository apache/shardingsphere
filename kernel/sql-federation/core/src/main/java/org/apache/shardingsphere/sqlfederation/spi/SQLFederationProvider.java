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

package org.apache.shardingsphere.sqlfederation.spi;

import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;

import java.util.Collection;

/**
 * SQL federation provider owned by a global SQL federation rule.
 */
public interface SQLFederationProvider extends TypedSPI {
    
    /**
     * Initialize shared provider state once for a rule.
     *
     * @param ruleConfig rule configuration
     * @param databases databases
     */
    void initialize(SQLFederationRuleConfiguration ruleConfig, Collection<ShardingSphereDatabase> databases);
    
    /**
     * Refresh the shared state held by this provider instance.
     *
     * @param databases databases
     */
    void refresh(Collection<ShardingSphereDatabase> databases);
    
    /**
     * Create an execution object for one SQL federation engine.
     *
     * @param currentDatabaseName current database name
     * @param currentSchemaName current schema name
     * @param statistics statistics
     * @param jdbcExecutor JDBC executor
     * @param processEngine process engine
     * @return SQL federation execution
     */
    SQLFederationExecution createExecution(String currentDatabaseName, String currentSchemaName, ShardingSphereStatistics statistics, JDBCExecutor jdbcExecutor, ProcessEngine processEngine);
    
    @Override
    String getType();
}
