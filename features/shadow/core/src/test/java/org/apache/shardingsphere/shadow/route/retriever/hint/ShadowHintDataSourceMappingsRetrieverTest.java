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

package org.apache.shardingsphere.shadow.route.retriever.hint;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShadowHintDataSourceMappingsRetrieverTest {
    
    @Test
    void assertRetrieveWithShadowHint() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setShadow(true);
        assertThat(new ShadowHintDataSourceMappingsRetriever(hintValueContext).retrieve(new ShadowRule(createRuleConfiguration())), is(Collections.singletonMap("prod_ds", "shadow_ds")));
    }
    
    @Test
    void assertRetrieverWithNotShadowHint() {
        HintValueContext hintValueContext = new HintValueContext();
        assertThat(new ShadowHintDataSourceMappingsRetriever(hintValueContext).retrieve(new ShadowRule(createRuleConfiguration())), is(Collections.emptyMap()));
    }
    
    private ShadowRuleConfiguration createRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "prod_ds", "shadow_ds")));
        result.setTables(Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.singleton("foo_ds"), Collections.singleton("sql-hint-algorithm"))));
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }
    
    private Map<String, AlgorithmConfiguration> createShadowAlgorithms() {
        return Collections.singletonMap("sql-hint-algorithm", new AlgorithmConfiguration("SQL_HINT", PropertiesBuilder.build(new Property("shadow", Boolean.TRUE.toString()))));
    }
}
