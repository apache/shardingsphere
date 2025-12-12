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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.planner.cache.ExecutionPlanCacheBuilder;
import org.apache.shardingsphere.sqlfederation.compiler.planner.cache.ExecutionPlanCacheKey;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExecutionPlanCacheBuilder.class)
class SQLStatementCompilerEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private SQLStatementCompiler sqlStatementCompiler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SQLStatement sqlStatement;
    
    private ExecutionPlanCacheKey cacheKey;
    
    @BeforeEach
    void setUp() {
        when(sqlStatement.getDatabaseType()).thenReturn(databaseType);
        cacheKey = new ExecutionPlanCacheKey("select 1", sqlStatement, sqlStatementCompiler);
    }
    
    @Test
    void assertCompileWithoutCache() {
        SQLFederationExecutionPlan expectedPlan = mock(SQLFederationExecutionPlan.class);
        when(sqlStatementCompiler.compile(sqlStatement, "FIXTURE")).thenReturn(expectedPlan);
        SQLStatementCompilerEngine engine = new SQLStatementCompilerEngine(new SQLFederationCacheOption(1, 1L));
        assertThat(engine.compile(cacheKey, false), is(expectedPlan));
        verify(sqlStatementCompiler).compile(sqlStatement, "FIXTURE");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertCompileWithCache() {
        LoadingCache<ExecutionPlanCacheKey, SQLFederationExecutionPlan> cache = mock(LoadingCache.class);
        SQLFederationExecutionPlan expectedCachedPlan = mock(SQLFederationExecutionPlan.class);
        when(cache.get(cacheKey)).thenReturn(expectedCachedPlan);
        when(ExecutionPlanCacheBuilder.build(any(SQLFederationCacheOption.class))).thenReturn(cache);
        SQLStatementCompilerEngine engine = new SQLStatementCompilerEngine(new SQLFederationCacheOption(1, 1L));
        assertThat(engine.compile(cacheKey, true), is(expectedCachedPlan));
        assertThat(engine.compile(cacheKey, true), is(expectedCachedPlan));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertUpdateCacheOption() {
        LoadingCache<ExecutionPlanCacheKey, SQLFederationExecutionPlan> cacheWithEviction = mock(LoadingCache.class);
        Policy cachePolicyWithEviction = mock(Policy.class);
        Policy.Eviction eviction = mock(Eviction.class);
        when(cachePolicyWithEviction.eviction()).thenReturn(Optional.of(eviction));
        when(cacheWithEviction.policy()).thenReturn(cachePolicyWithEviction);
        LoadingCache<ExecutionPlanCacheKey, SQLFederationExecutionPlan> cacheWithoutEviction = mock(LoadingCache.class);
        Policy cachePolicyWithoutEviction = mock(Policy.class);
        when(cachePolicyWithoutEviction.eviction()).thenReturn(Optional.empty());
        when(cacheWithoutEviction.policy()).thenReturn(cachePolicyWithoutEviction);
        when(ExecutionPlanCacheBuilder.build(any(SQLFederationCacheOption.class))).thenReturn(cacheWithEviction, cacheWithoutEviction);
        SQLStatementCompilerEngine engineWithEviction = new SQLStatementCompilerEngine(new SQLFederationCacheOption(1, 1L));
        engineWithEviction.updateCacheOption(new SQLFederationCacheOption(2, 2L));
        SQLStatementCompilerEngine engineWithoutEviction = new SQLStatementCompilerEngine(new SQLFederationCacheOption(1, 1L));
        engineWithoutEviction.updateCacheOption(new SQLFederationCacheOption(3, 3L));
        verify(eviction).setMaximum(2L);
        verify(cachePolicyWithoutEviction).eviction();
    }
}
