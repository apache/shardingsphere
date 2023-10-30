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

import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.enums.JoinType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeleteMultiTableSegmentBinderTest {

    @Test
    void assertBind() {
        DeleteMultiTableSegment deleteMultiTableSegment = mock(DeleteMultiTableSegment.class, RETURNS_DEEP_STUBS);
        SQLStatementBinderContext sqlStatementBinderContext = mock(SQLStatementBinderContext.class);
        TableSegmentBinderContext tableSegmentBinderContext = mock(TableSegmentBinderContext.class);
        Map<String, TableSegmentBinderContext> map = mock(Map.class);
        DeleteMultiTableSegment actual = DeleteMultiTableSegmentBinder.bind(deleteMultiTableSegment, sqlStatementBinderContext, map);
        assertThat(actual, is(deleteMultiTableSegment));
    }

    @Test
    void assertBindWithJoinTableSegment() {
        DeleteMultiTableSegment deleteMultiTableSegment = mock(DeleteMultiTableSegment.class, RETURNS_DEEP_STUBS);
        SQLStatementBinderContext sqlStatementBinderContext = mock(SQLStatementBinderContext.class);
        TableSegmentBinderContext tableSegmentBinderContext = mock(TableSegmentBinderContext.class);
        Map<String, TableSegmentBinderContext> map = mock(Map.class);
        JoinTableSegment joinTableSegment = mock(JoinTableSegment.class);
        SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        AliasSegment aliasSegment = mock(AliasSegment.class);
        IdentifierValue identifierValue = mock(IdentifierValue.class);
        when(deleteMultiTableSegment.getRelationTable()).thenReturn(joinTableSegment);
        when(joinTableSegment.getLeft()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(identifierValue);
        when(identifierValue.getValue()).thenReturn("test");
        when(joinTableSegment.getJoinType()).thenReturn(JoinType.INNER);
        when(joinTableSegment.getRight()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(identifierValue);
        when(identifierValue.getValue()).thenReturn("test");
        when(joinTableSegment.getOnCondition().isPresent()).thenReturn(true);
        when(joinTableSegment.getOnCondition().get().getStartIndex()).thenReturn(1);
        when(joinTableSegment.getOnCondition().get().getStopIndex()).thenReturn(2);
        when(joinTableSegment.getAlias().isPresent()).thenReturn(true);
        when(joinTableSegment.getAlias().get()).thenReturn(aliasSegment);
        when(aliasSegment.getStartIndex()).thenReturn(1);
        when(aliasSegment.getStopIndex()).thenReturn(2);
        DeleteMultiTableSegment actual = DeleteMultiTableSegmentBinder.bind(deleteMultiTableSegment, sqlStatementBinderContext, map);
        assertThat(actual, is(deleteMultiTableSegment));
    }

    @Test
    void assertBindWithJoinTableSegmentWithLeft() {
        DeleteMultiTableSegment deleteMultiTableSegment = mock(DeleteMultiTableSegment.class, RETURNS_DEEP_STUBS);
        SQLStatementBinderContext sqlStatementBinderContext = mock(SQLStatementBinderContext.class);
        TableSegmentBinderContext tableSegmentBinderContext = mock(TableSegmentBinderContext.class);
        Map<String,
}
