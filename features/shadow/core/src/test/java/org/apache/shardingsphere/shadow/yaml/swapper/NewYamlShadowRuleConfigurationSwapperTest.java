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

package org.apache.shardingsphere.shadow.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NewYamlShadowRuleConfigurationSwapperTest {
    
    private final NewYamlShadowRuleConfigurationSwapper swapper = new NewYamlShadowRuleConfigurationSwapper();
    
    @Test
    void assertSwapEmptyConfigToDataNodes() {
        ShadowRuleConfiguration config = new ShadowRuleConfiguration();
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(0));
    }
    
    @Test
    void assertSwapFullConfigToDataNodes() {
        ShadowRuleConfiguration config = createMaximumShadowRule();
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(4));
        Iterator<YamlDataNode> iterator = result.iterator();
        assertThat(iterator.next().getKey(), is("algorithms/FIXTURE"));
        assertThat(iterator.next().getKey(), is("default_algorithm_name"));
        assertThat(iterator.next().getKey(), is("data_sources/foo"));
        assertThat(iterator.next().getKey(), is("tables/foo_table"));
    }
    
    private ShadowRuleConfiguration createMaximumShadowRule() {
        Collection<ShadowDataSourceConfiguration> dataSources = new LinkedList<>();
        dataSources.add(new ShadowDataSourceConfiguration("foo", "ds_0", "ds_1"));
        Map<String, ShadowTableConfiguration> tables = new LinkedHashMap<>();
        tables.put("foo_table", new ShadowTableConfiguration(Collections.singleton("ds_0"), Collections.singleton("FIXTURE")));
        Map<String, AlgorithmConfiguration> shadowAlgorithms = new LinkedHashMap<>();
        shadowAlgorithms.put("FIXTURE", new AlgorithmConfiguration("FIXTURE", new Properties()));
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setDataSources(dataSources);
        result.setTables(tables);
        result.setShadowAlgorithms(shadowAlgorithms);
        result.setDefaultShadowAlgorithmName("FIXTURE");
        return result;
    }
    
    @Test
    void assertSwapToObjectEmpty() {
        Collection<YamlDataNode> config = new LinkedList<>();
        assertFalse(swapper.swapToObject(config).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        Collection<YamlDataNode> config = new LinkedList<>();
        config.add(new YamlDataNode("/metadata/foo_db/rules/shadow/data_sources/foo_db/versions/0", "productionDataSourceName: ds_0\n"
                + "shadowDataSourceName: ds_1\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/shadow/tables/foo_table/versions/0", "dataSourceNames:\n"
                + "- ds_0\n"
                + "shadowAlgorithmNames:\n"
                + "- FIXTURE\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/shadow/algorithms/FIXTURE/versions/0", "type: FIXTURE\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/shadow/default_algorithm_name/versions/0", "FIXTURE"));
        ShadowRuleConfiguration result = swapper.swapToObject(config).get();
        assertThat(result.getDataSources().size(), is(1));
        assertThat(result.getDataSources().iterator().next().getName(), is("foo_db"));
        assertThat(result.getDataSources().iterator().next().getProductionDataSourceName(), is("ds_0"));
        assertThat(result.getDataSources().iterator().next().getShadowDataSourceName(), is("ds_1"));
        assertThat(result.getTables().size(), is(1));
        assertThat(result.getTables().get("foo_table").getDataSourceNames().size(), is(1));
        assertThat(result.getTables().get("foo_table").getDataSourceNames().iterator().next(), is("ds_0"));
        assertThat(result.getTables().get("foo_table").getShadowAlgorithmNames().size(), is(1));
        assertThat(result.getTables().get("foo_table").getShadowAlgorithmNames().iterator().next(), is("FIXTURE"));
        assertThat(result.getShadowAlgorithms().size(), is(1));
        assertThat(result.getShadowAlgorithms().get("FIXTURE").getType(), is("FIXTURE"));
        assertThat(result.getShadowAlgorithms().get("FIXTURE").getProps().size(), is(0));
        assertThat(result.getDefaultShadowAlgorithmName(), is("FIXTURE"));
    }
}
