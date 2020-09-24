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

package org.apache.shardingsphere.governance.core.yaml.swapper;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.yaml.config.metadata.YamlRuleSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class RuleSchemaMetaDataYamlSwapperTest {
    
    private static final String META_DATA_YAM = "yaml/metadata.yaml";
    
    @Test
    public void assertSwapToYamlRuleSchemaMetaData() {
        RuleSchemaMetaData ruleSchemaMetaData = new RuleSchemaMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(META_DATA_YAM), YamlRuleSchemaMetaData.class));
        YamlRuleSchemaMetaData yamlRuleSchemaMetaData = new RuleSchemaMetaDataYamlSwapper().swapToYamlConfiguration(ruleSchemaMetaData);
        assertNotNull(yamlRuleSchemaMetaData);
        assertNotNull(yamlRuleSchemaMetaData.getConfiguredSchemaMetaData());
        assertNotNull(yamlRuleSchemaMetaData.getUnconfiguredSchemaMetaDataMap());
        assertThat(yamlRuleSchemaMetaData.getConfiguredSchemaMetaData().getTables().keySet(), is(Collections.singleton("t_order")));
        assertThat(yamlRuleSchemaMetaData.getConfiguredSchemaMetaData().getTables().get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(yamlRuleSchemaMetaData.getConfiguredSchemaMetaData().getTables().get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
        assertThat(yamlRuleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().keySet(), is(Collections.singleton("ds_0")));
        assertThat(yamlRuleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().get("ds_0"), is(Arrays.asList("t_user")));
    }
    
    @Test
    public void assertSwapToRuleSchemaMetaData() {
        YamlRuleSchemaMetaData yamlRuleSchemaMetaData = YamlEngine.unmarshal(readYAML(META_DATA_YAM), YamlRuleSchemaMetaData.class);
        RuleSchemaMetaData ruleSchemaMetaData = new RuleSchemaMetaDataYamlSwapper().swapToObject(yamlRuleSchemaMetaData);
        assertNotNull(ruleSchemaMetaData);
        assertNotNull(ruleSchemaMetaData.getConfiguredSchemaMetaData());
        assertNotNull(ruleSchemaMetaData.getConfiguredSchemaMetaData());
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().getAllTableNames(), is(Collections.singleton("t_order")));
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().getAllColumnNames("t_order").size(), is(1));
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
        assertThat(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().keySet(), is(Collections.singleton("ds_0")));
        assertThat(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().get("ds_0"), is(Arrays.asList("t_user")));
    }
    
    @SneakyThrows
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI()))
                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
