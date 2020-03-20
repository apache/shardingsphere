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
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.engine.DateNodeRouter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PreparedQueryShardingEngineTest extends BaseShardingEngineTest {
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private DateNodeRouter dateNodeRouter;
    
    private PreparedQueryShardingEngine shardingEngine;
    
    public PreparedQueryShardingEngineTest() {
        super("SELECT ?", Collections.singletonList(1));
    }
    
    @Before
    public void setUp() {
        when(metaData.getSchema()).thenReturn(mock(SchemaMetaData.class));
        EncryptRule encryptRule = mock(EncryptRule.class);
        when(shardingRule.getEncryptRule()).thenReturn(encryptRule);
        shardingEngine = new PreparedQueryShardingEngine(shardingRule, getProperties(), metaData, mock(SQLParserEngine.class));
        setRoutingEngine();
    }
    
    @SneakyThrows
    private void setRoutingEngine() {
        Field field = BaseShardingEngine.class.getDeclaredField("dateNodeRouter");
        field.setAccessible(true);
        field.set(shardingEngine, dateNodeRouter);
    }
    
    protected void assertShard() {
        RouteContext routeContext = createSQLRouteContext();
        when(dateNodeRouter.route(getSql(), getParameters(), true)).thenReturn(routeContext);
        assertExecutionContext(shardingEngine.shard(getSql(), getParameters()));
    }
    
    @Test(expected = SQLException.class)
    public void assertWithRouteException() {
        when(dateNodeRouter.route(getSql(), getParameters(), true)).thenThrow(SQLException.class);
        shardingEngine.shard(getSql(), getParameters());
    }
}
