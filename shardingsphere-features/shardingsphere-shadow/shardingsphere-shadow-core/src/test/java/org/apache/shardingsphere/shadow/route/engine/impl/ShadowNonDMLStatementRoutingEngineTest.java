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

import java.util.Collections;
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
        MySQLCreateTableStatement sqlStatement = new MySQLCreateTableStatement();
        sqlStatement.getCommentSegments().add(new CommentSegment("/*shadow:true*/", 0, 20));
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        return result;
    }
    
    @Test
    public void assertRoute() {
        shadowRouteEngine.route(createRouteContext(), new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration()));
        // TODO finish assert
    }
    
    private RouteContext createRouteContext() {
        RouteContext result = mock(RouteContext.class);
        when(result.getRouteUnits()).thenReturn(
                Collections.singleton(new RouteUnit(new RouteMapper("ds", "ds"), Collections.singleton(new RouteMapper("t_order", "t_order")))));
        return result;
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setDataSources(Collections.singletonMap("shadow-data-source", new ShadowDataSourceConfiguration("ds", "ds_shadow")));
        result.setTables(Collections.singletonMap("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source"), Collections.singleton("simple-hint-algorithm"))));
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }
    
    private Map<String, ShadowAlgorithm> createShadowAlgorithms() {
        SimpleHintShadowAlgorithm simpleHintShadowAlgorithm = new SimpleHintShadowAlgorithm();
        Properties props = new Properties();
        props.setProperty("shadow", "true");
        simpleHintShadowAlgorithm.setProps(props);
        simpleHintShadowAlgorithm.init();
        return Collections.singletonMap("simple-hint-algorithm", simpleHintShadowAlgorithm);
    }
}
