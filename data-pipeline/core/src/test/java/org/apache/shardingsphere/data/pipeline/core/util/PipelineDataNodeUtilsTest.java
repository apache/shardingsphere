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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PipelineDataNodeUtilsTest {
    
    @Test
    void assertBuildTableAndDataNodesMap() {
        DataNodeRuleAttribute notReplicaBasedDistributionDataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(notReplicaBasedDistributionDataNodeRuleAttribute.getDataNodesByTableName("foo_tbl")).thenReturn(Collections.singleton(new DataNode("foo_ds.foo_tbl")));
        DataNodeRuleAttribute replicaBasedDistributionDataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(replicaBasedDistributionDataNodeRuleAttribute.getDataNodesByTableName("foo_tbl")).thenReturn(Collections.singleton(new DataNode("bar_ds.foo_tbl")));
        when(replicaBasedDistributionDataNodeRuleAttribute.getDataNodesByTableName("bar_tbl")).thenReturn(Arrays.asList(new DataNode("foo_ds.bar_tbl"), new DataNode("bar_ds.bar_tbl")));
        when(replicaBasedDistributionDataNodeRuleAttribute.isDataNodeTableNameLoadedFromStorage()).thenReturn(true);
        when(replicaBasedDistributionDataNodeRuleAttribute.isReplicaBasedDistribution()).thenReturn(true);
        ShardingSphereDatabase database = mockDatabase(Arrays.asList(notReplicaBasedDistributionDataNodeRuleAttribute, replicaBasedDistributionDataNodeRuleAttribute));
        Map<String, Set<String>> schemaTableNames = Collections.singletonMap("", new LinkedHashSet<>(Arrays.asList("foo_tbl", "bar_tbl")));
        Map<String, List<DataNode>> actual = PipelineDataNodeUtils.buildTableAndDataNodesMap(database, schemaTableNames);
        assertThat(actual.size(), is(2));
        assertThat(actual.get("foo_tbl"), is(Collections.singletonList(new DataNode("foo_ds.foo_tbl"))));
        assertThat(actual.get("bar_tbl"), is(Collections.singletonList(new DataNode("foo_ds.bar_tbl"))));
        verify(replicaBasedDistributionDataNodeRuleAttribute, never()).getDataNodesByTableName("foo_tbl");
    }
    
    @Test
    void assertBuildTableAndDataNodesMapFiltersStorageLoadedDataNodesBeforeSelectingReplica() {
        DataNode mismatchedDataNode = new DataNode("foo_ds", "caseschema", "foo_tbl");
        DataNode matchedDataNode = new DataNode("bar_ds", "CaseSchema", "foo_tbl");
        DataNodeRuleAttribute ruleAttribute = mockRuleAttribute("foo_tbl", Arrays.asList(mismatchedDataNode, matchedDataNode), true, true);
        ShardingSphereDatabase database = mockDatabase(Collections.singleton(ruleAttribute));
        Map<String, List<DataNode>> actual = PipelineDataNodeUtils.buildTableAndDataNodesMap(
                database, Collections.singletonMap("CaseSchema", Collections.singleton("foo_tbl")));
        assertThat(actual, is(Collections.singletonMap("foo_tbl", Collections.singletonList(matchedDataNode))));
        Map<String, List<DataNode>> lowerCaseActual = PipelineDataNodeUtils.buildTableAndDataNodesMap(
                database, Collections.singletonMap("caseschema", Collections.singleton("foo_tbl")));
        assertThat(lowerCaseActual, is(Collections.singletonMap("foo_tbl", Collections.singletonList(mismatchedDataNode))));
    }
    
    @Test
    void assertBuildTableAndDataNodesMapDoesNotFilterOwnerNotLoadedFromStorage() {
        DataNode dataNode = new DataNode("foo_ds", "bar_schema", "foo_tbl");
        ShardingSphereDatabase database = mockDatabase(Collections.singleton(mockRuleAttribute("foo_tbl", Collections.singleton(dataNode), false, false)));
        Map<String, List<DataNode>> actual = PipelineDataNodeUtils.buildTableAndDataNodesMap(
                database, Collections.singletonMap("foo_schema", Collections.singleton("foo_tbl")));
        assertThat(actual, is(Collections.singletonMap("foo_tbl", Collections.singletonList(dataNode))));
    }
    
    @Test
    void assertBuildTableAndDataNodesMapDoesNotFallThroughWhenStorageLoadedOwnerHasNoMatchingSchema() {
        DataNodeRuleAttribute storageLoadedOwner = mockRuleAttribute(
                "foo_tbl", Collections.singleton(new DataNode("foo_ds", "bar_schema", "foo_tbl")), true, false);
        DataNodeRuleAttribute laterOwner = mock(DataNodeRuleAttribute.class);
        ShardingSphereDatabase database = mockDatabase(Arrays.asList(storageLoadedOwner, laterOwner));
        PipelineInvalidParameterException actual = assertThrows(PipelineInvalidParameterException.class, () -> PipelineDataNodeUtils.buildTableAndDataNodesMap(
                database, Collections.singletonMap("foo_schema", Collections.singleton("foo_tbl"))));
        assertThat(actual.getMessage(), is("There is invalid parameter value. Not find actual data nodes of `foo_tbl`"));
        verify(laterOwner, never()).getDataNodesByTableName("foo_tbl");
    }
    
    @Test
    void assertBuildTableAndDataNodesMapWithEmptySchemaTableNames() {
        PipelineInvalidParameterException actual = assertThrows(
                PipelineInvalidParameterException.class, () -> PipelineDataNodeUtils.buildTableAndDataNodesMap(mock(ShardingSphereDatabase.class), Collections.emptyMap()));
        assertThat(actual.getMessage(), is("There is invalid parameter value. Table names are empty."));
    }
    
    @Test
    void assertBuildTableAndDataNodesMapWithEmptyTableNames() {
        PipelineInvalidParameterException actual = assertThrows(PipelineInvalidParameterException.class, () -> PipelineDataNodeUtils.buildTableAndDataNodesMap(
                mock(ShardingSphereDatabase.class), Collections.singletonMap("foo_schema", Collections.emptySet())));
        assertThat(actual.getMessage(), is("There is invalid parameter value. Table names are empty."));
    }
    
    @Test
    void assertBuildTableAndDataNodesMapWithNotExistedTable() {
        PipelineInvalidParameterException actual = assertThrows(PipelineInvalidParameterException.class, () -> PipelineDataNodeUtils.buildTableAndDataNodesMap(
                mockDatabase(Collections.emptyList()), Collections.singletonMap("", Collections.singleton("foo_tbl"))));
        assertThat(actual.getMessage(), is("There is invalid parameter value. Not find actual data nodes of `foo_tbl`"));
    }
    
    @Test
    void assertBuildTableAndDataNodesMapWithDuplicateTableNameAcrossSchemas() {
        DataNodeRuleAttribute ruleAttribute = mockRuleAttribute("foo_tbl", Collections.singleton(new DataNode("foo_ds.foo_tbl")), false, false);
        ShardingSphereDatabase database = mockDatabase(Collections.singleton(ruleAttribute));
        Map<String, Set<String>> schemaTableNames = new LinkedHashMap<>(2, 1F);
        schemaTableNames.put("foo_schema", Collections.singleton("foo_tbl"));
        schemaTableNames.put("bar_schema", Collections.singleton("foo_tbl"));
        PipelineInvalidParameterException actual = assertThrows(
                PipelineInvalidParameterException.class, () -> PipelineDataNodeUtils.buildTableAndDataNodesMap(database, schemaTableNames));
        assertThat(actual.getMessage(), is("There is invalid parameter value. More than one schema table has the same table name `foo_tbl`."));
        verify(ruleAttribute).getDataNodesByTableName("foo_tbl");
    }
    
    @Test
    void assertBuildTableAndDataNodesMapDoesNotNormalizeTableNamesAcrossSchemas() {
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        DataNode fooDataNode = new DataNode("foo_ds.foo_tbl");
        DataNode upperCaseFooDataNode = new DataNode("foo_ds.FOO_TBL");
        when(ruleAttribute.getDataNodesByTableName("foo_tbl")).thenReturn(Collections.singleton(fooDataNode));
        when(ruleAttribute.getDataNodesByTableName("FOO_TBL")).thenReturn(Collections.singleton(upperCaseFooDataNode));
        ShardingSphereDatabase database = mockDatabase(Collections.singleton(ruleAttribute));
        Map<String, Set<String>> schemaTableNames = new LinkedHashMap<>(2, 1F);
        schemaTableNames.put("foo_schema", Collections.singleton("foo_tbl"));
        schemaTableNames.put("bar_schema", Collections.singleton("FOO_TBL"));
        Map<String, List<DataNode>> actual = PipelineDataNodeUtils.buildTableAndDataNodesMap(database, schemaTableNames);
        assertThat(actual.get("foo_tbl"), is(Collections.singletonList(fooDataNode)));
        assertThat(actual.get("FOO_TBL"), is(Collections.singletonList(upperCaseFooDataNode)));
    }
    
    private ShardingSphereDatabase mockDatabase(final Collection<DataNodeRuleAttribute> ruleAttributes) {
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        when(ruleMetaData.getAttributes(DataNodeRuleAttribute.class)).thenReturn(ruleAttributes);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
    
    private DataNodeRuleAttribute mockRuleAttribute(final String tableName, final Collection<DataNode> dataNodes,
                                                    final boolean tableNameLoadedFromStorage, final boolean replicaBasedDistribution) {
        DataNodeRuleAttribute result = mock(DataNodeRuleAttribute.class);
        when(result.getDataNodesByTableName(tableName)).thenReturn(dataNodes);
        if (tableNameLoadedFromStorage) {
            when(result.isDataNodeTableNameLoadedFromStorage()).thenReturn(true);
        }
        if (replicaBasedDistribution) {
            when(result.isReplicaBasedDistribution()).thenReturn(true);
        }
        return result;
    }
}
