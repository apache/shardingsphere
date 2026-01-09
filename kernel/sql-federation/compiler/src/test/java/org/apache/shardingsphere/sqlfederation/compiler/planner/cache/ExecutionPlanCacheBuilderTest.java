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

package org.apache.shardingsphere.sqlfederation.compiler.planner.cache;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Policy;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.compiler.SQLFederationExecutionPlan;
import org.apache.shardingsphere.sqlfederation.compiler.compiler.SQLStatementCompiler;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecutionPlanCacheBuilderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBuildCreatesCacheAndLoadsWithConfiguredLimits() {
        LoadingCache<ExecutionPlanCacheKey, SQLFederationExecutionPlan> actual = ExecutionPlanCacheBuilder.build(new SQLFederationCacheOption(2, 3L));
        SQLStatementCompiler compiler = mock(SQLStatementCompiler.class);
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatement.getDatabaseType()).thenReturn(databaseType);
        SQLFederationExecutionPlan executionPlan = mock(SQLFederationExecutionPlan.class);
        when(compiler.compile(sqlStatement, "FIXTURE")).thenReturn(executionPlan);
        assertThat(actual.get(new ExecutionPlanCacheKey("select 1", sqlStatement, compiler)), is(executionPlan));
        Optional<Policy.Eviction<ExecutionPlanCacheKey, SQLFederationExecutionPlan>> evictionPolicy = actual.policy().eviction();
        assertTrue(evictionPolicy.isPresent());
        assertThat(evictionPolicy.get().getMaximum(), is(3L));
    }
}
