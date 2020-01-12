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

package org.apache.shardingsphere.core.shard;

import lombok.SneakyThrows;
import org.apache.shardingsphere.sharding.route.engine.ShardingRouter;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SimpleQueryShardingEngineTest extends BaseShardingEngineTest {
    
    @Mock
    private ShardingRouter shardingRouter;
    
    private SimpleQueryShardingEngine shardingEngine;
    
    public SimpleQueryShardingEngineTest() {
        super("SELECT 1", Collections.emptyList());
    }
    
    @Before
    public void setUp() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        EncryptRule encryptRule = mock(EncryptRule.class);
        when(shardingRule.getEncryptRule()).thenReturn(encryptRule);
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class);
        when(shardingSphereMetaData.getTables()).thenReturn(mock(TableMetas.class));
        shardingEngine = new SimpleQueryShardingEngine(shardingRule, getProperties(), shardingSphereMetaData, mock(SQLParseEngine.class));
        setRoutingEngine();
    }
    
    @SneakyThrows
    private void setRoutingEngine() {
        Field field = BaseShardingEngine.class.getDeclaredField("shardingRouter");
        field.setAccessible(true);
        field.set(shardingEngine, shardingRouter);
    }
    
    protected void assertShard() {
        when(shardingRouter.route(getSql(), Collections.emptyList(), false)).thenReturn(createSQLRouteContext());
        assertExecutionContext(shardingEngine.shard(getSql(), getParameters()));
    }
}
