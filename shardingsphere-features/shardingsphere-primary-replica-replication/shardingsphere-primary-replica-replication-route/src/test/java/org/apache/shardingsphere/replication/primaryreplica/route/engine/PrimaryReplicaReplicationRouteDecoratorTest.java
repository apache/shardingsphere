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

package org.apache.shardingsphere.replication.primaryreplica.route.engine;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.decorator.RouteDecorator;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.replication.primaryreplica.api.config.PrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.api.config.rule.PrimaryReplicaReplicationDataSourceRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.route.engine.impl.PrimaryVisitedManager;
import org.apache.shardingsphere.replication.primaryreplica.rule.PrimaryReplicaReplicationRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PrimaryReplicaReplicationRouteDecoratorTest {
    
    private static final String DATASOURCE_NAME = "ds";
    
    private static final String NON_PRIMARY_REPLICA_DATASOURCE_NAME = "nonPrDatasource";
    
    private static final String PRIMARY_DATASOURCE = "primary";
    
    private static final String REPLICA_DATASOURCE = "query";
    
    private PrimaryReplicaReplicationRule rule;
    
    @Mock
    private SQLStatementContext<SQLStatement> sqlStatementContext;
    
    @Mock
    private InsertStatement insertStatement;
    
    @Mock
    private MySQLSelectStatement selectStatement;
    
    private PrimaryReplicaReplicationRouteDecorator routeDecorator;

    static {
        ShardingSphereServiceLoader.register(RouteDecorator.class);
    }
    
    @Before
    public void setUp() {
        rule = new PrimaryReplicaReplicationRule(new PrimaryReplicaReplicationRuleConfiguration(Collections.singleton(
                new PrimaryReplicaReplicationDataSourceRuleConfiguration(DATASOURCE_NAME, PRIMARY_DATASOURCE, Collections.singletonList(REPLICA_DATASOURCE), null)), Collections.emptyMap()));
        routeDecorator = (PrimaryReplicaReplicationRouteDecorator) OrderedSPIRegistry.getRegisteredServices(Collections.singleton(rule), RouteDecorator.class).get(rule);
    }
    
    @After
    public void tearDown() {
        PrimaryVisitedManager.clear();
    }
    
    @Test
    public void assertDecorateToPrimary() {
        RouteContext actual = mockSQLRouteContext(insertStatement);
        routeDecorator.decorate(actual, mock(SQLStatementContext.class), Collections.emptyList(), mock(ShardingSphereMetaData.class), rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NON_PRIMARY_REPLICA_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertDecorateToPrimaryWithoutRouteUnits() {
        RouteContext actual = mockSQLRouteContextWithoutRouteUnits(insertStatement);
        routeDecorator.decorate(actual, mock(SQLStatementContext.class), Collections.emptyList(), mock(ShardingSphereMetaData.class), rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertDecorateToReplica() {
        RouteContext actual = mockSQLRouteContext(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.empty());
        routeDecorator.decorate(actual, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class), rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NON_PRIMARY_REPLICA_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(REPLICA_DATASOURCE));
    }
    
    @Test
    public void assertDecorateToReplicaWithoutRouteUnits() {
        RouteContext actual = mockSQLRouteContextWithoutRouteUnits(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.empty());
        routeDecorator.decorate(actual, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class), rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(REPLICA_DATASOURCE));
    }
    
    @Test
    public void assertLockDecorateToPrimary() {
        RouteContext actual = mockSQLRouteContext(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.of(mock(LockSegment.class)));
        routeDecorator.decorate(actual, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class), rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NON_PRIMARY_REPLICA_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertLockDecorateToPrimaryWithoutRouteUnits() {
        RouteContext actual = mockSQLRouteContextWithoutRouteUnits(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.of(mock(LockSegment.class)));
        routeDecorator.decorate(actual, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class), rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertDecorateToPrimaryWithoutRouteUnitsAndWithParameters() {
        RouteContext actual = mockSQLRouteContextWithoutRouteUnitsAndWithParameters(insertStatement);
        routeDecorator.decorate(actual, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereMetaData.class), rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    private RouteContext mockSQLRouteContext(final SQLStatement sqlStatement) {
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        RouteContext result = new RouteContext(sqlStatementContext, Collections.emptyList());
        mockRouteResult(result.getRouteResult());
        result.addNextRouteStageContext(null, null);
        return result;
    }
    
    private void mockRouteResult(final RouteResult routeResult) {
        RouteUnit routeUnit = new RouteUnit(new RouteMapper(DATASOURCE_NAME, DATASOURCE_NAME), Collections.singletonList(new RouteMapper("table", "table_0")));
        routeResult.getRouteUnits().add(routeUnit);
        routeResult.getRouteUnits().add(new RouteUnit(new RouteMapper(NON_PRIMARY_REPLICA_DATASOURCE_NAME, NON_PRIMARY_REPLICA_DATASOURCE_NAME), Collections.emptyList()));
    }
    
    private RouteContext mockSQLRouteContextWithoutRouteUnits(final SQLStatement sqlStatement) {
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        return new RouteContext(sqlStatementContext, Collections.emptyList());
    }
    
    private RouteContext mockSQLRouteContextWithoutRouteUnitsAndWithParameters(final SQLStatement sqlStatement) {
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        return new RouteContext(sqlStatementContext, Collections.singletonList("true"));
    }
}
