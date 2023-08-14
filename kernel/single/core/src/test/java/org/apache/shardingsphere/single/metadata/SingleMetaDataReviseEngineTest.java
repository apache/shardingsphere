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

package org.apache.shardingsphere.single.metadata;

import org.apache.shardingsphere.infra.database.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEngine;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class SingleMetaDataReviseEngineTest {
    
    private static final String TABLE_NAME = "t_single";
    
    @Test
    void assertRevise() {
        Map<String, SchemaMetaData> schemaMetaDataMap = Collections.singletonMap("sharding_db", new SchemaMetaData("sharding_db", Collections.singleton(createTableMetaData())));
        TableMetaData tableMetaData = new MetaDataReviseEngine(Collections.singleton(mock(SingleRule.class))).revise(
                schemaMetaDataMap, mock(GenericSchemaBuilderMaterial.class)).get("sharding_db").getTables().iterator().next();
        Iterator<ColumnMetaData> columns = tableMetaData.getColumns().iterator();
        assertThat(columns.next(), is(new ColumnMetaData("id", Types.INTEGER, true, false, false, true, false, true)));
        assertThat(columns.next(), is(new ColumnMetaData("name", Types.VARCHAR, false, false, false, true, false, false)));
        assertThat(columns.next(), is(new ColumnMetaData("doc", Types.LONGVARCHAR, false, false, false, true, false, false)));
        assertThat(tableMetaData.getIndexes().size(), is(2));
        Iterator<IndexMetaData> indexes = tableMetaData.getIndexes().iterator();
        assertThat(indexes.next(), is(new IndexMetaData("id")));
        assertThat(indexes.next(), is(new IndexMetaData("idx_name")));
    }
    
    private TableMetaData createTableMetaData() {
        Collection<ColumnMetaData> columns = Arrays.asList(new ColumnMetaData("id", Types.INTEGER, true, false, false, true, false, true),
                new ColumnMetaData("name", Types.VARCHAR, false, false, false, true, false, false),
                new ColumnMetaData("doc", Types.LONGVARCHAR, false, false, false, true, false, false));
        Collection<IndexMetaData> indexMetaDataList = Arrays.asList(new IndexMetaData("id_" + TABLE_NAME), new IndexMetaData("idx_name_" + TABLE_NAME));
        return new TableMetaData(TABLE_NAME, columns, indexMetaDataList, Collections.emptyList());
    }
}
