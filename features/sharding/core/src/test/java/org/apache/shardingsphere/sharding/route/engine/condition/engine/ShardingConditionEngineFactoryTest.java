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

package org.apache.shardingsphere.sharding.route.engine.condition.engine;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingConditionEngineFactoryTest {
    
    @Mock
    private QueryContext queryContext;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingRule shardingRule;
    
    @Before
    public void setUp() {
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertCreateInsertClauseShardingConditionEngine() {
        SQLStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(queryContext.getSqlStatementContext()).thenReturn(insertStatementContext);
        ShardingConditionEngine<?> actual = ShardingConditionEngineFactory.createShardingConditionEngine(queryContext, database, shardingRule);
        assertThat(actual, instanceOf(InsertClauseShardingConditionEngine.class));
    }
    
    @Test
    public void assertCreateWhereClauseShardingConditionEngine() {
        ShardingConditionEngine<?> actual = ShardingConditionEngineFactory.createShardingConditionEngine(queryContext, database, shardingRule);
        assertThat(actual, instanceOf(WhereClauseShardingConditionEngine.class));
    }
}
