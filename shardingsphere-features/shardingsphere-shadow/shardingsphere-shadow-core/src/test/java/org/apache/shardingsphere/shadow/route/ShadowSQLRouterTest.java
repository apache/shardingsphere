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

package org.apache.shardingsphere.shadow.route;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowSQLRouterTest {
    
    private ShadowSQLRouter router;
    
    @Before
    public void init() {
        router = new ShadowSQLRouter();
    }
    
    @Test
    public void assertCreateRouteContext() {
        RouteContext routeContext = router.createRouteContext(mock(LogicSQL.class), mock(ShardingSphereMetaData.class), mock(ShadowRule.class), mock(ConfigurationProperties.class));
        assertNotNull(routeContext);
    }
    
//    @Test
//    public void assertDecorateRouteContext() {
//        router.decorateRouteContext(createRouteContext(), mock(LogicSQL.class), mock(ShardingSphereMetaData.class), new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration()),
//                mock(ConfigurationProperties.class));
//    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setDataSources(createDataSources());
        result.setTables(createTables());
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }
    
    private Map<String, ShadowAlgorithm> createShadowAlgorithms() {
        Map<String, ShadowAlgorithm> result = new LinkedHashMap<>();
        result.put("user-id-insert-regex-algorithm", createColumnShadowAlgorithm());
        return result;
    }
    
    private ShadowAlgorithm createColumnShadowAlgorithm() {
        final ColumnRegexMatchShadowAlgorithm columnRegexMatchShadowAlgorithm = new ColumnRegexMatchShadowAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("column", "user_id");
        properties.setProperty("operation", "insert");
        properties.setProperty("regex", "[1]");
        columnRegexMatchShadowAlgorithm.setProps(properties);
        columnRegexMatchShadowAlgorithm.init();
        return columnRegexMatchShadowAlgorithm;
    }
    
    private Map<String, ShadowTableConfiguration> createTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        Collection<String> shadowAlgorithmNames = new LinkedList<>();
        shadowAlgorithmNames.add("user-id-insert-regex-algorithm");
        result.put("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source"), shadowAlgorithmNames));
        return result;
    }
    
    private Map<String, ShadowDataSourceConfiguration> createDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("shadow-data-source", new ShadowDataSourceConfiguration("ds", "ds_shadow"));
        return result;
    }
    
    private RouteContext createRouteContext() {
        RouteContext result = mock(RouteContext.class);
        Collection<RouteUnit> routeUnits = new LinkedList<>();
        Collection<RouteMapper> tableRouteMappers = new LinkedList<>();
        tableRouteMappers.add(new RouteMapper("t_order", "t_order"));
        routeUnits.add(new RouteUnit(new RouteMapper("shadow-data-source", "shadow-data-source"), tableRouteMappers));
        when(result.getRouteUnits()).thenReturn(routeUnits);
        return result;
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(router.getOrder(), is(ShadowOrder.ORDER));
    }
    
    @Test
    public void getTypeClass() {
        assertThat(router.getTypeClass() == ShadowRule.class, is(true));
    }
}
