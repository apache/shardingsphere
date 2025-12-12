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

package org.apache.shardingsphere.shadow.rule;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowRuleTest {
    
    private ShadowRule rule;
    
    @BeforeEach
    void init() {
        rule = new ShadowRule(createRuleConfiguration());
    }
    
    private ShadowRuleConfiguration createRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setDataSources(createDataSources());
        result.setTables(createTables());
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }
    
    private Collection<ShadowDataSourceConfiguration> createDataSources() {
        return Arrays.asList(new ShadowDataSourceConfiguration("foo_ds_0", "prod_ds_0", "shadow_ds_0"),
                new ShadowDataSourceConfiguration("foo_ds_1", "prod_ds_1", "shadow_ds_1"));
    }
    
    private Map<String, ShadowTableConfiguration> createTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>(2, 1F);
        result.put("foo_tbl", new ShadowTableConfiguration(Collections.singleton("foo_ds_0"), createShadowAlgorithmNames("foo_tbl")));
        result.put("bar_tbl", new ShadowTableConfiguration(Collections.singleton("foo_ds_1"), createShadowAlgorithmNames("bar_tbl")));
        return result;
    }
    
    private Collection<String> createShadowAlgorithmNames(final String tableName) {
        Collection<String> result = new LinkedList<>();
        result.add("sql-hint-algorithm");
        if ("foo_tbl".equals(tableName)) {
            result.add("foo-id-insert-regex-algorithm");
            result.add("foo-id-update-regex-algorithm");
        } else {
            result.add("bar-id-insert-regex-algorithm");
        }
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> createShadowAlgorithms() {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(4, 1F);
        result.put("sql-hint-algorithm", new AlgorithmConfiguration("SQL_HINT", PropertiesBuilder.build(new Property("shadow", Boolean.TRUE.toString()))));
        result.put("foo-id-insert-regex-algorithm", new AlgorithmConfiguration("REGEX_MATCH",
                PropertiesBuilder.build(new Property("column", "foo_id"), new Property("operation", "insert"), new Property("regex", "[1]"))));
        result.put("foo-id-update-regex-algorithm", new AlgorithmConfiguration("REGEX_MATCH",
                PropertiesBuilder.build(new Property("column", "foo_id"), new Property("operation", "update"), new Property("regex", "[1]"))));
        result.put("bar-id-insert-regex-algorithm", new AlgorithmConfiguration("REGEX_MATCH",
                PropertiesBuilder.build(new Property("column", "bar_id"), new Property("operation", "insert"), new Property("regex", "[1]"))));
        return result;
    }
    
    @Test
    void assertContainsShadowAlgorithm() {
        assertTrue(rule.containsShadowAlgorithm("sql-hint-algorithm"));
    }
    
    @Test
    void assertGetDefaultShadowAlgorithm() {
        assertFalse(rule.getDefaultShadowAlgorithm().isPresent());
    }
    
    @Test
    void assertFilterShadowTables() {
        assertThat(rule.filterShadowTables(Arrays.asList("foo_tbl", "no_tbl")), is(Collections.singletonList("foo_tbl")));
    }
    
    @Test
    void assertGetAllShadowTableNames() {
        assertThat(rule.getAllShadowTableNames(), is(new HashSet<>(Arrays.asList("foo_tbl", "bar_tbl"))));
    }
    
    @Test
    void assertGetAllHintShadowAlgorithms() {
        assertThat(rule.getAllHintShadowAlgorithms().size(), is(1));
    }
    
    @Test
    void assertGetHintShadowAlgorithms() {
        assertThat(rule.getHintShadowAlgorithms("foo_tbl").size(), is(1));
    }
    
    @Test
    void assertGetColumnShadowAlgorithms() {
        assertThat(rule.getColumnShadowAlgorithms(ShadowOperationType.INSERT, "foo_tbl", "foo_id").size(), is(1));
        assertTrue(rule.getColumnShadowAlgorithms(ShadowOperationType.INSERT, "foo_tbl", "bar_id").isEmpty());
    }
    
    @Test
    void assertGetShadowColumnNames() {
        assertThat(rule.getShadowColumnNames(ShadowOperationType.INSERT, "foo_tbl").size(), is(1));
    }
    
    @Test
    void assertGetShadowDataSourceMappings() {
        assertThat(rule.getShadowDataSourceMappings("foo_tbl"), is(Collections.singletonMap("prod_ds_0", "shadow_ds_0")));
    }
    
    @Test
    void assertGetAllShadowDataSourceMappings() {
        assertThat(rule.getAllShadowDataSourceMappings().size(), is(2));
        assertThat(rule.getAllShadowDataSourceMappings().get("prod_ds_0"), is("shadow_ds_0"));
        assertThat(rule.getAllShadowDataSourceMappings().get("prod_ds_1"), is("shadow_ds_1"));
    }
    
    @Test
    void assertFindProductionDataSourceNameSuccess() {
        assertThat(rule.findProductionDataSourceName("foo_ds_0"), is(Optional.of("prod_ds_0")));
        assertThat(rule.findProductionDataSourceName("foo_ds_1"), is(Optional.of("prod_ds_1")));
    }
    
    @Test
    void assertFindProductionDataSourceNameFailed() {
        assertFalse(rule.findProductionDataSourceName("foo_ds_2").isPresent());
    }
}
