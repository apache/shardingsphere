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

package org.apache.shardingsphere.shadow.route.finder.other;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShadowHintDataSourceMappingsFinderTest {
    
    private ShadowHintDataSourceMappingsFinder finder;
    
    @BeforeEach
    void init() {
        finder = new ShadowHintDataSourceMappingsFinder(createHintValueContext());
    }
    
    private HintValueContext createHintValueContext() {
        HintValueContext result = new HintValueContext();
        result.setShadow(true);
        return result;
    }
    
    @Test
    void assertRoute() {
        Map<String, String> shadowDataSourceMappings = finder.find(new ShadowRule(createShadowRuleConfiguration()));
        assertThat(shadowDataSourceMappings, is(Collections.singletonMap("ds", "ds_shadow")));
    }
    
    private ShadowRuleConfiguration createShadowRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("shadow-data-source", "ds", "ds_shadow")));
        result.setTables(Collections.singletonMap("t_order", new ShadowTableConfiguration(Collections.singleton("shadow-data-source"), Collections.singleton("sql-hint-algorithm"))));
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> createShadowAlgorithms() {
        return Collections.singletonMap("sql-hint-algorithm", new AlgorithmConfiguration("SQL_HINT", PropertiesBuilder.build(new Property("shadow", Boolean.TRUE.toString()))));
    }
}
