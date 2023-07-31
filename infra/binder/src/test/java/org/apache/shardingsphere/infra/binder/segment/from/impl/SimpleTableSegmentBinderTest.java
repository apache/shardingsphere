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

package org.apache.shardingsphere.infra.binder.segment.from.impl;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimpleTableSegmentBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBind() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("t_order")));
        ShardingSphereMetaData metaData = createMetaData();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        SimpleTableSegment actual = SimpleTableSegmentBinder.bind(simpleTableSegment, metaData, DefaultDatabase.LOGIC_NAME, databaseType, tableBinderContexts);
        assertThat(actual.getTableName().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getTableName().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertTrue(tableBinderContexts.containsKey("t_order"));
        assertThat(tableBinderContexts.get("t_order").getProjectionSegments().size(), is(3));
        assertTrue(tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("user_id") instanceof ColumnProjectionSegment);
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("user_id")).getColumn().getOriginalDatabase().getValue(),
                is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("user_id")).getColumn().getOriginalSchema().getValue(),
                is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("user_id")).getColumn().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("user_id")).getColumn().getOriginalColumn().getValue(), is("user_id"));
        assertTrue(tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("order_id") instanceof ColumnProjectionSegment);
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("order_id")).getColumn().getOriginalDatabase().getValue(),
                is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("order_id")).getColumn().getOriginalSchema().getValue(),
                is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("order_id")).getColumn().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("order_id")).getColumn().getOriginalColumn().getValue(), is("order_id"));
        assertTrue(tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("status") instanceof ColumnProjectionSegment);
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("status")).getColumn().getOriginalDatabase().getValue(),
                is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("status")).getColumn().getOriginalSchema().getValue(),
                is(DefaultDatabase.LOGIC_NAME));
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("status")).getColumn().getOriginalTable().getValue(), is("t_order"));
        assertThat(((ColumnProjectionSegment) tableBinderContexts.get("t_order").getProjectionSegmentByColumnLabel("status")).getColumn().getOriginalColumn().getValue(), is("status"));
    }
    
    @Test
    void assertBindWithSchemaForMySQL() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("t_order")));
        simpleTableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("sharding_db")));
        ShardingSphereMetaData metaData = createMetaData();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        SimpleTableSegment actual = SimpleTableSegmentBinder.bind(simpleTableSegment, metaData, DefaultDatabase.LOGIC_NAME, databaseType, tableBinderContexts);
        assertThat(actual.getTableName().getOriginalDatabase().getValue(), is("sharding_db"));
        assertThat(actual.getTableName().getOriginalSchema().getValue(), is("sharding_db"));
    }
    
    @Test
    void assertBindWithoutSchemaForMySQL() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("t_order")));
        ShardingSphereMetaData metaData = createMetaData();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        SimpleTableSegment actual = SimpleTableSegmentBinder.bind(simpleTableSegment, metaData, DefaultDatabase.LOGIC_NAME, databaseType, tableBinderContexts);
        assertThat(actual.getTableName().getOriginalDatabase().getValue(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.getTableName().getOriginalSchema().getValue(), is(DefaultDatabase.LOGIC_NAME));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_order").getColumnValues()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false)));
        when(schema.getTable("pg_database").getColumnValues()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("datname", Types.VARCHAR, false, false, false, true, false),
                new ShardingSphereColumn("datdba", Types.VARCHAR, false, false, false, true, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        when(result.getDatabase("sharding_db").getSchema("sharding_db")).thenReturn(schema);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema("public")).thenReturn(schema);
        when(result.getDatabase("sharding_db").getSchema("test")).thenReturn(schema);
        return result;
    }
}
