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

package org.apache.shardingsphere.governance.core.metadata;

import org.apache.shardingsphere.governance.core.metadata.yaml.YamlRuleSchemaMetaData;
import org.apache.shardingsphere.governance.repository.api.GovernanceRepository;
import org.apache.shardingsphere.governance.core.metadata.yaml.RuleSchemaMetaDataYamlSwapper;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataCenterTest {
    
    @Mock
    private GovernanceRepository repository;
    
    private MetaDataCenter metaDataCenter;
    
    @Before
    public void setUp() {
        metaDataCenter = new MetaDataCenter(repository);
    }
    
    @Test
    public void assertPersistMetaDataCenterNode() {
        RuleSchemaMetaData ruleSchemaMetaData = new RuleSchemaMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(MetaDataJson.META_DATA, YamlRuleSchemaMetaData.class));
        metaDataCenter.persistMetaDataCenterNode("schema", ruleSchemaMetaData);
        verify(repository).persist(eq("/metadata/schema"), anyString());
    }
    
    @Test
    public void assertLoadRuleSchemaMetaData() {
        when(repository.get("/metadata/schema")).thenReturn(MetaDataJson.META_DATA);
        Optional<RuleSchemaMetaData> optionalRuleSchemaMetaData = metaDataCenter.loadRuleSchemaMetaData("schema");
        assertTrue(optionalRuleSchemaMetaData.isPresent());
        Optional<RuleSchemaMetaData> empty = metaDataCenter.loadRuleSchemaMetaData("test");
        assertThat(empty, is(Optional.empty()));
        RuleSchemaMetaData ruleSchemaMetaData = optionalRuleSchemaMetaData.get();
        verify(repository).get(eq("/metadata/schema"));
        assertNotNull(ruleSchemaMetaData);
        assertNotNull(ruleSchemaMetaData.getConfiguredSchemaMetaData());
        assertNotNull(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap());
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
