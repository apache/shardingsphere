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

package org.apache.shardingsphere.core;

import lombok.SneakyThrows;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.parse.cache.ParsingResultCache;
import org.apache.shardingsphere.core.parse.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.route.RouteUnit;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.StatementRoutingEngine;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SimpleQueryShardingEngineTest {
    
    @Mock
    private StatementRoutingEngine routingEngine;
    
    private SimpleQueryShardingEngine shardingEngine;
    
    @Before
    public void setUp() {
        shardingEngine = new SimpleQueryShardingEngine(mock(ShardingRule.class), getShardingProperties(), mock(ShardingMetaData.class), DatabaseType.MySQL, new ParsingResultCache());
        setRoutingEngine();
    }
    
    private ShardingProperties getShardingProperties() {
        Properties result = new Properties();
        result.setProperty(ShardingPropertiesConstant.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        return new ShardingProperties(result);
    }
    
    @SneakyThrows
    private void setRoutingEngine() {
        Field field = SimpleQueryShardingEngine.class.getDeclaredField("routingEngine");
        field.setAccessible(true);
        field.set(shardingEngine, routingEngine);
    }
    
    @Test
    public void assertShardWithHintDatabaseShardingOnly() {
        HintManager.getInstance().setDatabaseShardingValue("1");
        when(routingEngine.route("SELECT 1")).thenReturn(createSQLRouteResult());
        assertSQLRouteResult(shardingEngine.shard("SELECT 1", Collections.emptyList()));
        HintManager.clear();
    }
    
    @Test
    public void assertShardWithoutHint() {
        when(routingEngine.route("SELECT 1")).thenReturn(createSQLRouteResult());
        assertSQLRouteResult(shardingEngine.shard("SELECT 1", Collections.emptyList()));
    }
    
    private SQLRouteResult createSQLRouteResult() {
        SQLRouteResult result = new SQLRouteResult(new SelectStatement());
        RoutingResult routingResult = new RoutingResult();
        routingResult.getTableUnits().getTableUnits().add(new TableUnit("ds"));
        result.setRoutingResult(routingResult);
        return result;
    }
    
    private void assertSQLRouteResult(final SQLRouteResult actual) {
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit actualRouteUnit = actual.getRouteUnits().iterator().next();
        assertThat(actualRouteUnit.getDataSourceName(), is("ds"));
        assertThat(actualRouteUnit.getSqlUnit().getSql(), is("SELECT 1"));
        assertThat(actualRouteUnit.getSqlUnit().getParameters(), is(Collections.emptyList()));
    }
}
