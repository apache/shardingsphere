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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.TargetAdviceObjectFixture;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RouteResultCountAdviceTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final MetricConfiguration routedStorageUnitResultConfig = new MetricConfiguration("routed_storage_unit_total", MetricCollectorType.COUNTER, null, Arrays.asList("database", "name"));
    
    private final MetricConfiguration routedTableResultConfig = new MetricConfiguration("routed_table_total", MetricCollectorType.COUNTER, null, Arrays.asList("database", "name"));
    
    @AfterEach
    void reset() {
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(routedStorageUnitResultConfig, "FIXTURE")).reset();
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(routedTableResultConfig, "FIXTURE")).reset();
    }
    
    @Test
    void assertCountRouteResult() {
        RouteContext routeContext = new RouteContext();
        RouteMapper dataSourceMapper = new RouteMapper("logic_db", "ds_0");
        RouteMapper tableMapper = new RouteMapper("t_order", "t_order_0");
        routeContext.getRouteUnits().add(new RouteUnit(dataSourceMapper, Collections.singleton(tableMapper)));
        new RouteResultCountAdvice().afterMethod(new TargetAdviceObjectFixture(), mock(TargetAdviceMethod.class), new Object[]{createQueryContext(),
                new ConnectionContext(Collections::emptySet), mockDatabase()}, routeContext, "FIXTURE");
        assertThat(MetricsCollectorRegistry.get(routedStorageUnitResultConfig, "FIXTURE").toString(), is("foo_db.ds_0=1"));
        assertThat(MetricsCollectorRegistry.get(routedTableResultConfig, "FIXTURE").toString(), is("foo_db.t_order=1"));
    }
    
    private QueryContext createQueryContext() {
        return new QueryContext(new CommonSQLStatementContext(SelectStatement.builder().databaseType(DATABASE_TYPE).build(), createTablesContext()), "",
                Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
    }
    
    private TablesContext createTablesContext() {
        return new TablesContext(Collections.singleton(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn("foo_db");
        return result;
    }
}
