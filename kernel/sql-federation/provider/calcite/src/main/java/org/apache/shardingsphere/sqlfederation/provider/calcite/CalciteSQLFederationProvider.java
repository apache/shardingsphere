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

package org.apache.shardingsphere.sqlfederation.provider.calcite;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContextFactory;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecution;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Calcite SQL federation provider.
 */
public final class CalciteSQLFederationProvider implements SQLFederationProvider {
    
    private final AtomicReference<CompilerContext> compilerContext = new AtomicReference<>();
    
    private SQLFederationCacheOption executionPlanCache;
    
    @Override
    public void initialize(final SQLFederationRuleConfiguration ruleConfig, final Collection<ShardingSphereDatabase> databases) {
        executionPlanCache = ruleConfig.getExecutionPlanCache();
        refresh(databases);
    }
    
    @Override
    public void refresh(final Collection<ShardingSphereDatabase> databases) {
        compilerContext.set(CompilerContextFactory.create(databases));
    }
    
    CompilerContext getCompilerContext() {
        return compilerContext.get();
    }
    
    SQLFederationCacheOption getExecutionPlanCache() {
        return executionPlanCache;
    }
    
    @HighFrequencyInvocation
    @Override
    public SQLFederationExecution createExecution(final String currentDatabaseName, final String currentSchemaName, final ShardingSphereStatistics statistics,
                                                  final JDBCExecutor jdbcExecutor, final ProcessEngine processEngine) {
        return new CalciteSQLFederationExecution(currentDatabaseName, currentSchemaName, statistics, jdbcExecutor, processEngine, this);
    }
    
    @Override
    public String getType() {
        return "CALCITE";
    }
}
