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

package org.apache.shardingsphere.singletable.metadata;

import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.RuleBasedSchemaMetaDataDecoratorFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class SingleTableSchemaMetaDataDecoratorTest {
    
    private static final String TABLE_NAME = "t_single";
    
    @Test
    public void assertDecorate() {
        SingleTableRule singleTableRule = mock(SingleTableRule.class);
        Collection<ShardingSphereRule> rules = Collections.singletonList(singleTableRule);
        SingleTableSchemaMetaDataDecorator builder = (SingleTableSchemaMetaDataDecorator) RuleBasedSchemaMetaDataDecoratorFactory.getInstances(rules).get(singleTableRule);
        Map<String, SchemaMetaData> schemaMetaDataMap = mockSchemaMetaDataMap();
        TableMetaData tableMetaData = builder.decorate(schemaMetaDataMap, singleTableRule, mock(GenericSchemaBuilderMaterials.class)).get("sharding_db").getTables().iterator().next();
        Iterator<ColumnMetaData> columnsIterator = tableMetaData.getColumns().iterator();
        assertThat(columnsIterator.next(), is(new ColumnMetaData("id", 4, true, false, false, true)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("name", 12, false, false, false, true)));
        assertThat(columnsIterator.next(), is(new ColumnMetaData("doc", -1, false, false, false, true)));
        assertThat(tableMetaData.getIndexes().size(), is(2));
        Iterator<IndexMetaData> indexesIterator = tableMetaData.getIndexes().iterator();
        assertThat(indexesIterator.next(), is(new IndexMetaData("id")));
        assertThat(indexesIterator.next(), is(new IndexMetaData("idx_name")));
    }
    
    private Map<String, SchemaMetaData> mockSchemaMetaDataMap() {
        Collection<ColumnMetaData> columns = Arrays.asList(new ColumnMetaData("id", 4, true, false, false, true),
                new ColumnMetaData("name", 12, false, false, false, true),
                new ColumnMetaData("doc", -1, false, false, false, true));
        Collection<IndexMetaData> indexMetaDataList = Arrays.asList(new IndexMetaData("id_" + TABLE_NAME), new IndexMetaData("idx_name_" + TABLE_NAME));
        Collection<TableMetaData> tableMetaDataList = new LinkedList<>();
        tableMetaDataList.add(new TableMetaData(TABLE_NAME, columns, indexMetaDataList, Collections.emptyList()));
        return Collections.singletonMap("sharding_db", new SchemaMetaData("sharding_db", tableMetaDataList, Collections.emptyList()));
    }
}
