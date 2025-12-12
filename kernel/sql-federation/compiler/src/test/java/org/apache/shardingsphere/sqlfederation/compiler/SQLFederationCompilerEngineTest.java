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

package org.apache.shardingsphere.sqlfederation.compiler;

import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.compiler.SQLStatementCompiler;
import org.apache.shardingsphere.sqlfederation.compiler.compiler.SQLStatementCompilerEngine;
import org.apache.shardingsphere.sqlfederation.compiler.compiler.SQLStatementCompilerEngineFactory;
import org.apache.shardingsphere.sqlfederation.compiler.planner.cache.ExecutionPlanCacheKey;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(SQLStatementCompilerEngineFactory.class)
class SQLFederationCompilerEngineTest {
    
    @Test
    void assertCompileDelegatesForCacheFlags() {
        SQLFederationCacheOption cacheOption = new SQLFederationCacheOption(1, 1L);
        ExecutionPlanCacheKey cacheKey = new ExecutionPlanCacheKey("select 1", mock(SQLStatement.class), mock(SQLStatementCompiler.class));
        SQLStatementCompilerEngine statementCompilerEngine = mock(SQLStatementCompilerEngine.class);
        SQLFederationExecutionPlan expectedPlanWithCache = mock(SQLFederationExecutionPlan.class);
        SQLFederationExecutionPlan expectedPlanWithoutCache = mock(SQLFederationExecutionPlan.class);
        when(SQLStatementCompilerEngineFactory.getSQLStatementCompilerEngine("foo_db", "foo_schema", cacheOption)).thenReturn(statementCompilerEngine);
        when(statementCompilerEngine.compile(cacheKey, true)).thenReturn(expectedPlanWithCache);
        when(statementCompilerEngine.compile(cacheKey, false)).thenReturn(expectedPlanWithoutCache);
        SQLFederationCompilerEngine compilerEngine = new SQLFederationCompilerEngine("foo_db", "foo_schema", cacheOption);
        SQLFederationExecutionPlan actualPlanWithCache = compilerEngine.compile(cacheKey, true);
        SQLFederationExecutionPlan actualPlanWithoutCache = compilerEngine.compile(cacheKey, false);
        assertThat(actualPlanWithCache, is(expectedPlanWithCache));
        assertThat(actualPlanWithoutCache, is(expectedPlanWithoutCache));
        verify(statementCompilerEngine).compile(cacheKey, true);
        verify(statementCompilerEngine).compile(cacheKey, false);
    }
}
