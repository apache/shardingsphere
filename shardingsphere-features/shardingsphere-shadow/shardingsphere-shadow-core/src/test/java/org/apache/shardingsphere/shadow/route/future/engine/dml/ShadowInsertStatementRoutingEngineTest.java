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

package org.apache.shardingsphere.shadow.route.future.engine.dml;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.algorithm.shadow.note.SimpleSQLNoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.route.future.engine.ShadowRouteEngine;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowInsertStatementRoutingEngineTest {
    
    private ShadowRouteEngine shadowRoutingEngine;
    
    @Before
    public void init() {
        shadowRoutingEngine = new ShadowInsertStatementRoutingEngine(createInsertStatementContext());
    }
    
    @Test
    public void assertRoute() {
        shadowRoutingEngine.route(createRouteContext(), new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration()));
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration("shadow", Arrays.asList("ds", "ds1"), Arrays.asList("shadow_ds", "shadow_ds1"));
        result.setEnable(true);
        result.setDataSources(createDataSources());
        result.setTables(createTables());
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }
    
    private Map<String, ShadowAlgorithm> createShadowAlgorithms() {
        Map<String, ShadowAlgorithm> result = new LinkedHashMap<>();
        result.put("simple-note-algorithm", createNoteShadowAlgorithm());
        result.put("user-id-insert-regex-algorithm", createColumnShadowAlgorithm("user_id", "insert"));
        result.put("user-id-update-regex-algorithm", createColumnShadowAlgorithm("user_id", "update"));
        result.put("order-id-insert-regex-algorithm", createColumnShadowAlgorithm("order_id", "insert"));
        return result;
    }
    
    private ShadowAlgorithm createColumnShadowAlgorithm(final String column, final String operation) {
        ColumnRegexMatchShadowAlgorithm columnRegexMatchShadowAlgorithm = new ColumnRegexMatchShadowAlgorithm();
        columnRegexMatchShadowAlgorithm.setProps(createColumnProperties(column, operation));
        columnRegexMatchShadowAlgorithm.init();
        return columnRegexMatchShadowAlgorithm;
    }
    
    private Properties createColumnProperties(final String column, final String operation) {
        Properties properties = new Properties();
        properties.setProperty("column", column);
        properties.setProperty("operation", operation);
        properties.setProperty("regex", "[1]");
        return properties;
    }
    
    private ShadowAlgorithm createNoteShadowAlgorithm() {
        SimpleSQLNoteShadowAlgorithm simpleSQLNoteShadowAlgorithm = new SimpleSQLNoteShadowAlgorithm();
        simpleSQLNoteShadowAlgorithm.setProps(createNoteProperties());
        simpleSQLNoteShadowAlgorithm.init();
        return simpleSQLNoteShadowAlgorithm;
    }
    
    private Properties createNoteProperties() {
        Properties properties = new Properties();
        properties.setProperty("shadow", "true");
        return properties;
    }
    
    private Map<String, ShadowTableConfiguration> createTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        result.put("t_user", new ShadowTableConfiguration(createShadowAlgorithmNames("t_user")));
        result.put("t_order", new ShadowTableConfiguration(createShadowAlgorithmNames("t_order")));
        return result;
    }
    
    private Collection<String> createShadowAlgorithmNames(final String tableName) {
        Collection<String> result = new LinkedList<>();
        if ("t_user".equals(tableName)) {
            result.add("user-id-insert-regex-algorithm");
            result.add("user-id-update-regex-algorithm");
        } else {
            result.add("order-id-insert-regex-algorithm");
            result.add("simple-note-algorithm");
        }
        return result;
    }
    
    private Map<String, ShadowDataSourceConfiguration> createDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("ds-data-source", new ShadowDataSourceConfiguration("ds", "ds_shadow"));
        result.put("ds1-data-source", new ShadowDataSourceConfiguration("ds1", "ds1_shadow"));
        return result;
    }
    
    private InsertStatementContext createInsertStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class);
        when(result.getInsertColumnNames()).thenReturn(createInsertColumnNames());
        when(result.getGroupedParameters()).thenReturn(createGroupedParameters());
        Collection<SimpleTableSegment> allTables = createAllTables();
        when(result.getAllTables()).thenReturn(allTables);
        return result;
    }
    
    private List<String> createInsertColumnNames() {
        return Lists.newArrayList("user_id", "user_name", "user_pwd");
    }
    
    private List<List<Object>> createGroupedParameters() {
        List<List<Object>> result = new LinkedList<>();
        result.add(Lists.newArrayList(1, 1));
        result.add(Lists.newArrayList("jack", "rose"));
        result.add(Lists.newArrayList("123456", "123456"));
        return result;
    }
    
    private Collection<SimpleTableSegment> createAllTables() {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        result.add(createSimpleTableSegment());
        return result;
    }
    
    private SimpleTableSegment createSimpleTableSegment() {
        IdentifierValue identifierValue = mock(IdentifierValue.class);
        when(identifierValue.getValue()).thenReturn("t_user");
        return new SimpleTableSegment(new TableNameSegment(0, 10, identifierValue));
    }
    
    private RouteContext createRouteContext() {
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.getRouteUnits()).thenReturn(createRouteUnits());
        return routeContext;
    }
    
    private Collection<RouteUnit> createRouteUnits() {
        Collection<RouteUnit> result = new LinkedList<>();
        result.add(new RouteUnit(new RouteMapper("logic_ds", "ds"), Lists.newArrayList()));
        return result;
    }
}
