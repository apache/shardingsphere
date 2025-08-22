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

package org.apache.shardingsphere.sharding.metadata;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEngine;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingMetaDataReviseEngineTest {
    
    @Test
    void assertReviseWithKeyGenerateStrategy() {
        GenericSchemaBuilderMaterial material = mock(GenericSchemaBuilderMaterial.class);
        when(material.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        Map<String, ShardingSphereSchema> actual = new MetaDataReviseEngine(Collections.singleton(mockShardingRule()))
                .revise(Collections.singletonMap("sharding_db", new SchemaMetaData("sharding_db", Collections.singleton(createTableMetaData()))), material);
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("sharding_db"));
        assertThat(actual.get("sharding_db").getAllTables().size(), is(1));
        ShardingSphereTable table = actual.get("sharding_db").getAllTables().iterator().next();
        Iterator<ShardingSphereColumn> columns = table.getAllColumns().iterator();
        assertTrue(columns.next().isGenerated());
        assertFalse(columns.next().isGenerated());
        assertFalse(columns.next().isGenerated());
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        when(ruleAttribute.findLogicTableByActualTable("t_order")).thenReturn(Optional.of("t_order"));
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
    
    private TableMetaData createTableMetaData() {
        Collection<ColumnMetaData> columns = Arrays.asList(new ColumnMetaData("id", Types.INTEGER, true, true, true, true, false, false),
                new ColumnMetaData("pwd_cipher", Types.VARCHAR, false, false, true, true, false, false),
                new ColumnMetaData("product_id", Types.INTEGER, false, false, true, true, false, false));
        return new TableMetaData("t_order", columns, Collections.emptyList(), Collections.emptyList());
    }
}
