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

package org.apache.shardingsphere.readwritesplitting.route;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadwriteSplittingSQLRouterTest {
    
    private static final String DATASOURCE_NAME = "ds";
    
    private static final String NONE_READWRITE_SPLITTING_DATASOURCE_NAME = "noneReadwriteSplittingDataSource";
    
    private static final String WRITE_DATASOURCE = "write";
    
    private static final String READ_DATASOURCE = "read";
    
    private ReadwriteSplittingRule staticRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CommonSQLStatementContext sqlStatementContext;
    
    private ReadwriteSplittingSQLRouter sqlRouter;
    
    @BeforeEach
    void setUp() {
        staticRule = new ReadwriteSplittingRule("logic_db", new ReadwriteSplittingRuleConfiguration(Collections.singleton(new ReadwriteSplittingDataSourceGroupRuleConfiguration(DATASOURCE_NAME,
                WRITE_DATASOURCE, Collections.singletonList(READ_DATASOURCE), "")), Collections.emptyMap()), mock(ComputeNodeInstanceContext.class));
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        sqlRouter = (ReadwriteSplittingSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(staticRule)).get(staticRule);
    }
    
    @Test
    void assertDecorateRouteContextToPrimaryDataSource() {
        RouteContext actual = mockRouteContext();
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                mock(DatabaseType.class), mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyList());
        sqlRouter.decorateRouteContext(actual, queryContext, database, staticRule, Collections.emptyList(), new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_READWRITE_SPLITTING_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(WRITE_DATASOURCE));
    }
    
    @Test
    void assertDecorateRouteContextToReplicaDataSource() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.empty());
        ConnectionContext connectionContext = mockConnectionContext();
        when(connectionContext.getTransactionContext()).thenReturn(mock(TransactionConnectionContext.class));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), connectionContext, mock(ShardingSphereMetaData.class));
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyList());
        RouteContext actual = mockRouteContext();
        sqlRouter.decorateRouteContext(actual, queryContext, database, staticRule, Collections.emptyList(), new ConfigurationProperties(new Properties()));
        assertThat(actual.getActualDataSourceNames(), is(new HashSet<>(Arrays.asList(NONE_READWRITE_SPLITTING_DATASOURCE_NAME, READ_DATASOURCE))));
    }
    
    @Test
    void assertDecorateRouteContextToPrimaryDataSourceWithLock() {
        RouteContext actual = mockRouteContext();
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.of(mock(LockSegment.class)));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyList());
        sqlRouter.decorateRouteContext(actual, queryContext, database, staticRule, Collections.emptyList(), new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_READWRITE_SPLITTING_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(WRITE_DATASOURCE));
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    private RouteContext mockRouteContext() {
        RouteContext result = new RouteContext();
        RouteUnit routeUnit = new RouteUnit(new RouteMapper(DATASOURCE_NAME, DATASOURCE_NAME), Collections.singleton(new RouteMapper("table", "table_0")));
        result.getRouteUnits().add(routeUnit);
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(NONE_READWRITE_SPLITTING_DATASOURCE_NAME, NONE_READWRITE_SPLITTING_DATASOURCE_NAME), Collections.emptyList()));
        return result;
    }
}
