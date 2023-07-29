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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubqueryTableSegmentBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBindWithAlias() {
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        when(selectStatement.getFrom()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(new SubquerySegment(0, 0, selectStatement));
        subqueryTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("temp")));
        ShardingSphereMetaData metaData = createMetaData();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        SubqueryTableSegment actual = SubqueryTableSegmentBinder.bind(subqueryTableSegment, metaData, DefaultDatabase.LOGIC_NAME, tableBinderContexts);
        assertTrue(actual.getAlias().isPresent());
        assertTrue(tableBinderContexts.containsKey("temp"));
    }
    
    @Test
    void assertBindWithoutAlias() {
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        when(selectStatement.getFrom()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(new SubquerySegment(0, 0, selectStatement));
        ShardingSphereMetaData metaData = createMetaData();
        Map<String, TableSegmentBinderContext> tableBinderContexts = new CaseInsensitiveMap<>();
        SubqueryTableSegment actual = SubqueryTableSegmentBinder.bind(subqueryTableSegment, metaData, DefaultDatabase.LOGIC_NAME, tableBinderContexts);
        assertFalse(actual.getAlias().isPresent());
        assertTrue(tableBinderContexts.containsKey(""));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getTable("t_order").getColumnValues()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        return result;
    }
}
