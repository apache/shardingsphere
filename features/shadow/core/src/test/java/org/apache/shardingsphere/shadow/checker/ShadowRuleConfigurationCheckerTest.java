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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Collections;

import static org.mockito.Mockito.mock;

public final class ShadowRuleConfigurationCheckerTest {
    
    @Test
    public void assertCheck() {
        new ShadowRuleConfigurationChecker().check("", createShadowRuleConfiguration(), createDataSourceMap(), Collections.emptyList());
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds", mock(DataSource.class));
        result.put("ds_shadow", mock(DataSource.class));
        return result;
    }
    
    private ShadowRuleConfiguration createShadowRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(createShadowAlgorithmConfigurations());
        result.setDataSources(createDataSources());
        result.setTables(createTables());
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> createShadowAlgorithmConfigurations() {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>();
        result.put("user-id-insert-match-algorithm", createAlgorithmConfiguration());
        return result;
    }
    
    private AlgorithmConfiguration createAlgorithmConfiguration() {
        return new AlgorithmConfiguration("user-id-insert-match-algorithm", createProperties());
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("column", "shadow");
        result.setProperty("operation", "insert");
        result.setProperty("regex", "[1]");
        return result;
    }
    
    private Map<String, ShadowTableConfiguration> createTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        Collection<String> dataSourceNames = new LinkedList<>();
        Collection<String> shadowAlgorithmNames = new LinkedList<>();
        shadowAlgorithmNames.add("user-id-insert-match-algorithm");
        result.put("t_order", new ShadowTableConfiguration(dataSourceNames, shadowAlgorithmNames));
        return result;
    }
    
    private Collection<ShadowDataSourceConfiguration> createDataSources() {
        Collection<ShadowDataSourceConfiguration> result = new LinkedList<>();
        result.add(new ShadowDataSourceConfiguration("shadow-data-source", "ds", "ds_shadow"));
        return result;
    }
}
