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

package org.apache.shardingsphere.infra.route.engine.impl;

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintDataSourceNotExistsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartialSQLRouteExecutorTest {
    
    private final PartialSQLRouteExecutor partialSQLRouteExecutor = new PartialSQLRouteExecutor(Collections.emptyList(), new ConfigurationProperties(new Properties()));
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private CommonSQLStatementContext commonSQLStatementContext;
    
    @Mock
    private HintValueContext hintValueContext;
    
    private final ConnectionContext connectionContext = new ConnectionContext();
    
    @BeforeEach
    void setup() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", null);
        dataSourceMap.put("ds_1", null);
        when(database.getResourceMetaData().getDataSources()).thenReturn(dataSourceMap);
    }
    
    @Test
    void assertRouteBySQLCommentHint() {
        when(hintValueContext.findHintDataSourceName()).thenReturn(Optional.of("ds_1"));
        QueryContext queryContext = new QueryContext(commonSQLStatementContext, "", Collections.emptyList(), hintValueContext);
        RouteContext routeContext = partialSQLRouteExecutor.route(connectionContext, queryContext, mock(RuleMetaData.class), database);
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_1"));
    }
    
    @Test
    void assertRouteByHintManagerHint() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_1");
            QueryContext queryContext = new QueryContext(commonSQLStatementContext, "", Collections.emptyList());
            RouteContext routeContext = partialSQLRouteExecutor.route(connectionContext, queryContext, mock(RuleMetaData.class), database);
            assertThat(routeContext.getRouteUnits().size(), is(1));
            assertThat(routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_1"));
        }
    }
    
    @Test
    void assertRouteBySQLCommentHintWithException() {
        when(hintValueContext.findHintDataSourceName()).thenReturn(Optional.of("ds_3"));
        QueryContext queryContext = new QueryContext(commonSQLStatementContext, "", Collections.emptyList(), hintValueContext);
        assertThrows(SQLHintDataSourceNotExistsException.class, () -> partialSQLRouteExecutor.route(connectionContext, queryContext, mock(RuleMetaData.class), database));
    }
    
    @Test
    void assertRouteByHintManagerHintWithException() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds-3");
            QueryContext logicSQL = new QueryContext(commonSQLStatementContext, "", Collections.emptyList());
            assertThrows(SQLHintDataSourceNotExistsException.class, () -> partialSQLRouteExecutor.route(connectionContext, logicSQL, mock(RuleMetaData.class), database));
        }
    }
}
