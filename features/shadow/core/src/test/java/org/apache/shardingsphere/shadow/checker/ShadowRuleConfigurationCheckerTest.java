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

package org.apache.shardingsphere.shadow.checker;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.exception.metadata.MissingRequiredProductionDataSourceException;
import org.apache.shardingsphere.shadow.exception.metadata.MissingRequiredShadowDataSourceException;
import org.apache.shardingsphere.shadow.exception.metadata.NotImplementHintShadowAlgorithmException;
import org.apache.shardingsphere.shadow.exception.metadata.ShadowDataSourceMappingNotFoundException;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShadowRuleConfigurationCheckerTest {
    
    private ShadowRuleConfigurationChecker ruleConfigChecker;
    
    @BeforeEach
    void setUp() {
        ruleConfigChecker = (ShadowRuleConfigurationChecker) OrderedSPILoader.getServicesByClass(
                DatabaseRuleConfigurationChecker.class, Collections.singleton(ShadowRuleConfiguration.class)).get(ShadowRuleConfiguration.class);
    }
    
    @Test
    void assertCheckWithNotExistedDefaultShadowAlgorithm() {
        assertThrows(NotImplementHintShadowAlgorithmException.class,
                () -> ruleConfigChecker.check("foo_db", createRuleConfigurationWithNotExistedDefaultShadowAlgorithm(), createDataSourceMap(), Collections.emptyList()));
    }
    
    private ShadowRuleConfiguration createRuleConfigurationWithNotExistedDefaultShadowAlgorithm() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap("foo-algo", new AlgorithmConfiguration("SQL_HINT", new Properties())));
        result.setDefaultShadowAlgorithmName("bar-algo");
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds")));
        result.setTables(Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.singletonList("foo_ds"), new LinkedList<>(Collections.singleton("foo-algo")))));
        return result;
    }
    
    @Test
    void assertCheckWithInvalidDefaultShadowAlgorithm() {
        assertThrows(NotImplementHintShadowAlgorithmException.class,
                () -> ruleConfigChecker.check("foo_db", createRuleConfigurationWithInvalidDefaultShadowAlgorithm(), createDataSourceMap(), Collections.emptyList()));
    }
    
    private ShadowRuleConfiguration createRuleConfigurationWithInvalidDefaultShadowAlgorithm() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap("foo-algo", new AlgorithmConfiguration("REGEX_MATCH",
                PropertiesBuilder.build(new Property("column", "foo_id"), new Property("operation", "insert"), new Property("regex", "[1]")))));
        result.setDefaultShadowAlgorithmName("foo-algo");
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds")));
        result.setTables(Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.singletonList("foo_ds"), new LinkedList<>(Collections.singleton("foo-algo")))));
        return result;
    }
    
    @Test
    void assertCheckWithInvalidShadowTableDataSourcesReferences() {
        assertThrows(ShadowDataSourceMappingNotFoundException.class,
                () -> ruleConfigChecker.check("foo_db", createRuleConfigurationWithInvalidShadowTableDataSourcesReferences(), createDataSourceMap(), Collections.emptyList()));
    }
    
    private ShadowRuleConfiguration createRuleConfigurationWithInvalidShadowTableDataSourcesReferences() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap("foo-algo", new AlgorithmConfiguration("SQL_HINT", new Properties())));
        result.setDefaultShadowAlgorithmName("foo-algo");
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds")));
        result.setTables(Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.singletonList("bar_ds"), new LinkedList<>(Collections.singleton("foo-algo")))));
        return result;
    }
    
    @Test
    void assertCheckWithInvalidShadowTableAlgorithmsReferences() {
        assertThrows(MissingRequiredAlgorithmException.class,
                () -> ruleConfigChecker.check("foo_db", createRuleConfigurationWithInvalidShadowTableAlgorithmsReferences(), createDataSourceMap(), Collections.emptyList()));
    }
    
    private ShadowRuleConfiguration createRuleConfigurationWithInvalidShadowTableAlgorithmsReferences() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap("foo-algo", new AlgorithmConfiguration("SQL_HINT", new Properties())));
        result.setDefaultShadowAlgorithmName("foo-algo");
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds")));
        result.setTables(Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.singletonList("foo_ds"), new LinkedList<>(Collections.singleton("bar-algo")))));
        return result;
    }
    
    @Test
    void assertCheckWithoutProductionDataSourceName() {
        assertThrows(MissingRequiredProductionDataSourceException.class,
                () -> ruleConfigChecker.check("foo_db", createRuleConfigurationWithoutProductionDataSourceName(), createDataSourceMap(), Collections.emptyList()));
    }
    
    private ShadowRuleConfiguration createRuleConfigurationWithoutProductionDataSourceName() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap("foo-algo", new AlgorithmConfiguration("SQL_HINT", new Properties())));
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "no_prod_ds", "shadow_ds")));
        result.setTables(Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.singletonList("foo_ds"), new LinkedList<>(Collections.singleton("foo-algo")))));
        return result;
    }
    
    @Test
    void assertCheckWithoutShadowDataSourceName() {
        assertThrows(MissingRequiredShadowDataSourceException.class,
                () -> ruleConfigChecker.check("foo_db", createRuleConfigurationWithoutShadowDataSourceName(), createDataSourceMap(), Collections.emptyList()));
    }
    
    private ShadowRuleConfiguration createRuleConfigurationWithoutShadowDataSourceName() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap("foo-algo", new AlgorithmConfiguration("SQL_HINT", new Properties())));
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "no_shadow_ds")));
        result.setTables(Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.singletonList("foo_ds"), new LinkedList<>(Collections.singleton("foo-algo")))));
        return result;
    }
    
    @Test
    void assertCheckWithoutDefaultShadowAlgorithm() {
        assertDoesNotThrow(() -> ruleConfigChecker.check("foo_db", createRuleConfigurationWithoutDefaultShadowAlgorithm(), createDataSourceMap(), Collections.emptyList()));
    }
    
    private ShadowRuleConfiguration createRuleConfigurationWithoutDefaultShadowAlgorithm() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap("foo-algo", new AlgorithmConfiguration("SQL_HINT", new Properties())));
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds")));
        result.setTables(Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.singletonList("foo_ds"), new LinkedList<>(Collections.singleton("foo-algo")))));
        return result;
    }
    
    @Test
    void assertCheckWithDefaultShadowAlgorithm() {
        assertDoesNotThrow(() -> ruleConfigChecker.check("foo_db", createRuleConfiguration(), createDataSourceMap(), Collections.emptyList()));
    }
    
    private ShadowRuleConfiguration createRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap("foo-algo", new AlgorithmConfiguration("SQL_HINT", new Properties())));
        result.setDefaultShadowAlgorithmName("foo-algo");
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds")));
        result.setTables(Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.singletonList("foo_ds"), new LinkedList<>(Collections.singleton("foo-algo")))));
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
        result.put("prod_ds", new MockedDataSource());
        result.put("shadow_ds", new MockedDataSource());
        return result;
    }
    
    @Test
    void assertGetRequiredDataSourceNames() {
        assertThat(ruleConfigChecker.getRequiredDataSourceNames(createRuleConfiguration()), is(new LinkedHashSet<>(Arrays.asList("shadow_ds", "prod_ds"))));
    }
}
