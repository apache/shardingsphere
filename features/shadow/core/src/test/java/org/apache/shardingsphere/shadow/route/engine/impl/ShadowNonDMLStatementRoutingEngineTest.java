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

package org.apache.shardingsphere.shadow.route.engine.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.factory.ShadowAlgorithmFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowNonDMLStatementRoutingEngineTest {
    
    private ShadowNonDMLStatementRoutingEngine shadowRouteEngine;
    
    @Before
    public void init() {
        shadowRouteEngine = new ShadowNonDMLStatementRoutingEngine(createSQLStatementContext());
    }
    
    private SQLStatementContext<?> createSQLStatementContext() {
        CreateTableStatementContext result = mock(CreateTableStatementContext.class);
        MySQLCreateTableStatement sqlStatement = new MySQLCreateTableStatement(false);
        sqlStatement.getCommentSegments().add(new CommentSegment("/* SHARDINGSPHERE_HINT: SHADOW=true */", 0, 20));
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        return result;
    }
    
    @Test
    public void assertRoute() {
        RouteContext routeContext = createRouteContext();
        shadowRouteEngine.route(routeContext, new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration()));
        Collection<RouteUnit> routeUnits = routeContext.getRouteUnits();
        RouteMapper dataSourceMapper = routeUnits.iterator().next().getDataSourceMapper();
        assertThat(dataSourceMapper.getLogicName(), is("logic_db"));
        assertThat(dataSourceMapper.getActualName(), is("ds_shadow"));
    }
    
    private RouteContext createRouteContext() {
        RouteContext result = new RouteContext();
        Collection<RouteUnit> routeUnits = result.getRouteUnits();
        routeUnits.add(createRouteUnit());
        return result;
    }
    
    private RouteUnit createRouteUnit() {
        return new RouteUnit(new RouteMapper("logic_db", "shadow-data-source"), Collections.singleton(new RouteMapper("t_order", "t_order")));
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setDataSources(Collections.singletonMap("shadow-data-source", new ShadowDataSourceConfiguration("ds", "ds_shadow")));
        result.setTables(Collections.singletonMap("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source"), Collections.singleton("simple-hint-algorithm"))));
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }
    
    private Map<String, ShadowAlgorithm> createShadowAlgorithms() {
        return Collections.singletonMap("simple-hint-algorithm", ShadowAlgorithmFactory.newInstance(new AlgorithmConfiguration("SIMPLE_HINT", createProperties())));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("shadow", Boolean.TRUE.toString());
        return result;
    }
}
