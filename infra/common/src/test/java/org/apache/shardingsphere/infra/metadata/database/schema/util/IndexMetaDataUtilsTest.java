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

import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class IndexMetaDataUtilsTest {
    
    private static final String TABLE_NAME = "t_order";
    
    private static final String INDEX_NAME = "user_id_idx";
    
    @Test
    void assertGetLogicIndexNameWithIndexNameSuffix() {
        assertThat(IndexMetaDataUtils.getLogicIndexName("order_index_t_order", "t_order"), is("order_index"));
    }
    
    @Test
    void assertGetLogicIndexNameWithMultiIndexNameSuffix() {
        assertThat(IndexMetaDataUtils.getLogicIndexName("order_t_order_index_t_order", "t_order"), is("order_t_order_index"));
    }
    
    @Test
    void assertGetLogicIndexNameWithoutIndexNameSuffix() {
        assertThat(IndexMetaDataUtils.getLogicIndexName("order_index", "t_order"), is("order_index"));
    }
    
    @Test
    void assertGetActualIndexNameWithActualTableName() {
        assertThat(IndexMetaDataUtils.getActualIndexName("order_index", "t_order"), is("order_index_t_order"));
    }
    
    @Test
    void assertGetActualIndexNameWithoutActualTableName() {
        assertThat(IndexMetaDataUtils.getActualIndexName("order_index", null), is("order_index"));
    }
    
    @Test
    void assertGetGeneratedLogicIndexName() {
        ColumnSegment userIdColumnSegment = new ColumnSegment(0, 0, new IdentifierValue("user_id"));
        ColumnSegment userNameColumnSegment = new ColumnSegment(0, 0, new IdentifierValue("user_name"));
        assertThat(IndexMetaDataUtils.getGeneratedLogicIndexName(Arrays.asList(userIdColumnSegment, userNameColumnSegment)), is("user_id_user_name_idx"));
    }
    
    @Test
    void assertGetTableNames() {
        IndexSegment indexSegment = new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue(INDEX_NAME)));
        Collection<QualifiedTable> actual = IndexMetaDataUtils.getTableNames(buildDatabase(), new MySQLDatabaseType(), Collections.singleton(indexSegment));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getSchemaName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.iterator().next().getTableName(), is(TABLE_NAME));
    }
    
    private ShardingSphereDatabase buildDatabase() {
        ShardingSphereTable table = new ShardingSphereTable(TABLE_NAME, Collections.emptyList(), Collections.singleton(new ShardingSphereIndex(INDEX_NAME)), Collections.emptyList());
        Map<String, ShardingSphereTable> tables = Collections.singletonMap(TABLE_NAME, table);
        Map<String, ShardingSphereSchema> schemas = Collections.singletonMap(DefaultDatabase.LOGIC_NAME, new ShardingSphereSchema(tables, Collections.emptyMap()));
        return new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class), mock(ShardingSphereRuleMetaData.class), schemas);
    }
}
