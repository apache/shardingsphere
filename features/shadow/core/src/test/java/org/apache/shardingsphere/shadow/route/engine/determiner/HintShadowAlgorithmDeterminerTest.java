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

import org.apache.shardingsphere.infra.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.Assert.assertTrue;

public final class HintShadowAlgorithmDeterminerTest {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertIsShadow() {
        HintShadowAlgorithm hintShadowAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(
                new AlgorithmConfiguration("SIMPLE_HINT", PropertiesBuilder.build(new Property("foo", "foo_value"))), ShadowAlgorithm.class);
        assertTrue(HintShadowAlgorithmDeterminer.isShadow(hintShadowAlgorithm, createShadowDetermineCondition(), new ShadowRule(createShadowRuleConfiguration())));
    }
    
    private ShadowRuleConfiguration createShadowRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setDataSources(createDataSources());
        result.setTables(Collections.singletonMap("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source-0"), Collections.singleton("simple-hint-algorithm"))));
        result.setShadowAlgorithms(Collections.singletonMap("simple-hint-algorithm", new AlgorithmConfiguration("SIMPLE_HINT", PropertiesBuilder.build(new Property("foo", "foo_value")))));
        return result;
    }
    
    private Collection<ShadowDataSourceConfiguration> createDataSources() {
        Collection<ShadowDataSourceConfiguration> result = new LinkedList<>();
        result.add(new ShadowDataSourceConfiguration("shadow-data-source-0", "ds", "ds_shadow"));
        result.add(new ShadowDataSourceConfiguration("shadow-data-source-1", "ds1", "ds1_shadow"));
        return result;
    }
    
    private ShadowDetermineCondition createShadowDetermineCondition() {
        return new ShadowDetermineCondition("t_order", ShadowOperationType.INSERT).initSQLComments(Collections.singleton("/* SHARDINGSPHERE_HINT: SHADOW=true */"));
    }
}
