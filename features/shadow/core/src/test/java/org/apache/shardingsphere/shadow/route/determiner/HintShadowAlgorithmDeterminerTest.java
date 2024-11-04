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

package org.apache.shardingsphere.shadow.route.determiner;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.condition.ShadowCondition;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.hint.HintShadowAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HintShadowAlgorithmDeterminerTest {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertIsShadow() {
        HintShadowAlgorithm hintShadowAlgorithm = (HintShadowAlgorithm) TypedSPILoader.getService(ShadowAlgorithm.class, "SQL_HINT", new Properties());
        HintValueContext hintValueContext = createHintValueContext();
        assertTrue(HintShadowAlgorithmDeterminer.isShadow(hintShadowAlgorithm, createShadowCondition(), new ShadowRule(createShadowRuleConfiguration()), hintValueContext.isShadow()));
    }
    
    private HintValueContext createHintValueContext() {
        HintValueContext result = new HintValueContext();
        result.setShadow(true);
        return result;
    }
    
    private ShadowRuleConfiguration createShadowRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setDataSources(createDataSources());
        result.setTables(Collections.singletonMap("t_order", new ShadowTableConfiguration(Collections.singleton("shadow-data-source-0"), Collections.singleton("sql-hint-algorithm"))));
        result.setShadowAlgorithms(Collections.singletonMap("sql-hint-algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties())));
        return result;
    }
    
    private Collection<ShadowDataSourceConfiguration> createDataSources() {
        Collection<ShadowDataSourceConfiguration> result = new LinkedList<>();
        result.add(new ShadowDataSourceConfiguration("shadow-data-source-0", "ds", "ds_shadow"));
        result.add(new ShadowDataSourceConfiguration("shadow-data-source-1", "ds1", "ds1_shadow"));
        return result;
    }
    
    private ShadowCondition createShadowCondition() {
        return new ShadowCondition("t_order", ShadowOperationType.INSERT);
    }
}
