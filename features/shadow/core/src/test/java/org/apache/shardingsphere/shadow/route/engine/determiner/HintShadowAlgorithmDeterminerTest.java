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

package org.apache.shardingsphere.shadow.route.engine.determiner;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.factory.ShadowAlgorithmFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public final class HintShadowAlgorithmDeterminerTest {
    
    @Test
    public void assertIsShadow() {
        assertTrue(HintShadowAlgorithmDeterminer.isShadow(createHintShadowAlgorithm(), createShadowDetermineCondition(), new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration())));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private HintShadowAlgorithm<Comparable<?>> createHintShadowAlgorithm() {
        return (HintShadowAlgorithm) ShadowAlgorithmFactory.newInstance(new AlgorithmConfiguration("SIMPLE_HINT", createProperties()));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("foo", "foo_value");
        return result;
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setDataSources(createDataSources());
        result.setTables(Collections.singletonMap("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source-0"), Collections.singleton("simple-hint-algorithm"))));
        result.setShadowAlgorithms(Collections.singletonMap("simple-hint-algorithm", createHintShadowAlgorithm()));
        return result;
    }
    
    private Map<String, ShadowDataSourceConfiguration> createDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>(2, 1);
        result.put("shadow-data-source-0", new ShadowDataSourceConfiguration("ds", "ds_shadow"));
        result.put("shadow-data-source-1", new ShadowDataSourceConfiguration("ds1", "ds1_shadow"));
        return result;
    }
    
    private ShadowDetermineCondition createShadowDetermineCondition() {
        return new ShadowDetermineCondition("t_order", ShadowOperationType.INSERT).initSQLComments(Collections.singleton("/* SHARDINGSPHERE_HINT: SHADOW=true */"));
    }
}
