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
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.Policy.Eviction;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.planner.cache.ExecutionPlanCacheBuilder;
import org.apache.shardingsphere.sqlfederation.compiler.planner.cache.ExecutionPlanCacheKey;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExecutionPlanCacheBuilder.class)
class SQLStatementCompilerEngineFactoryTest {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertGetSQLStatementCompilerEngineCoversAllBranches() {
        LoadingCache<ExecutionPlanCacheKey, SQLFederationExecutionPlan> cacheWithEviction = mock(LoadingCache.class);
        Policy cachePolicyWithEviction = mock(Policy.class);
        Eviction eviction = mock(Eviction.class);
        when(cachePolicyWithEviction.eviction()).thenReturn(Optional.of(eviction));
        when(cacheWithEviction.policy()).thenReturn(cachePolicyWithEviction);
        when(ExecutionPlanCacheBuilder.build(any(SQLFederationCacheOption.class))).thenReturn(cacheWithEviction, mock(LoadingCache.class));
        SQLStatementCompilerEngine actualCreatedEngine = SQLStatementCompilerEngineFactory.getSQLStatementCompilerEngine("factory_db", "factory_schema", new SQLFederationCacheOption(1, 1L));
        SQLStatementCompilerEngine actualUpdatedEngine = SQLStatementCompilerEngineFactory.getSQLStatementCompilerEngine("factory_db", "factory_schema", new SQLFederationCacheOption(1, 2L));
        SQLStatementCompilerEngine actualReplacedEngine = SQLStatementCompilerEngineFactory.getSQLStatementCompilerEngine("factory_db", "factory_schema", new SQLFederationCacheOption(2, 2L));
        SQLStatementCompilerEngine actualUnchangedEngine = SQLStatementCompilerEngineFactory.getSQLStatementCompilerEngine("factory_db", "factory_schema", new SQLFederationCacheOption(2, 2L));
        assertThat(actualUpdatedEngine, is(actualCreatedEngine));
        assertThat(actualReplacedEngine, is(not(sameInstance(actualCreatedEngine))));
        assertThat(actualUnchangedEngine, is(actualReplacedEngine));
        verify(eviction).setMaximum(2L);
    }
}
