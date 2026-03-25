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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimpleTableSegmentBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @SuppressWarnings("resource")
    @Test
    void assertBindTableNotExists() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("t_not_exists")));
        ShardingSphereMetaData metaData = createMetaData();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        assertThrows(TableNotFoundException.class, () -> SimpleTableSegmentBinder.bind(
                simpleTableSegment, new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), SelectStatement.builder().databaseType(databaseType).build()), tableBinderContexts));
    }
    
    @Test
    void assertBindWithDBLinkContainsDBLink() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("t_not_exists")));
        simpleTableSegment.setDbLink(new IdentifierValue("foo_db_link"));
        ShardingSphereMetaData metaData = createMetaData();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegmentBinder.bind(simpleTableSegment,
                new SQLStatementBinderContext(metaData, "foo_db", new HintValueContext(), SelectStatement.builder().databaseType(databaseType).build()), tableBinderContexts);
        SimpleTableSegmentBinderContext tableSegmentBinderContext = (SimpleTableSegmentBinderContext) tableBinderContexts.values().iterator().next();
        assertTrue(tableSegmentBinderContext.isContainsDBLink());
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        IdentifierValue fooDatabase = new IdentifierValue("foo_db");
        IdentifierValue shardingDatabase = new IdentifierValue("sharding_db");
        IdentifierValue publicSchema = new IdentifierValue("public");
        IdentifierValue testSchema = new IdentifierValue("test");
        IdentifierValue tOrder = new IdentifierValue("t_order");
        IdentifierValue pgDatabase = new IdentifierValue("pg_database");
        when(schema.getTable(tOrder).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable(pgDatabase).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("datname", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("datdba", Types.VARCHAR, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db").getSchema("foo_db")).thenReturn(schema);
        when(result.getDatabase("sharding_db").getSchema("sharding_db")).thenReturn(schema);
        when(result.getDatabase("foo_db").getSchema("public")).thenReturn(schema);
        when(result.getDatabase("sharding_db").getSchema("test")).thenReturn(schema);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase)).thenReturn(schema);
        when(result.getDatabase(shardingDatabase).getSchema(shardingDatabase)).thenReturn(schema);
        when(result.getDatabase(fooDatabase).getSchema(publicSchema)).thenReturn(schema);
        when(result.getDatabase(shardingDatabase).getSchema(testSchema)).thenReturn(schema);
        when(result.containsDatabase(fooDatabase)).thenReturn(true);
        when(result.getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getDatabase(fooDatabase).containsSchema(fooDatabase)).thenReturn(true);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase).containsTable(tOrder)).thenReturn(true);
        when(result.containsDatabase(shardingDatabase)).thenReturn(true);
        when(result.getDatabase("sharding_db").containsSchema("sharding_db")).thenReturn(true);
        when(result.getDatabase(shardingDatabase).containsSchema(shardingDatabase)).thenReturn(true);
        when(result.getDatabase(shardingDatabase).getSchema(shardingDatabase).containsTable(tOrder)).thenReturn(true);
        return result;
    }
}
