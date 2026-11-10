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

package org.apache.shardingsphere.infra.binder.mysql.bind.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLLoadDataStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class MySQLLoadDataStatementBinderTest {
    
    @ParameterizedTest(name = "{index}: ignore={0}, replace={1}, local={2}")
    @CsvSource({"true,false,false", "false,true,true", "false,false,false"})
    void assertBindPreservesNativeInputFlags(final boolean ignore, final boolean replace, final boolean local) {
        SimpleTableSegment source = createTable("foo_source");
        SimpleTableSegment bound = createTable("foo_bound");
        MySQLLoadDataStatement statement = new MySQLLoadDataStatement(mock(DatabaseType.class), source);
        statement.setIgnore(ignore);
        statement.setReplace(replace);
        statement.setLocal(local);
        statement.setStopIndex(120);
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        try (MockedStatic<SimpleTableSegmentBinder> binder = mockStatic(SimpleTableSegmentBinder.class)) {
            binder.when(() -> SimpleTableSegmentBinder.bind(same(source), same(context), any())).thenReturn(bound);
            MySQLLoadDataStatement actual = new MySQLLoadDataStatementBinder().bind(statement, context);
            assertTrue(actual.isIgnore() == ignore);
            assertTrue(actual.isReplace() == replace);
            assertTrue(actual.isLocal() == local);
            assertThat(actual.getTable(), sameInstance(bound));
            assertThat(actual.getStopIndex(), is(120));
            assertFalse(actual.getPartitionDefinition().isPresent());
        }
    }
    
    @Test
    void assertBindPreservesPartitionSpanUsingBoundTable() {
        SimpleTableSegment source = createTable("foo_source");
        SimpleTableSegment bound = createTable("foo_bound");
        MySQLLoadDataStatement statement = new MySQLLoadDataStatement(mock(DatabaseType.class), source);
        statement.getPartitions().add(new PartitionSegment(52, 53, new IdentifierValue("p0")));
        statement.setPartitionDefinition(new PartitionDefinitionSegment(42, 54, source, statement.getPartitions()));
        SQLStatementBinderContext context = mock(SQLStatementBinderContext.class);
        try (MockedStatic<SimpleTableSegmentBinder> binder = mockStatic(SimpleTableSegmentBinder.class)) {
            binder.when(() -> SimpleTableSegmentBinder.bind(same(source), same(context), any())).thenReturn(bound);
            MySQLLoadDataStatement actual = new MySQLLoadDataStatementBinder().bind(statement, context);
            PartitionDefinitionSegment actualDefinition = actual.getPartitionDefinition().get();
            assertThat(actual.getPartitions(), is(statement.getPartitions()));
            assertThat(actualDefinition.getStartIndex(), is(42));
            assertThat(actualDefinition.getStopIndex(), is(54));
            assertThat(actualDefinition.getTable(), sameInstance(bound));
            assertThat(actualDefinition.getPartitions(), sameInstance(actual.getPartitions()));
        }
    }
    
    private SimpleTableSegment createTable(final String name) {
        return new SimpleTableSegment(new TableNameSegment(0, name.length() - 1, new IdentifierValue(name)));
    }
}
