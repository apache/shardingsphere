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

package org.apache.shardingsphere.sqlfederation.optimizer.context;

import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.optimizer.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.sqlfederation.optimizer.context.planner.OptimizerPlannerContext;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OptimizerContextTest {
    
    @Test
    public void assertGetSqlParserRule() {
        OptimizerContext actual = OptimizerContextFactory.create(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, createShardingSphereDatabase()), createShardingSphereRuleMetaData());
        assertThat(actual.getSqlParserRule(), instanceOf(SQLParserRule.class));
    }
    
    @Test
    public void assertGetParserContext() {
        OptimizerContext actual = OptimizerContextFactory.create(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, createShardingSphereDatabase()), createShardingSphereRuleMetaData());
        assertThat(actual.getParserContext(DefaultDatabase.LOGIC_NAME.toLowerCase()), instanceOf(OptimizerParserContext.class));
    }
    
    @Test
    public void assertGetPlannerContext() {
        OptimizerContext actual = OptimizerContextFactory.create(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, createShardingSphereDatabase()), createShardingSphereRuleMetaData());
        assertThat(actual.getPlannerContext(DefaultDatabase.LOGIC_NAME.toLowerCase()), instanceOf(OptimizerPlannerContext.class));
    }
    
    private static ShardingSphereDatabase createShardingSphereDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getProtocolType().getType()).thenReturn("MySQL");
        return result;
    }
    
    private static ShardingSphereRuleMetaData createShardingSphereRuleMetaData() {
        ShardingSphereRuleMetaData result = mock(ShardingSphereRuleMetaData.class);
        CacheOption cacheOption = new CacheOption(10, 1000);
        when(result.getSingleRule(SQLParserRule.class)).thenReturn(new SQLParserRule(new SQLParserRuleConfiguration(true, cacheOption, cacheOption)));
        return result;
    }
}
