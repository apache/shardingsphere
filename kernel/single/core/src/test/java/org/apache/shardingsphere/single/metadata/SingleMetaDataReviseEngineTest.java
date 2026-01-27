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

import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SingleMetaDataReviseEngineTest {
    
    private static final String TABLE_NAME = "t_single";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertRevise() {
        Map<String, SchemaMetaData> schemaMetaDataMap = Collections.singletonMap("sharding_db", new SchemaMetaData("sharding_db", Collections.singleton(createTableMetaData())));
        Map<String, ShardingSphereSchema> actual = new MetaDataReviseEngine(Collections.singleton(mock(SingleRule.class)), databaseType)
                .revise(schemaMetaDataMap, mock(GenericSchemaBuilderMaterial.class));
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("sharding_db"));
        assertThat(actual.get("sharding_db").getAllTables().size(), is(1));
        ShardingSphereTable table = actual.get("sharding_db").getAllTables().iterator().next();
        Iterator<ShardingSphereColumn> columns = table.getAllColumns().iterator();
        assertShardingSphereColumn(columns.next(), new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, true));
        assertShardingSphereColumn(columns.next(), new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, true, false, false));
        assertShardingSphereColumn(columns.next(), new ShardingSphereColumn("doc", Types.LONGVARCHAR, false, false, false, true, false, false));
        assertThat(table.getAllIndexes().size(), is(2));
        Iterator<ShardingSphereIndex> indexes = table.getAllIndexes().iterator();
        assertShardingSphereIndex(indexes.next(), new ShardingSphereIndex("id", Collections.emptyList(), false));
        assertShardingSphereIndex(indexes.next(), new ShardingSphereIndex("idx_name", Collections.emptyList(), false));
    }
    
    private TableMetaData createTableMetaData() {
        Collection<ColumnMetaData> columns = Arrays.asList(new ColumnMetaData("id", Types.INTEGER, true, false, false, true, false, true),
                new ColumnMetaData("name", Types.VARCHAR, false, false, false, true, false, false),
                new ColumnMetaData("doc", Types.LONGVARCHAR, false, false, false, true, false, false));
        Collection<IndexMetaData> indexMetaDataList = Arrays.asList(new IndexMetaData("id"), new IndexMetaData("idx_name"));
        return new TableMetaData(TABLE_NAME, columns, indexMetaDataList, Collections.emptyList());
    }
    
    private void assertShardingSphereColumn(final ShardingSphereColumn actual, final ShardingSphereColumn expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getDataType(), is(expected.getDataType()));
        assertThat(actual.isPrimaryKey(), is(expected.isPrimaryKey()));
        assertThat(actual.isGenerated(), is(expected.isGenerated()));
        assertThat(actual.isCaseSensitive(), is(expected.isCaseSensitive()));
        assertThat(actual.isVisible(), is(expected.isVisible()));
        assertThat(actual.isUnsigned(), is(expected.isUnsigned()));
        assertThat(actual.isNullable(), is(expected.isNullable()));
    }
    
    private void assertShardingSphereIndex(final ShardingSphereIndex actual, final ShardingSphereIndex expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getColumns(), is(expected.getColumns()));
        assertThat(actual.isUnique(), is(expected.isUnique()));
    }
}
