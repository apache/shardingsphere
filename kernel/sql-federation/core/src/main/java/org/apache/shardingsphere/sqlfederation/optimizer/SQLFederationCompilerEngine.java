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

package org.apache.shardingsphere.sqlfederation.optimizer;

import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.optimizer.planner.cache.ExecutionPlanCacheBuilder;

/**
 * SQL federation compiler engine.
 */
public final class SQLFederationCompilerEngine {
    
    private final SQLFederationCompiler sqlFederationCompiler;
    
    private final LoadingCache<SQLStatement, SQLFederationExecutionPlan> executionPlanCache;
    
    public SQLFederationCompilerEngine(final SQLFederationCompiler sqlFederationCompiler, final CacheOption cacheOption) {
        this.sqlFederationCompiler = sqlFederationCompiler;
        executionPlanCache = ExecutionPlanCacheBuilder.build(cacheOption, sqlFederationCompiler);
    }
    
    /**
     * Compile sql statement to execution plan.
     *
     * @param sqlStatement SQL statement
     * @param useCache use cache
     * @return sql federation execution plan
     */
    public SQLFederationExecutionPlan compile(final SQLStatement sqlStatement, final boolean useCache) {
        return useCache ? executionPlanCache.get(sqlStatement) : sqlFederationCompiler.compile(sqlStatement);
    }
}
