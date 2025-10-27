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

package org.apache.shardingsphere.infra.metadata.database.schema.util;

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SchemaMetaDataUtilsTest {
    
    @Test
    void assertGetSchemaMetaDataLoaderMaterialsWhenConfigCheckMetaDataEnable() {
        ShardingSphereRule rule0 = mock(ShardingSphereRule.class);
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        when(ruleAttribute.getDataNodesByTableName("t_order")).thenReturn(mockShardingDataNodes());
        when(rule0.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        ShardingSphereRule rule1 = mock(ShardingSphereRule.class);
        when(rule1.getAttributes()).thenReturn(new RuleAttributes());
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        when(props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED)).thenReturn(true);
        when(props.getValue(ConfigurationPropertyKey.LOAD_TABLE_METADATA_BATCH_SIZE)).thenReturn(100);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(mockStorageUnits(), Arrays.asList(rule0, rule1), props, "sharding_db");
        Collection<MetaDataLoaderMaterial> actual = SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("t_order"), material);
        assertThat(actual.size(), is(2));
        Iterator<MetaDataLoaderMaterial> iterator = actual.iterator();
        MetaDataLoaderMaterial firstMaterial = iterator.next();
        assertThat(firstMaterial.getDefaultSchemaName(), is("sharding_db"));
        assertThat(firstMaterial.getActualTableNames(), is(Collections.singletonList("t_order_0")));
        MetaDataLoaderMaterial secondMaterial = iterator.next();
        assertThat(secondMaterial.getDefaultSchemaName(), is("sharding_db"));
        assertThat(secondMaterial.getActualTableNames(), is(Collections.singletonList("t_order_1")));
    }
    
    @Test
    void assertGetSchemaMetaDataLoaderMaterialsWhenNotConfigCheckMetaDataEnable() {
        ShardingSphereRule rule0 = mock(ShardingSphereRule.class);
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        when(ruleAttribute.getDataNodesByTableName("t_order")).thenReturn(mockShardingDataNodes());
        when(rule0.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        ShardingSphereRule rule1 = mock(ShardingSphereRule.class);
        when(rule1.getAttributes()).thenReturn(new RuleAttributes());
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        when(props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED)).thenReturn(false);
        when(props.getValue(ConfigurationPropertyKey.LOAD_TABLE_METADATA_BATCH_SIZE)).thenReturn(100);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(mockStorageUnits(), Arrays.asList(rule0, rule1), props, "sharding_db");
        Collection<MetaDataLoaderMaterial> actual = SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("t_order"), material);
        assertThat(actual.size(), is(1));
        Iterator<MetaDataLoaderMaterial> iterator = actual.iterator();
        MetaDataLoaderMaterial firstMaterial = iterator.next();
        assertThat(firstMaterial.getDefaultSchemaName(), is("sharding_db"));
        assertThat(firstMaterial.getActualTableNames(), is(Collections.singletonList("t_order_0")));
    }
    
    @Test
    void assertGetSchemaMetaDataLoaderMaterialsWhenNotConfigCheckMetaDataEnableForSingleTableDataNode() {
        ShardingSphereRule rule0 = mock(ShardingSphereRule.class);
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        when(ruleAttribute.getDataNodesByTableName("t_single")).thenReturn(mockSingleTableDataNodes());
        when(rule0.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        ShardingSphereRule rule1 = mock(ShardingSphereRule.class);
        when(rule1.getAttributes()).thenReturn(new RuleAttributes());
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        when(props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED)).thenReturn(false);
        when(props.getValue(ConfigurationPropertyKey.LOAD_TABLE_METADATA_BATCH_SIZE)).thenReturn(100);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(mockStorageUnits(), Arrays.asList(rule0, rule1), props, "public");
        Collection<MetaDataLoaderMaterial> actual = SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("t_single"), material);
        assertThat(actual.size(), is(1));
        Iterator<MetaDataLoaderMaterial> iterator = actual.iterator();
        MetaDataLoaderMaterial firstMaterial = iterator.next();
        assertThat(firstMaterial.getDefaultSchemaName(), is("public"));
        assertThat(firstMaterial.getActualTableNames(), is(Collections.singletonList("t_single")));
    }
    
    private Collection<DataNode> mockShardingDataNodes() {
        return Arrays.asList(new DataNode("ds_0.t_order_0"), new DataNode("ds_1.t_order_1"));
    }
    
    private List<DataNode> mockSingleTableDataNodes() {
        DataNode firstDataNode = new DataNode("ds_0", "public", "t_single");
        DataNode secondDataNode = new DataNode("ds_0", "test", "t_single");
        return Arrays.asList(firstDataNode, secondDataNode);
    }
    
    private Map<String, StorageUnit> mockStorageUnits() {
        Map<String, StorageUnit> result = new HashMap<>(2, 1F);
        StorageUnit storageUnit1 = mock(StorageUnit.class);
        when(storageUnit1.getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(storageUnit1.getDataSource()).thenReturn(new MockedDataSource());
        result.put("ds_0", storageUnit1);
        StorageUnit storageUnit2 = mock(StorageUnit.class);
        when(storageUnit2.getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(storageUnit2.getDataSource()).thenReturn(new MockedDataSource());
        result.put("ds_1", storageUnit2);
        return result;
    }
}
