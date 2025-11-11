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
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SchemaMetaDataUtilsTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetMetaDataLoaderMaterialsWhenConfigCheckMetaDataEnable() {
        ConfigurationProperties props = new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.TRUE.toString())));
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(
                mockStorageUnits(), Arrays.asList(mockDataNodeRule(Arrays.asList(new DataNode("ds_0.foo_tbl_0"), new DataNode("ds_1.foo_tbl_1"))),
                        mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)),
                props, "foo_db");
        List<MetaDataLoaderMaterial> actual = new ArrayList<>(SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("foo_tbl"), material));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getDefaultSchemaName(), is("foo_db"));
        assertThat(actual.get(0).getActualTableNames(), is(Collections.singletonList("foo_tbl_0")));
        assertThat(actual.get(1).getDefaultSchemaName(), is("foo_db"));
        assertThat(actual.get(1).getActualTableNames(), is(Collections.singletonList("foo_tbl_1")));
    }
    
    @Test
    void assertGetMetaDataLoaderMaterialsWhenNotConfigCheckMetaDataEnable() {
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(
                mockStorageUnits(), Arrays.asList(mockDataNodeRule(Arrays.asList(new DataNode("ds_0.foo_tbl_0"), new DataNode("ds_1.foo_tbl_1"))),
                        mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)),
                new ConfigurationProperties(new Properties()), "foo_db");
        List<MetaDataLoaderMaterial> actual = new ArrayList<>(SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("foo_tbl"), material));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getDefaultSchemaName(), is("foo_db"));
        assertThat(actual.get(0).getActualTableNames(), is(Collections.singletonList("foo_tbl_0")));
    }
    
    @Test
    void assertGetMetaDataLoaderMaterialsWhenEmptyDataNodesAndNotEmptyStorageUnits() {
        ConfigurationProperties props = new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.TRUE.toString())));
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(mockStorageUnits(), Collections.singleton(mockDataNodeRule(Collections.emptyList())), props, "foo_db");
        Collection<MetaDataLoaderMaterial> actual = SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("foo_tbl"), material);
        assertThat(actual.size(), is(1));
        Iterator<MetaDataLoaderMaterial> iterator = actual.iterator();
        MetaDataLoaderMaterial firstMaterial = iterator.next();
        assertThat(firstMaterial.getStorageUnitName(), is("ds.with.dot"));
        assertThat(firstMaterial.getActualTableNames(), is(Collections.singletonList("foo_tbl")));
    }
    
    @Test
    void assertGetMetaDataLoaderMaterialsWithDifferentSchemaName() {
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(mockStorageUnits(),
                Collections.singleton(mockDataNodeRule(Collections.singleton(new DataNode("ds_0", "different_schema", "foo_tbl")))),
                new ConfigurationProperties(new Properties()), "default_schema");
        Collection<MetaDataLoaderMaterial> actual = SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("foo_tbl"), material);
        assertThat(actual.size(), is(1));
        Iterator<MetaDataLoaderMaterial> iterator = actual.iterator();
        MetaDataLoaderMaterial firstMaterial = iterator.next();
        assertThat(firstMaterial.getActualTableNames(), is(Collections.singletonList("foo_tbl")));
    }
    
    @Test
    void assertGetMetaDataLoaderMaterialsWithEmptyStorageUnitsAndCheckTableMetadataEnabled() {
        ConfigurationProperties props = new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.TRUE.toString())));
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(Collections.emptyMap(), Collections.singleton(mockDataNodeRule(Collections.emptyList())), props, "foo_db");
        assertTrue(SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("foo_tbl"), material).isEmpty());
    }
    
    @Test
    void assertGetMetaDataLoaderMaterialsWithEmptyDataNodesAndCheckTableMetadataDisabled() {
        ConfigurationProperties props = new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.FALSE.toString())));
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(Collections.emptyMap(), Collections.singleton(mockDataNodeRule(Collections.emptyList())), props, "foo_db");
        assertTrue(SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("foo_tbl"), material).isEmpty());
    }
    
    private Map<String, StorageUnit> mockStorageUnits() {
        Map<String, StorageUnit> result = new LinkedHashMap<>(4, 1F);
        result.put("ds.with.dot", mockStorageUnit());
        result.put("ds", mockStorageUnit());
        result.put("ds_0", mockStorageUnit());
        result.put("ds_1", mockStorageUnit());
        return result;
    }
    
    private StorageUnit mockStorageUnit() {
        StorageUnit result = mock(StorageUnit.class);
        when(result.getStorageType()).thenReturn(databaseType);
        return result;
    }
    
    private ShardingSphereRule mockDataNodeRule(final Collection<DataNode> dataNodes) {
        ShardingSphereRule result = mock(ShardingSphereRule.class);
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        when(ruleAttribute.getDataNodesByTableName("foo_tbl")).thenReturn(dataNodes);
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
}
