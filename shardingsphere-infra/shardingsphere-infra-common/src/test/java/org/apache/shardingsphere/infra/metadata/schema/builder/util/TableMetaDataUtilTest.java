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

package org.apache.shardingsphere.infra.metadata.schema.builder.util;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public final class TableMetaDataUtilTest {

    @Mock
    private SchemaBuilderMaterials materials;

    @Mock
    private DataSourceContainedRule dataSourceContainedRule;

    @Mock
    private DataNodeContainedRule dataNodeContainedRule;

    @Mock
    private DataSource dataSource1;

    @Mock
    private DataSource dataSource2;

    @Test
    public void assertGetTableMetaDataLoadMaterial() {
        Map<String, Collection<DataNode>> dataNodes = new HashMap<>();
        dataNodes.put("t_user", Arrays.asList(new DataNode("ds0.t_user")));
        dataNodes.put("t_order", Arrays.asList(new DataNode("ds1.t_order")));
        when(dataNodeContainedRule.getAllDataNodes()).thenReturn(dataNodes);
        Map<String, Collection<String>> dataSourceMapper = new HashMap<>();
        when(dataSourceContainedRule.getDataSourceMapper()).thenReturn(dataSourceMapper);
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        rules.add(dataNodeContainedRule);
        rules.add(dataSourceContainedRule);
        when(materials.getRules()).thenReturn(rules);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds0", dataSource1);
        dataSourceMap.put("ds1", dataSource2);
        when(materials.getDataSourceMap()).thenReturn(dataSourceMap);
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add("t_user");
        Collection<TableMetaDataLoaderMaterial> results = TableMetaDataUtil.getTableMetaDataLoadMaterial(tableNames, materials, false);
        assertThat(results.size(), is(1));
    }
}
