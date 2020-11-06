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

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingConditionEngineFactoryTest {
    
    @Mock
    private LogicSQL logicSQL;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ShardingRule shardingRule;
    
    @Before
    public void setUp() {
        when(metaData.getSchema().getSchemaMetaData()).thenReturn(mock(PhysicalSchemaMetaData.class));
    }
    
    @Test
    public void assertCreateInsertClauseShardingConditionEngine() {
        SQLStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(logicSQL.getSqlStatementContext()).thenReturn(insertStatementContext);
        ShardingConditionEngine<?> actual = ShardingConditionEngineFactory.createShardingConditionEngine(logicSQL, metaData, shardingRule);
        assertTrue(actual instanceof InsertClauseShardingConditionEngine);
    }
    
    @Test
    public void assertCreateWhereClauseShardingConditionEngine() {
        ShardingConditionEngine<?> actual = ShardingConditionEngineFactory.createShardingConditionEngine(logicSQL, metaData, shardingRule);
        assertTrue(actual instanceof WhereClauseShardingConditionEngine);
    }
}
