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
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class IndexMetaDataUtilTest {
    
    private static final String TABLE_NAME = "t_order";
    
    private static final String INDEX_NAME = "user_id_idx";
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource;
    
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
    @SneakyThrows(SQLException.class)
    public void assertGetTableNamesFromMetaData() {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection()).thenReturn(connection);
        ShardingSphereSchema schema = buildSchema();
        IndexSegment indexSegment = new IndexSegment(0, 0, new IdentifierValue(TABLE_NAME));
        assertThat(IndexMetaDataUtil.getTableNamesFromMetaData(schema, Lists.newArrayList(indexSegment)), is(Collections.singletonList(TABLE_NAME)));
    }
    
    private ShardingSphereSchema buildSchema() {
        TableMetaData tableMetaData = new TableMetaData(TABLE_NAME, Collections.emptyList(), Collections.singletonList(new IndexMetaData(INDEX_NAME)), Collections.emptyList());
        Map<String, TableMetaData> tables = new HashMap<>(1, 1);
        tables.put(TABLE_NAME, tableMetaData);
        return new ShardingSphereSchema(tables);
    }
}
