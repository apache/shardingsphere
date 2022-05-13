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

package org.apache.shardingsphere.infra.metadata.schema.util;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class IndexMetaDataUtilTest {
    
    private static final String TABLE_NAME = "t_order";
    
    private static final String INDEX_NAME = "user_id_idx";
    
    @Test
    public void assertGetLogicIndexNameWithIndexNameSuffix() {
        assertThat(IndexMetaDataUtil.getLogicIndexName("order_index_t_order", "t_order"), is("order_index"));
    }
    
    @Test
    public void assertGetLogicIndexNameWithMultiIndexNameSuffix() {
        assertThat(IndexMetaDataUtil.getLogicIndexName("order_t_order_index_t_order", "t_order"), is("order_t_order_index"));
    }
    
    @Test
    public void assertGetLogicIndexNameWithoutIndexNameSuffix() {
        assertThat(IndexMetaDataUtil.getLogicIndexName("order_index", "t_order"), is("order_index"));
    }
    
    @Test
    public void assertGetActualIndexNameWithActualTableName() {
        assertThat(IndexMetaDataUtil.getActualIndexName("order_index", "t_order"), is("order_index_t_order"));
    }
    
    @Test
    public void assertGetActualIndexNameWithoutActualTableName() {
        assertThat(IndexMetaDataUtil.getActualIndexName("order_index", null), is("order_index"));
    }
    
    @Test
    public void assertGetGeneratedLogicIndexName() {
        ColumnSegment userIdColumnSegment = new ColumnSegment(0, 0, new IdentifierValue("user_id"));
        ColumnSegment userNameColumnSegment = new ColumnSegment(0, 0, new IdentifierValue("user_name"));
        assertThat(IndexMetaDataUtil.getGeneratedLogicIndexName(Arrays.asList(userIdColumnSegment, userNameColumnSegment)), is("user_id_user_name_idx"));
    }
    
    @Test
    public void assertGetTableNamesFromMetaData() {
        IndexSegment indexSegment = new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue(INDEX_NAME)));
        Collection<QualifiedTable> actual = IndexMetaDataUtil.getTableNamesFromMetaData(buildMetaData(), Lists.newArrayList(indexSegment), new MySQLDatabaseType());
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getSchemaName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.iterator().next().getTableName(), is(TABLE_NAME));
    }
    
    private ShardingSphereMetaData buildMetaData() {
        TableMetaData tableMetaData = new TableMetaData(TABLE_NAME, Collections.emptyList(), Collections.singletonList(new IndexMetaData(INDEX_NAME)), Collections.emptyList());
        Map<String, TableMetaData> tables = new HashMap<>(1, 1);
        tables.put(TABLE_NAME, tableMetaData);
        Map<String, ShardingSphereSchema> schemas = Collections.singletonMap(DefaultDatabase.LOGIC_NAME, new ShardingSphereSchema(tables));
        return new ShardingSphereMetaData(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), mock(ShardingSphereResource.class), mock(ShardingSphereRuleMetaData.class), schemas);
    }
}
