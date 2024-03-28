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

import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sqlfederation.optimizer.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.sqlfederation.optimizer.context.planner.OptimizerMetaData;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OptimizerContextTest {
    
    @Test
    void assertGetSqlParserRule() {
        OptimizerContext actual = OptimizerContextFactory.create(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, createShardingSphereDatabase()));
        assertThat(actual.getSqlParserRule(), instanceOf(SQLParserRule.class));
    }
    
    @Test
    void assertGetParserContext() {
        OptimizerContext actual = OptimizerContextFactory.create(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, createShardingSphereDatabase()));
        assertThat(actual.getParserContext(DefaultDatabase.LOGIC_NAME), instanceOf(OptimizerParserContext.class));
    }
    
    @Test
    void assertGetOptimizerMetaData() {
        OptimizerContext actual = OptimizerContextFactory.create(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, createShardingSphereDatabase()));
        assertThat(actual.getMetaData(DefaultDatabase.LOGIC_NAME), instanceOf(OptimizerMetaData.class));
    }
    
    private ShardingSphereDatabase createShardingSphereDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        return result;
    }
}
