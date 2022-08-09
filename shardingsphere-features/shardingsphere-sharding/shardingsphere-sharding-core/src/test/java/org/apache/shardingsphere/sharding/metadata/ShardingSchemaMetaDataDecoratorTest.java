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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.RuleBasedSchemaMetaDataDecoratorFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSchemaMetaDataDecoratorTest {
    
    private static final String TABLE_NAME = "t_order";
    
    @Test
    public void assertDecorateWithKeyGenerateStrategy() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        when(shardingRule.findLogicTableByActualTable(TABLE_NAME)).thenReturn(Optional.of(TABLE_NAME));
        Collection<ShardingSphereRule> rules = Collections.singletonList(shardingRule);
        ShardingSchemaMetaDataDecorator builder = (ShardingSchemaMetaDataDecorator) RuleBasedSchemaMetaDataDecoratorFactory.getInstances(rules).get(shardingRule);
        Collection<TableMetaData> tableMetaDataList = new LinkedList<>();
        tableMetaDataList.add(createTableMetaData());
        GenericSchemaBuilderMaterials materials = mock(GenericSchemaBuilderMaterials.class);
        when(materials.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        Map<String, SchemaMetaData> actual = builder.decorate(Collections.singletonMap("sharding_db",
                new SchemaMetaData("sharding_db", tableMetaDataList)), shardingRule, materials);
        Collection<ColumnMetaData> columns = actual.get("sharding_db").getTables().iterator().next().getColumns();
        Iterator<ColumnMetaData> iterator = columns.iterator();
        assertTrue(iterator.next().isGenerated());
        assertFalse(iterator.next().isGenerated());
        assertFalse(iterator.next().isGenerated());
        assertFalse(iterator.next().isGenerated());
    }
    
    private TableMetaData createTableMetaData() {
        Collection<ColumnMetaData> columns = Arrays.asList(new ColumnMetaData("id", 1, true, true, true, true),
                new ColumnMetaData("pwd_cipher", 2, false, false, true, true),
                new ColumnMetaData("pwd_plain", 2, false, false, true, true),
                new ColumnMetaData("product_id", 2, false, false, true, true));
        return new TableMetaData(TABLE_NAME, columns, Collections.emptyList(), Collections.emptyList());
    }
}
