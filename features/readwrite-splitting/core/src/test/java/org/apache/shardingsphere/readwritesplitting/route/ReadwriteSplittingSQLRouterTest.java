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

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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
    
    @Mock
    private CommonSQLStatementContext sqlStatementContext;
    
    private ReadwriteSplittingSQLRouter sqlRouter;
    
    @BeforeEach
    void setUp() {
        staticRule = new ReadwriteSplittingRule("logic_db", new ReadwriteSplittingRuleConfiguration(Collections.singleton(new ReadwriteSplittingDataSourceRuleConfiguration(DATASOURCE_NAME,
                WRITE_DATASOURCE, Collections.singletonList(READ_DATASOURCE), "")), Collections.emptyMap()), mock(InstanceContext.class));
        sqlRouter = (ReadwriteSplittingSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(staticRule)).get(staticRule);
    }
    
    @Test
    void assertCreateRouteContextToPrimaryWithoutRouteUnits() {
        QueryContext queryContext = new QueryContext(mock(SQLStatementContext.class), "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        RouteContext actual = sqlRouter.createRouteContext(queryContext,
                mock(ShardingSphereRuleMetaData.class), database, staticRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is(DATASOURCE_NAME));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is(WRITE_DATASOURCE));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(WRITE_DATASOURCE));
    }
    
    @Test
    void assertDecorateRouteContextToPrimaryDataSource() {
        RouteContext actual = mockRouteContext();
        QueryContext queryContext = new QueryContext(mock(SQLStatementContext.class), "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        sqlRouter.decorateRouteContext(actual, queryContext, database, staticRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_READWRITE_SPLITTING_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(WRITE_DATASOURCE));
    }
    
    @Test
    void assertCreateRouteContextToReplicaDataSource() {
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.empty());
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        RouteContext actual = sqlRouter.createRouteContext(queryContext,
                mock(ShardingSphereRuleMetaData.class), database, staticRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is(DATASOURCE_NAME));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is(READ_DATASOURCE));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(READ_DATASOURCE));
    }
    
    @Test
    void assertDecorateRouteContextToReplicaDataSource() {
        RouteContext actual = mockRouteContext();
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.empty());
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        sqlRouter.decorateRouteContext(actual, queryContext, database, staticRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_READWRITE_SPLITTING_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(READ_DATASOURCE));
    }
    
    @Test
    void assertCreateRouteContextToPrimaryDataSourceWithLock() {
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.of(mock(LockSegment.class)));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        RouteContext actual = sqlRouter.createRouteContext(queryContext,
                mock(ShardingSphereRuleMetaData.class), database, staticRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(WRITE_DATASOURCE));
    }
    
    @Test
    void assertDecorateRouteContextToPrimaryDataSourceWithLock() {
        RouteContext actual = mockRouteContext();
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.of(mock(LockSegment.class)));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        sqlRouter.decorateRouteContext(actual, queryContext, database, staticRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_READWRITE_SPLITTING_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(WRITE_DATASOURCE));
    }
    
    @Test
    void assertCreateRouteContextToPrimaryDataSource() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(InsertStatement.class));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        RouteContext actual = sqlRouter.createRouteContext(queryContext,
                mock(ShardingSphereRuleMetaData.class), database, staticRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(WRITE_DATASOURCE));
    }
    
    @Test
    void assertCreateRouteContextToReadDataSource() {
        MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(insertStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        RouteContext actual = sqlRouter.createRouteContext(queryContext,
                mock(ShardingSphereRuleMetaData.class), database, staticRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(WRITE_DATASOURCE));
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.empty());
        queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList());
        actual = sqlRouter.createRouteContext(queryContext,
                mock(ShardingSphereRuleMetaData.class), database, staticRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(READ_DATASOURCE));
    }
    
    @Test
    void assertSqlHintRouteWriteOnly() {
        SelectStatement statement = mock(SelectStatement.class);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(statement);
        HintValueContext hintValueContext = mock(HintValueContext.class);
        when(hintValueContext.isWriteRouteOnly()).thenReturn(true);
        when(sqlStatementContext.getProjectionsContext().isContainsLastInsertIdProjection()).thenReturn(false);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), hintValueContext);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(staticRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME,
                mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS), ruleMetaData, Collections.emptyMap());
        RouteContext actual = sqlRouter.createRouteContext(queryContext,
                mock(ShardingSphereRuleMetaData.class), database, staticRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(WRITE_DATASOURCE));
    }
    
    private RouteContext mockRouteContext() {
        RouteContext result = new RouteContext();
        RouteUnit routeUnit = new RouteUnit(new RouteMapper(DATASOURCE_NAME, DATASOURCE_NAME), Collections.singletonList(new RouteMapper("table", "table_0")));
        result.getRouteUnits().add(routeUnit);
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(NONE_READWRITE_SPLITTING_DATASOURCE_NAME, NONE_READWRITE_SPLITTING_DATASOURCE_NAME), Collections.emptyList()));
        return result;
    }
}
