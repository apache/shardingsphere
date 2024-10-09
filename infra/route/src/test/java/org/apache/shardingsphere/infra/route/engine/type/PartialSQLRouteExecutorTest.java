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

package org.apache.shardingsphere.infra.route.engine.type;

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.exception.kernel.syntax.hint.DataSourceHintNotExistsException;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
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
    
    private final PartialSQLRouteExecutor sqlRouteExecutor = new PartialSQLRouteExecutor(Collections.emptyList(), new ConfigurationProperties(new Properties()));
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock
    private CommonSQLStatementContext sqlStatementContext;
    
    @Mock
    private HintValueContext hintValueContext;
    
    private final ConnectionContext connectionContext = new ConnectionContext(Collections::emptySet);
    
    @BeforeEach
    void setup() {
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        storageUnits.put("ds_0", mock(StorageUnit.class));
        storageUnits.put("ds_1", mock(StorageUnit.class));
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        connectionContext.setCurrentDatabaseName(DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    void assertRouteBySQLCommentHint() {
        when(hintValueContext.findHintDataSourceName()).thenReturn(Optional.of("ds_1"));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), hintValueContext, connectionContext, metaData);
        RouteContext routeContext = sqlRouteExecutor.route(queryContext, mock(RuleMetaData.class), database);
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_1"));
    }
    
    @Test
    void assertRouteByHintManagerHint() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_1");
            QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
            RouteContext routeContext = sqlRouteExecutor.route(queryContext, mock(RuleMetaData.class), database);
            assertThat(routeContext.getRouteUnits().size(), is(1));
            assertThat(routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_1"));
        }
    }
    
    @Test
    void assertRouteBySQLCommentHintWithException() {
        when(hintValueContext.findHintDataSourceName()).thenReturn(Optional.of("ds_3"));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), hintValueContext, connectionContext, metaData);
        assertThrows(DataSourceHintNotExistsException.class, () -> sqlRouteExecutor.route(queryContext, mock(RuleMetaData.class), database));
    }
    
    @Test
    void assertRouteByHintManagerHintWithException() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("ds_3");
            QueryContext logicSQL = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
            assertThrows(DataSourceHintNotExistsException.class, () -> sqlRouteExecutor.route(logicSQL, mock(RuleMetaData.class), database));
        }
    }
}
