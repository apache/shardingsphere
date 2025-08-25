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

package org.apache.shardingsphere.sqlfederation.compiler.compiler;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.planner.cache.ExecutionPlanCacheBuilder;
import org.apache.shardingsphere.sqlfederation.compiler.planner.cache.ExecutionPlanCacheKey;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;

/**
 * SQL statement compiler engine.
 */
@Slf4j
public final class SQLStatementCompilerEngine {
    
    private final LoadingCache<ExecutionPlanCacheKey, SQLFederationExecutionPlan> executionPlanCache;
    
    @Getter
    private final SQLFederationCacheOption cacheOption;
    
    public SQLStatementCompilerEngine(final SQLFederationCacheOption cacheOption) {
        executionPlanCache = ExecutionPlanCacheBuilder.build(cacheOption);
        this.cacheOption = cacheOption;
    }
    
    /**
     * Compile SQL statement to execution plan.
     *
     * @param cacheKey execution plan cache key
     * @param useCache use cache
     * @return SQL federation execution plan
     */
    public SQLFederationExecutionPlan compile(final ExecutionPlanCacheKey cacheKey, final boolean useCache) {
        if (log.isDebugEnabled()) {
            String cacheExists = null == executionPlanCache.get(cacheKey) ? "not exists" : "exists";
            log.debug("Execution plan cache {} for SQL: {}, useCache: {}.", cacheExists, cacheKey.getSql(), useCache);
        }
        return useCache ? executionPlanCache.get(cacheKey) : cacheKey.getSqlStatementCompiler().compile(cacheKey.getSqlStatement(), cacheKey.getSqlStatement().getDatabaseType().getType());
    }
    
    /**
     * Update cache option.
     *
     * @param cacheOption cache option
     */
    public void updateCacheOption(final SQLFederationCacheOption cacheOption) {
        executionPlanCache.policy().eviction().ifPresent(optional -> optional.setMaximum(cacheOption.getMaximumSize()));
    }
}
