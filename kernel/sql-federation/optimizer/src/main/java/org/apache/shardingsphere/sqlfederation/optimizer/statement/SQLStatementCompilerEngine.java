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

package org.apache.shardingsphere.sqlfederation.optimizer.statement;

import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.optimizer.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.optimizer.planner.cache.ExecutionPlanCacheBuilder;
import org.apache.shardingsphere.sqlfederation.optimizer.planner.cache.ExecutionPlanCacheKey;

/**
 * SQL statement compiler engine.
 */
public final class SQLStatementCompilerEngine {
    
    private final LoadingCache<ExecutionPlanCacheKey, SQLFederationExecutionPlan> executionPlanCache;
    
    public SQLStatementCompilerEngine(final CacheOption cacheOption) {
        executionPlanCache = ExecutionPlanCacheBuilder.build(cacheOption);
    }
    
    /**
     * Compile SQL statement to execution plan.
     *
     * @param cacheKey execution plan cache key
     * @param useCache use cache
     * @return SQL federation execution plan
     */
    public SQLFederationExecutionPlan compile(final ExecutionPlanCacheKey cacheKey, final boolean useCache) {
        return useCache ? executionPlanCache.get(cacheKey) : cacheKey.getSqlStatementCompiler().compile(cacheKey.getSqlStatement(), cacheKey.getDatabaseType());
    }
}
