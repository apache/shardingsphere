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

package org.apache.shardingsphere.governance.core.metadata.yaml;

import org.apache.shardingsphere.governance.core.metadata.MetaDataJson;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class RuleSchemaMetaDataYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlRuleSchemaMetaData() {
        RuleSchemaMetaData ruleSchemaMetaData = new RuleSchemaMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(MetaDataJson.META_DATA, YamlRuleSchemaMetaData.class));
        YamlRuleSchemaMetaData yamlRuleSchemaMetaData = new RuleSchemaMetaDataYamlSwapper().swapToYamlConfiguration(ruleSchemaMetaData);
        assertNotNull(yamlRuleSchemaMetaData);
        assertNotNull(yamlRuleSchemaMetaData.getConfiguredSchemaMetaData());
        assertNotNull(yamlRuleSchemaMetaData.getUnconfiguredSchemaMetaDataMap());
        assertThat(yamlRuleSchemaMetaData.getConfiguredSchemaMetaData().getTables().keySet(), is(Collections.singleton("t_order")));
        assertThat(yamlRuleSchemaMetaData.getConfiguredSchemaMetaData().getTables().get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(yamlRuleSchemaMetaData.getConfiguredSchemaMetaData().getTables().get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
        assertThat(yamlRuleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().keySet(), is(Collections.singleton("ds_0")));
        assertThat(yamlRuleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().get("ds_0").getTables().keySet(), is(Collections.singleton("t_user")));
        assertThat(yamlRuleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().get("ds_0").getTables().get("t_user").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(yamlRuleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().get("ds_0").getTables().get("t_user").getColumns().keySet(), is(Collections.singleton("id")));
    }
    
    @Test
    public void assertSwapToRuleSchemaMetaData() {
        YamlRuleSchemaMetaData yamlRuleSchemaMetaData = YamlEngine.unmarshal(MetaDataJson.META_DATA, YamlRuleSchemaMetaData.class);
        RuleSchemaMetaData ruleSchemaMetaData = new RuleSchemaMetaDataYamlSwapper().swapToObject(yamlRuleSchemaMetaData);
        assertNotNull(ruleSchemaMetaData);
        assertNotNull(ruleSchemaMetaData.getConfiguredSchemaMetaData());
        assertNotNull(ruleSchemaMetaData.getConfiguredSchemaMetaData());
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().getAllTableNames(), is(Collections.singleton("t_order")));
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().getAllColumnNames("t_order").size(), is(1));
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
        assertThat(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().keySet(), is(Collections.singleton("ds_0")));
        assertThat(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().get("ds_0").getAllTableNames(), is(Collections.singleton("t_user")));
        assertThat(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().get("ds_0").get("t_user").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().get("ds_0").get("t_user").getColumns().keySet(), is(Collections.singleton("id")));
    }
}
