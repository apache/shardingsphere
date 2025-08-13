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

package org.apache.shardingsphere.data.pipeline.core.util;

import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PipelineDataNodeUtilsTest {
    
    @Test
    void assertBuildTableAndDataNodesMap() {
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        DataNodeRuleAttribute notReplicaBasedDistributionDataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(notReplicaBasedDistributionDataNodeRuleAttribute.getDataNodesByTableName("foo_tbl")).thenReturn(Collections.singleton(new DataNode("foo_ds.foo_tbl")));
        DataNodeRuleAttribute replicaBasedDistributionDataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(replicaBasedDistributionDataNodeRuleAttribute.getDataNodesByTableName("bar_tbl")).thenReturn(Arrays.asList(new DataNode("foo_ds.bar_tbl"), new DataNode("bar_ds.bar_tbl")));
        when(replicaBasedDistributionDataNodeRuleAttribute.isReplicaBasedDistribution()).thenReturn(true);
        when(ruleMetaData.getAttributes(DataNodeRuleAttribute.class)).thenReturn(Arrays.asList(notReplicaBasedDistributionDataNodeRuleAttribute, replicaBasedDistributionDataNodeRuleAttribute));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        Map<String, List<DataNode>> actual = PipelineDataNodeUtils.buildTableAndDataNodesMap(database, Arrays.asList("foo_tbl", "bar_tbl"));
        assertThat(actual.size(), is(2));
        assertThat(actual.get("foo_tbl"), is(Collections.singletonList(new DataNode("foo_ds.foo_tbl"))));
        assertThat(actual.get("bar_tbl"), is(Collections.singletonList(new DataNode("foo_ds.bar_tbl"))));
    }
    
    @Test
    void assertBuildTableAndDataNodesMapWithNotExistedTable() {
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        when(ruleMetaData.getAttributes(DataNodeRuleAttribute.class)).thenReturn(Collections.emptyList());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        assertThrows(PipelineInvalidParameterException.class, () -> PipelineDataNodeUtils.buildTableAndDataNodesMap(database, Collections.singleton("foo_tbl")));
    }
}
