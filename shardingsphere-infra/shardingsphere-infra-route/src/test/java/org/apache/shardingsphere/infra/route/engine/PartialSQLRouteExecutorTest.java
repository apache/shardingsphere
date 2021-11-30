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

package org.apache.shardingsphere.infra.route.engine;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.impl.PartialSQLRouteExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PartialSQLRouteExecutorTest {
    
    private final PartialSQLRouteExecutor partialSQLRouteExecutor = new PartialSQLRouteExecutor(Collections.emptyList(), new ConfigurationProperties(new Properties()));
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private CommonSQLStatementContext<AbstractSQLStatement> commonSQLStatementContext;
    
    @Before
    public void setup() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", null);
        dataSourceMap.put("ds_1", null);
        when(shardingSphereMetaData.getResource().getDataSources()).thenReturn(dataSourceMap);
    }
    
    @Test
    public void assertRouteBySQLCommentHint() {
        when(commonSQLStatementContext.findHintDataSourceName()).thenReturn(Optional.of("ds_1"));
        LogicSQL logicSQL = new LogicSQL(commonSQLStatementContext, "", Collections.emptyList());
        RouteContext routeContext = partialSQLRouteExecutor.route(logicSQL, shardingSphereMetaData);
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_1"));
    }
    
    @Test
    public void assertRouteByHintManagerHint() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_1");
            LogicSQL logicSQL = new LogicSQL(commonSQLStatementContext, "", Collections.emptyList());
            RouteContext routeContext = partialSQLRouteExecutor.route(logicSQL, shardingSphereMetaData);
            assertThat(routeContext.getRouteUnits().size(), is(1));
            assertThat(routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_1"));
        }
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertRouteBySQLCommentHintWithException() {
        when(commonSQLStatementContext.findHintDataSourceName()).thenReturn(Optional.of("ds_3"));
        LogicSQL logicSQL = new LogicSQL(commonSQLStatementContext, "", Collections.emptyList());
        partialSQLRouteExecutor.route(logicSQL, shardingSphereMetaData);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertRouteByHintManagerHintWithException() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds-3");
            LogicSQL logicSQL = new LogicSQL(commonSQLStatementContext, "", Collections.emptyList());
            partialSQLRouteExecutor.route(logicSQL, shardingSphereMetaData);
        }
    }
}
