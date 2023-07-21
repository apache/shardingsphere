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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.SchemaMetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
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
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        when(dataNodeContainedRule.getDataNodesByTableName("t_order")).thenReturn(mockShardingDataNodes());
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(mock(DatabaseType.class), Collections.emptyMap(), mockDataSourceMap(),
                Arrays.asList(dataNodeContainedRule, mock(DataSourceContainedRule.class)), mock(ConfigurationProperties.class), "sharding_db");
        Collection<SchemaMetaDataLoaderMaterial> actual = SchemaMetaDataUtils.getSchemaMetaDataLoaderMaterials(Collections.singleton("t_order"), material, true);
        assertThat(actual.size(), is(2));
        Iterator<SchemaMetaDataLoaderMaterial> iterator = actual.iterator();
        SchemaMetaDataLoaderMaterial firstMaterial = iterator.next();
        assertThat(firstMaterial.getDefaultSchemaName(), is("sharding_db"));
        assertThat(firstMaterial.getActualTableNames(), is(Collections.singletonList("t_order_0")));
        SchemaMetaDataLoaderMaterial secondMaterial = iterator.next();
        assertThat(secondMaterial.getDefaultSchemaName(), is("sharding_db"));
        assertThat(secondMaterial.getActualTableNames(), is(Collections.singletonList("t_order_1")));
    }
    
    @Test
    void assertGetSchemaMetaDataLoaderMaterialsWhenNotConfigCheckMetaDataEnable() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        when(dataNodeContainedRule.getDataNodesByTableName("t_order")).thenReturn(mockShardingDataNodes());
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(mock(DatabaseType.class), Collections.emptyMap(), mockDataSourceMap(),
                Arrays.asList(dataNodeContainedRule, mock(DataSourceContainedRule.class)), mock(ConfigurationProperties.class), "sharding_db");
        Collection<SchemaMetaDataLoaderMaterial> actual = SchemaMetaDataUtils.getSchemaMetaDataLoaderMaterials(Collections.singleton("t_order"), material, false);
        assertThat(actual.size(), is(1));
        Iterator<SchemaMetaDataLoaderMaterial> iterator = actual.iterator();
        SchemaMetaDataLoaderMaterial firstMaterial = iterator.next();
        assertThat(firstMaterial.getDefaultSchemaName(), is("sharding_db"));
        assertThat(firstMaterial.getActualTableNames(), is(Collections.singletonList("t_order_0")));
    }
    
    @Test
    void assertGetSchemaMetaDataLoaderMaterialsWhenNotConfigCheckMetaDataEnableForSingleTableDataNode() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        when(dataNodeContainedRule.getDataNodesByTableName("t_single")).thenReturn(mockSingleTableDataNodes());
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(mock(DatabaseType.class), Collections.emptyMap(), mockDataSourceMap(),
                Arrays.asList(dataNodeContainedRule, mock(DataSourceContainedRule.class)), mock(ConfigurationProperties.class), "public");
        Collection<SchemaMetaDataLoaderMaterial> actual = SchemaMetaDataUtils.getSchemaMetaDataLoaderMaterials(Collections.singleton("t_single"), material, false);
        assertThat(actual.size(), is(1));
        Iterator<SchemaMetaDataLoaderMaterial> iterator = actual.iterator();
        SchemaMetaDataLoaderMaterial firstMaterial = iterator.next();
        assertThat(firstMaterial.getDefaultSchemaName(), is("public"));
        assertThat(firstMaterial.getActualTableNames(), is(Collections.singletonList("t_single")));
    }
    
    private Collection<DataNode> mockShardingDataNodes() {
        return Arrays.asList(new DataNode("ds_0.t_order_0"), new DataNode("ds_1.t_order_1"));
    }
    
    private List<DataNode> mockSingleTableDataNodes() {
        DataNode firstDataNode = new DataNode("ds_0.t_single");
        firstDataNode.setSchemaName("public");
        DataNode secondDataNode = new DataNode("ds_0.t_single");
        secondDataNode.setSchemaName("test");
        return Arrays.asList(firstDataNode, secondDataNode);
    }
    
    private Map<String, DataSource> mockDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1F);
        result.put("ds_0", new MockedDataSource());
        result.put("ds_1", new MockedDataSource());
        return result;
    }
}
