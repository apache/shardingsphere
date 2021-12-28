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
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.shadow.hint.SimpleHintShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

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
        MySQLCreateTableStatement mySQLCreateTableStatement = new MySQLCreateTableStatement();
        Collection<CommentSegment> commentSegments = new LinkedList<>();
        commentSegments.add(new CommentSegment("/*shadow:true*/", 0, 20));
        mySQLCreateTableStatement.setCommentSegments(commentSegments);
        when(result.getSqlStatement()).thenReturn(mySQLCreateTableStatement);
        return result;
    }
    
    @Test
    public void assertRoute() {
        shadowRouteEngine.route(createRouteContext(), new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration()));
    }
    
    private RouteContext createRouteContext() {
        RouteContext result = mock(RouteContext.class);
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        Collection<RouteMapper> tableRouteMappers = new LinkedList<>();
        tableRouteMappers.add(new RouteMapper("t_order", "t_order"));
        routeUnits.add(new RouteUnit(new RouteMapper("ds", "ds"), tableRouteMappers));
        when(result.getRouteUnits()).thenReturn(routeUnits);
        return result;
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setDataSources(createDataSources());
        result.setTables(createTables());
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }
    
    private Map<String, ShadowDataSourceConfiguration> createDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("shadow-data-source", new ShadowDataSourceConfiguration("ds", "ds_shadow"));
        return result;
    }
    
    private Map<String, ShadowTableConfiguration> createTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        Collection<String> shadowAlgorithmNames = new LinkedList<>();
        shadowAlgorithmNames.add("simple-hint-algorithm");
        result.put("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source"), shadowAlgorithmNames));
        return result;
    }
    
    private Map<String, ShadowAlgorithm> createShadowAlgorithms() {
        final Map<String, ShadowAlgorithm> result = new LinkedHashMap<>();
        SimpleHintShadowAlgorithm simpleHintShadowAlgorithm = new SimpleHintShadowAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("shadow", "true");
        simpleHintShadowAlgorithm.setProps(properties);
        simpleHintShadowAlgorithm.init();
        result.put("simple-hint-algorithm", simpleHintShadowAlgorithm);
        return result;
    }
}
