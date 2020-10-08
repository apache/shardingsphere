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

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.sql.LogicSQL;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingConditionEngineFactoryTest {
    
    @Mock
    private LogicSQL logicSQL;
    
    @Mock
    private ShardingRule shardingRule;
    
    @Before
    public void setUp() {
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        when(logicSQL.getSchema()).thenReturn(shardingSphereSchema);
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class);
        when(shardingSphereSchema.getMetaData()).thenReturn(shardingSphereMetaData);
        RuleSchemaMetaData ruleSchemaMetaData = mock(RuleSchemaMetaData.class);
        when(shardingSphereMetaData.getRuleSchemaMetaData()).thenReturn(ruleSchemaMetaData);
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(ruleSchemaMetaData.getConfiguredSchemaMetaData()).thenReturn(schemaMetaData);
        
    }
    
    @Test
    public void assertInsertClauseClauseShardingConditionEngine() {
        SQLStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(logicSQL.getSqlStatementContext()).thenReturn(insertStatementContext);
        ShardingConditionEngine<?> actualInsertClauseShardingConditionEngine = ShardingConditionEngineFactory.createShardingConditionEngine(logicSQL, shardingRule);
        assertTrue(actualInsertClauseShardingConditionEngine instanceof InsertClauseShardingConditionEngine);
    }
    
    @Test
    public void assertCreateWhereClauseShardingConditionEngine() {
        ShardingConditionEngine<?> actualWhereClauseShardingConditionEngine = ShardingConditionEngineFactory.createShardingConditionEngine(logicSQL, shardingRule);
        assertTrue(actualWhereClauseShardingConditionEngine instanceof WhereClauseShardingConditionEngine);
    }
}
