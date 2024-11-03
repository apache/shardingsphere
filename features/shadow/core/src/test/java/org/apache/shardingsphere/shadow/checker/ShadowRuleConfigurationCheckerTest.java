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
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
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

class ShadowRuleConfigurationCheckerTest {
    
    private ShadowRuleConfigurationChecker ruleConfigChecker;
    
    @BeforeEach
    void setUp() {
        ruleConfigChecker = (ShadowRuleConfigurationChecker) OrderedSPILoader.getServicesByClass(
                RuleConfigurationChecker.class, Collections.singleton(ShadowRuleConfiguration.class)).get(ShadowRuleConfiguration.class);
    }
    
    @Test
    void assertCheck() {
        ruleConfigChecker.check("", createRuleConfiguration(), createDataSourceMap(), Collections.emptyList());
    }
    
    private ShadowRuleConfiguration createRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap("hint-algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties())));
        result.setDefaultShadowAlgorithmName("hint-algorithm");
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "ds", "ds_shadow")));
        result.setTables(Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.singletonList("foo_ds"), new LinkedList<>(Collections.singleton("hint-algorithm")))));
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
        result.put("ds", new MockedDataSource());
        result.put("ds_shadow", new MockedDataSource());
        return result;
    }
    
    @Test
    void assertGetRequiredDataSourceNames() {
        assertThat(ruleConfigChecker.getRequiredDataSourceNames(createRuleConfiguration()), is(new LinkedHashSet<>(Arrays.asList("ds_shadow", "ds"))));
    }
}
