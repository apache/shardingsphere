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
import org.apache.shardingsphere.shadow.algorithm.shadow.hint.SimpleHintShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
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

public final class HintShadowAlgorithmDeterminerTest {

    private ShadowAlgorithmDeterminer shadowAlgorithmDeterminer;

    @Before
    public void init() {
        shadowAlgorithmDeterminer = new HintShadowAlgorithmDeterminer(createHintShadowAlgorithm());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private HintShadowAlgorithm<Comparable<?>> createHintShadowAlgorithm() {
        HintShadowAlgorithm result = new SimpleHintShadowAlgorithm();
        Properties properties = new Properties();
        properties.setProperty("shadow", "true");
        properties.setProperty("foo", "bar");
        result.setProps(properties);
        result.init();
        return result;
    }
    
    @Test
    public void assertIsShadow() {
        assertThat(shadowAlgorithmDeterminer.isShadow(createShadowDetermineCondition(), new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration())), is(true));
    }

    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setDataSources(createDataSources());
        result.setTables(createTables());
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }

    private Map<String, ShadowAlgorithm> createShadowAlgorithms() {
        Map<String, ShadowAlgorithm> result = new LinkedHashMap<>();
        result.put("simple-hint-algorithm", createHintShadowAlgorithm());
        return result;
    }

    private Map<String, ShadowTableConfiguration> createTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        result.put("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source-0"), createShadowAlgorithmNames()));
        return result;
    }

    private Collection<String> createShadowAlgorithmNames() {
        Collection<String> result = new LinkedList<>();
        result.add("simple-hint-algorithm");
        return result;
    }
    
    private Map<String, ShadowDataSourceConfiguration> createDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("shadow-data-source-0", new ShadowDataSourceConfiguration("ds", "ds_shadow"));
        result.put("shadow-data-source-1", new ShadowDataSourceConfiguration("ds1", "ds1_shadow"));
        return result;
    }
    
    private ShadowDetermineCondition createShadowDetermineCondition() {
        ShadowDetermineCondition result = new ShadowDetermineCondition("t_order", ShadowOperationType.INSERT);
        Collection<String> sqlComments = new LinkedList<>();
        sqlComments.add("/*foo:bar,shadow:true*/");
        return result.initSQLComments(sqlComments);
    }
}
