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

package org.apache.shardingsphere.shadow.route.engine.determiner.algorithm;

import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.engine.determiner.ShadowAlgorithmDeterminer;
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
import static org.junit.Assert.assertThat;

public final class ColumnShadowAlgorithmDeterminerTest {
    
    private ShadowAlgorithmDeterminer shadowAlgorithmDeterminer;
    
    @Before
    public void init() {
        shadowAlgorithmDeterminer = new ColumnShadowAlgorithmDeterminer(createColumnShadowAlgorithms());
    }
    
    private ColumnShadowAlgorithm<Comparable<?>> createColumnShadowAlgorithms() {
        ColumnShadowAlgorithm<Comparable<?>> result = new ColumnRegexMatchShadowAlgorithm();
        result.setProps(createProperties());
        result.init();
        return result;
    }
    
    private Properties createProperties() {
        Properties properties = new Properties();
        properties.setProperty("column", "user_id");
        properties.setProperty("operation", "insert");
        properties.setProperty("regex", "[1]");
        return properties;
    }
    
    @Test
    public void assertIsShadow() {
        assertThat(shadowAlgorithmDeterminer.isShadow(createShadowDetermineCondition(), new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration()), "t_order"), is(true));
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setEnable(true);
        result.setDataSources(createDataSources());
        result.setTables(createTables());
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }
    
    private Map<String, ShadowAlgorithm> createShadowAlgorithms() {
        Map<String, ShadowAlgorithm> result = new LinkedHashMap<>();
        result.put("user_id-insert-regex-algorithm", createColumnShadowAlgorithms());
        return result;
    }
    
    private Map<String, ShadowTableConfiguration> createTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        result.put("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source-0"), createShadowAlgorithmNames()));
        return result;
    }
    
    private Collection<String> createShadowAlgorithmNames() {
        Collection<String> result = new LinkedList<>();
        result.add("user_id-insert-regex-algorithm");
        return result;
    }
    
    private Map<String, ShadowDataSourceConfiguration> createDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("shadow-data-source-0", new ShadowDataSourceConfiguration("ds", "ds_shadow"));
        result.put("shadow-data-source-1", new ShadowDataSourceConfiguration("ds1", "ds1_shadow"));
        return result;
    }
    
    private ShadowDetermineCondition createShadowDetermineCondition() {
        ShadowDetermineCondition shadowDetermineCondition = new ShadowDetermineCondition(ShadowOperationType.INSERT);
        shadowDetermineCondition.initShadowColumnCondition(createColumnValuesMappings());
        return shadowDetermineCondition;
    }
    
    private Collection<ShadowColumnCondition> createColumnValuesMappings() {
        Collection<ShadowColumnCondition> result = new LinkedList<>();
        Collection<Comparable<?>> values = new LinkedList<>();
        values.add(1);
        result.add(new ShadowColumnCondition("t_order", "user_id", values));
        return result;
    }
}
