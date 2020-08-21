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

package org.apache.shardingsphere.masterslave.route.engine;

import org.apache.shardingsphere.infra.route.decorator.RouteDecorator;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveRule;
import org.apache.shardingsphere.masterslave.route.engine.impl.MasterVisitedManager;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
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
public final class MasterSlaveRouteDecoratorTest {
    
    private static final String DATASOURCE_NAME = "ds";
    
    private static final String NON_MASTER_SLAVE_DATASOURCE_NAME = "nonMsDatasource";
    
    private static final String MASTER_DATASOURCE = "master";
    
    private static final String SLAVE_DATASOURCE = "query";
    
    private MasterSlaveRule masterSlaveRule;
    
    @Mock
    private SQLStatementContext<SQLStatement> sqlStatementContext;
    
    @Mock
    private InsertStatement insertStatement;
    
    @Mock
    private SelectStatement selectStatement;
    
    private MasterSlaveRouteDecorator routeDecorator;

    static {
        ShardingSphereServiceLoader.register(RouteDecorator.class);
    }
    
    @Before
    public void setUp() {
        masterSlaveRule = new MasterSlaveRule(new MasterSlaveRuleConfiguration(
                Collections.singleton(new MasterSlaveDataSourceRuleConfiguration(DATASOURCE_NAME, MASTER_DATASOURCE, Collections.singletonList(SLAVE_DATASOURCE), null)), Collections.emptyMap()));
        routeDecorator = (MasterSlaveRouteDecorator) OrderedSPIRegistry.getRegisteredServices(Collections.singleton(masterSlaveRule), RouteDecorator.class).get(masterSlaveRule);
    }
    
    @After
    public void tearDown() {
        MasterVisitedManager.clear();
    }
    
    @Test
    public void assertDecorateToMaster() {
        RouteContext routeContext = mockSQLRouteContext(insertStatement);
        RouteContext actual = routeDecorator.decorate(routeContext, mock(ShardingSphereMetaData.class), masterSlaveRule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NON_MASTER_SLAVE_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(MASTER_DATASOURCE));
    }
    
    @Test
    public void assertDecorateToMasterWithoutRouteUnits() {
        RouteContext routeContext = mockSQLRouteContextWithoutRouteUnits(insertStatement);
        RouteContext actual = routeDecorator.decorate(routeContext, mock(ShardingSphereMetaData.class), masterSlaveRule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(MASTER_DATASOURCE));
    }
    
    @Test
    public void assertDecorateToSlave() {
        RouteContext routeContext = mockSQLRouteContext(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.empty());
        RouteContext actual = routeDecorator.decorate(routeContext, mock(ShardingSphereMetaData.class), masterSlaveRule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NON_MASTER_SLAVE_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(SLAVE_DATASOURCE));
    }
    
    @Test
    public void assertDecorateToSlaveWithoutRouteUnits() {
        RouteContext routeContext = mockSQLRouteContextWithoutRouteUnits(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.empty());
        RouteContext actual = routeDecorator.decorate(routeContext, mock(ShardingSphereMetaData.class), masterSlaveRule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(SLAVE_DATASOURCE));
    }
    
    @Test
    public void assertLockDecorateToMaster() {
        RouteContext routeContext = mockSQLRouteContext(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.of(mock(LockSegment.class)));
        RouteContext actual = routeDecorator.decorate(routeContext, mock(ShardingSphereMetaData.class), masterSlaveRule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NON_MASTER_SLAVE_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(MASTER_DATASOURCE));
    }
    
    @Test
    public void assertLockDecorateToMasterWithoutRouteUnits() {
        RouteContext routeContext = mockSQLRouteContextWithoutRouteUnits(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.of(mock(LockSegment.class)));
        RouteContext actual = routeDecorator.decorate(routeContext, mock(ShardingSphereMetaData.class), masterSlaveRule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(MASTER_DATASOURCE));
    }
    
    @Test
    public void assertDecorateToMasterWithoutRouteUnitsAndWithParameters() {
        RouteContext routeContext = mockSQLRouteContextWithoutRouteUnitsAndWithParameters(insertStatement);
        RouteContext actual = routeDecorator.decorate(routeContext, mock(ShardingSphereMetaData.class), masterSlaveRule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getRouteResult().getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(MASTER_DATASOURCE));
        assertThat(actual.getParameters().get(0), is("true"));
    }
    
    private RouteContext mockSQLRouteContext(final SQLStatement sqlStatement) {
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        return new RouteContext(sqlStatementContext, Collections.emptyList(), mockRouteResult());
    }
    
    private RouteResult mockRouteResult() {
        RouteResult result = new RouteResult();
        RouteUnit routeUnit = new RouteUnit(new RouteMapper(DATASOURCE_NAME, DATASOURCE_NAME), Collections.singletonList(new RouteMapper("table", "table_0")));
        result.getRouteUnits().add(routeUnit);
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(NON_MASTER_SLAVE_DATASOURCE_NAME, NON_MASTER_SLAVE_DATASOURCE_NAME), Collections.emptyList()));
        return result;
    }
    
    private RouteContext mockSQLRouteContextWithoutRouteUnits(final SQLStatement sqlStatement) {
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        return new RouteContext(sqlStatementContext, Collections.emptyList(), new RouteResult());
    }
    
    private RouteContext mockSQLRouteContextWithoutRouteUnitsAndWithParameters(final SQLStatement sqlStatement) {
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        return new RouteContext(sqlStatementContext, Collections.singletonList("true"), new RouteResult());
    }
}
